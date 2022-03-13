package ru.crystals.pos.loyal.cash.converter;

import org.apache.commons.collections.CollectionUtils;
import ru.crystals.cards.common.CardTypes;
import ru.crystals.loyal.calculation.PositionKey;
import ru.crystals.loyal.check.CardInfo;
import ru.crystals.loyal.check.Position;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.check.bonus.BonusPosition;
import ru.crystals.loyal.check.discount.DiscountPositionEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseCardsEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.fiscalprinter.datastruct.documents.NotUsedPositionalCouponInfo;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

/**
 * Просто "математика": предоставляет метод по поиску не использованных позиционных купонов в чеке.
 * 
 * @author aperevozchikov
 */
public abstract class NotUsedPositionalCouponsSearcher {

    /**
     * Вернет те позиционные купоны, что были использованы в указанном чеке, но реально не поучаствовали в распределении скидок (т.е., по факту не
     * были "погашены").
     * 
     * @param cashReceipt
     *            кассовая версия чека
     * @param loyReceipt
     *            "калькуляторная" версия этого же чека - уже после расчета скидок
     * @return не {@code null}
     */
    public static Collection<NotUsedPositionalCouponInfo> getNotUsedCoupons(PurchaseEntity cashReceipt, Purchase loyReceipt) {
        Collection<NotUsedPositionalCouponInfo> result = new LinkedList<>();
        
        if (cashReceipt == null || loyReceipt == null || cashReceipt.getPositions().isEmpty()) {
            return result;
        }
        
        Map<String, String> goodNames = getGoodNames(cashReceipt);        
        
        // купоны, что планировали использовать:
        Collection<PositionKey> allAttachedCoupons = getAttachedPositionalCoupons(cashReceipt);
        
        // реально использованные купоны:
        Collection<PositionKey> allUsedCoupons = getUsedPositionalCoupons(loyReceipt);

        for (PositionKey allUsedCoupon : allUsedCoupons) {
            allAttachedCoupons.removeIf(pk -> pk.couponId.equals(allUsedCoupon.couponId));
        }

        for (PositionKey notUsed : allAttachedCoupons) {
            result.add(new NotUsedPositionalCouponInfo(notUsed.positionalCouponNumber, notUsed.marking, goodNames.get(notUsed.marking)));
        }
        return result;
    }
    
    /**
     * Просто вернет названия товаров в указанном чеке.
     * 
     * @param cashReceipt
     *            чек
     * @return не {@code null}; ключ - артикул товара, значение - название товара
     */
    private static Map<String, String> getGoodNames(PurchaseEntity cashReceipt) {
        Map<String, String> result = new HashMap<>();
        
        if (cashReceipt == null || cashReceipt.getPositions().isEmpty()) {
            return result;
        }
        
        for (PositionEntity p : cashReceipt.getPositions()) {
            if (p == null || p.getItem() == null) {
                continue;
            }
            result.put(p.getItem(), p.getName());
        }
        
        return result;
    }
    
    /**
     * Вернет позиции, на которые были "наклеены" указанные купоны.
     * <p/>
     * NOTE: вернет именно только позиции (т.е. {@link PositionKey#positionalCouponNumber номер купона} у всех элементов результата будет об{@code null}ен).
     * 
     * @param coupons купоны, в которых ведем поиск
     * @return не {@code null}
     */
    private static Collection<PositionKey> getPositions(Collection<PositionKey> coupons) {
        Collection<PositionKey> result = new HashSet<>();
        
        if (CollectionUtils.isEmpty(coupons)) {
            return result;
        }
        for (PositionKey pk : coupons) {
            if (pk == null || pk.marking == null) {
                continue;
            }
            result.add(new PositionKey(pk.marking, pk.originalPrice));
        }
        
        return result;
    }
    
    /**
     * Из указанной коллекции позиционных купонов отфильтрует те, что были прикреплены на указанную позицию.
     * 
     * @param coupons
     *            купоны. что фильтруем
     * @param marking
     *            артикул товара в позиции, для которой надо вернуть прикрепленные купоны
     * @param originalPrice
     *            оригинальная (до расчета скидок) цена позиции, для которой надо вернуть прикрепленные купоны
     * @return не {@code null}; среди множества {@link Map#values() значений} результата {@code null}'ей тоже не будет; ключ - номер купона, значение
     *         - коллекция "использований" этого купона
     */
    private static Map<String, Collection<PositionKey>> filter(Collection<PositionKey> coupons, String marking,
        long originalPrice) {
        Map<String, Collection<PositionKey>> result = new HashMap<>();
        
        if (CollectionUtils.isEmpty(coupons) || marking == null) {
            return result;
        }
        for (PositionKey cd : coupons) {
            if (cd.couponId == null) {
                continue;
            }
            if (!result.containsKey(marking)) {
                result.put(cd.positionalCouponNumber, new LinkedList<PositionKey>());
            }
            result.get(cd.positionalCouponNumber).add(cd);
        }// for cd
        
        return result;
    }
    
    /**
     * Вернет описание позиционных купонов, что планировали "потратить" в указанном чеке.
     * 
     * @param cashReceipt
     *            чек
     * @return не {@code null}
     */
    private static Collection<PositionKey> getAttachedPositionalCoupons(PurchaseEntity cashReceipt) {
        Collection<PositionKey> result = new LinkedList<>();
        
        if (cashReceipt == null || cashReceipt.getCards().isEmpty()) {
            return result;
        }
        for (PurchaseCardsEntity c : cashReceipt.getCards()) {
            if (c.getPosition() == null || CardTypes.ExternalCoupon.equals(c.getType())
                    || (c.getCardType() != null && CardTypes.ExternalCoupon.equals(c.getCardType().getCardTypeEnumValue()))) {
                continue;
            }
            String number = c.getNumber();
            String marking = c.getPosition().getItem();
            long originalPrice = c.getPosition().getPriceEnd();

            PositionKey cd = new PositionKey(marking, originalPrice, number, c.getId());
            result.add(cd);
        } // for c
        
        return result;
    }    
    
    /**
     * Вернет описание использованных позиционных купонов в указанном чеке.
     * 
     * @param loyReceipt
     *            чек
     * @return не {@code null}
     */
    private static Collection<PositionKey> getUsedPositionalCoupons(Purchase loyReceipt) {
        Collection<PositionKey> result = new LinkedList<>();
        
        if (loyReceipt == null || loyReceipt.getPositions().isEmpty()) {
            return result;
        }
        Map<Long, Position> idToPosition = new HashMap<>();
        for (Position pos : loyReceipt.getPositions()) {
            if (pos == null) {
                continue;
            }
            idToPosition.put(pos.getNumber(), pos);
            if (pos.getDiscountBatch() == null || pos.getGoods() == null || pos.getGoodsItem() == null) {
                continue;
            }
            for (DiscountPositionEntity dpe : pos.getDiscountBatch().getDiscounts()) {
                if (dpe == null || dpe.getPositionalCouponNumber() == null || dpe.getCount() == null || loyReceipt.getCardWithId(dpe.getPositionalCouponNumber()) == null) {
                    // эта скидка дана не по позиционному купону
                    continue;
                }
                CardInfo appliedCoupon = loyReceipt.getCardWithId(dpe.getPositionalCouponNumber());
                String marking = pos.getGoodsItem();
                long originalPrice = pos.getOriginalCost();
                
                PositionKey cd = new PositionKey(marking, originalPrice, appliedCoupon.getCardNumber(), appliedCoupon.getCardId());
                result.add(cd);
            } // for dpe
        }// for pos

        for (BonusPosition bonusPosition : loyReceipt.getBonusPositions()) {
            if (bonusPosition == null || bonusPosition.getPositionalCouponNumbers().isEmpty()) {
                // эти бонусы даны не по позиционному купону
                continue;
            }
            for(Long couponNumber : bonusPosition.getPositionalCouponNumbers()) {
                CardInfo appliedCoupon = loyReceipt.getCardWithId(couponNumber);
                String marking = bonusPosition.getGoodsCode();
                Position position = idToPosition.get(bonusPosition.getPositionOrder());
                long originalPrice = position.getOriginalCost();

                PositionKey cd = new PositionKey(marking, originalPrice, appliedCoupon.getCardNumber(), appliedCoupon.getCardId());
                result.add(cd);
            }
        } // for dpe
        return result;
    }
    
}



































