package ru.crystals.pos.loyal.cash.utils;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.crystals.discount.processing.entity.LoyAdvActionInPurchaseEntity;
import ru.crystals.discount.processing.entity.LoyBonusPlastekTransactionEntity;
import ru.crystals.discount.processing.entity.LoyBonusPositionEntity;
import ru.crystals.discount.processing.entity.LoyBonusSberbankTransactionEntity;
import ru.crystals.discount.processing.entity.LoyBonusTransactionEntity;
import ru.crystals.discount.processing.entity.LoyDiscountPositionEntity;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.discounts.ActionType;
import ru.crystals.pos.catalog.ProductConfig;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PositionSpiritsEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.currency.CurrencyHandler;
import ru.crystals.pos.currency.CurrencyHandlerFactory;

/**
 * Created by v.osipov on 30.03.2017.
 */
public class EntitiesTestUtils {

    /**
     * Собирает информацию о бонусных транзакциях в транзакции лояльности
     */
    public static Collection<BonusTxDescriptor> collectBonusTxs(LoyTransactionEntity loyTx) {

        Collection<BonusTxDescriptor> bonusTxs = new ArrayList<>();
        for (LoyBonusTransactionEntity bonusTx : loyTx.getBonusTransactions()) {
            bonusTxs.add(new BonusTxDescriptor(bonusTx.getSumAmount(), bonusTx.getBonusAmount(), bonusTx.getAdvAction().getGuid()));

        }
        return bonusTxs;
    }

    public static Collection<BonusTxDescriptor> collectSberBonusTxs(LoyTransactionEntity loyTx) {

        Collection<BonusTxDescriptor> bonusTxs = new ArrayList<>();
        for(LoyBonusSberbankTransactionEntity sberBonusTx : loyTx.getBonusSberbankTransactions()){
            bonusTxs.add(new BonusTxDescriptor(0l, 0l, 0l, sberBonusTx.getBnsChange(), 0l));
        }
        return bonusTxs;
    }
    public static Collection<BonusTxDescriptor> collectPlastekBonusTxs(LoyTransactionEntity loyTx) {

        Collection<BonusTxDescriptor> bonusTxs = new ArrayList<>();
        for(LoyBonusPlastekTransactionEntity plastekBonusTx : loyTx.getBonusPlastekTransactions()){
            bonusTxs.add(new BonusTxDescriptor(0l, 0l, 0l,0l, plastekBonusTx.getBnsChange()));
        }
        return bonusTxs;
    }

    /**
     * Создаст чек возврата для указанного чека продажи
     */
    public static PurchaseEntity createRetReceipt(PurchaseEntity originalReceipt, Collection<PositionEntity> retPositions) {

        PurchaseEntity result = new PurchaseEntity();
        result.setReturn();
        result.setCheckSumStart(0L);
        result.setDiscountValueTotal(0L);
        long posNo = 0;
        for (PositionEntity retPos : retPositions) {

            posNo++;
            retPos.setNumber(posNo);

            result.getPositions().add(retPos);
            result.setCheckSumStart(result.getCheckSumStart() + retPos.getSum() + retPos.getSumDiscount());
            result.setDiscountValueTotal(result.getDiscountValueTotal() + retPos.getSumDiscount());
        }

        return result;
    }

    /**
     * Создаст "позицию возврата" для указанной оригинальной позиции
     */
    public static PositionEntity createRetPos(PositionEntity original, long qnty, CurrencyHandler currencyHandler) {
        PositionEntity result = new PositionEntity();

        long sum = qnty == original.getQnty() || original.getQnty() == 0 ? original.getSum() : currencyHandler.round(1.0 * qnty / original.getQnty() * original.getSum());

        result.setQnty(qnty);
        result.setPriceStart(original.getPriceStart());
        result.setSum(sum);
        long discount = currencyHandler.getPositionSum(original.getPriceStart(), qnty) - sum;
        if (original.getSumDiscount() == 0) {
            discount = 0L;
        }
        result.setSumDiscount(discount);
        result.setNumberInOriginal(original.getNumber());

        return result;
    }

    /**
     * Создает TX лояльности на указанный чек
     */
    public static LoyTransactionEntity createLoyTx(PurchaseEntity receipt, Collection<LoyDiscountPositionEntity> discounts, Collection<LoyBonusPositionEntity> bonuses, long moneyToBonusesRate) {
        LoyTransactionEntity result = new LoyTransactionEntity();
        result.setOperationType(LoyTransactionEntity.OPERATION_TYPE_SALE);

        result.setDiscountValueTotal(0L);

        for (LoyDiscountPositionEntity ldpe : discounts) {
            result.getDiscountPositions().add(ldpe);
            result.setDiscountValueTotal(result.getDiscountValueTotal() + ldpe.getDiscountAmount());

            if(ldpe.getAdvAction().getActionType().equals(ActionType.BONUS_CFT)){
                List<LoyBonusSberbankTransactionEntity> sberbankTransactionEntities = new ArrayList<>();

                LoyBonusSberbankTransactionEntity entity = new LoyBonusSberbankTransactionEntity();
                entity.setBnsChange(-ldpe.getDiscountAmount());

                result.getBonusSberbankTransactions().add(entity);
            }
        }

        result.getBonusPositions().addAll(bonuses);

        createBonusTxs(result, moneyToBonusesRate);

        receipt.setDiscountValueTotal(result.getDiscountValueTotal());

        return result;
    }



    /**
     * Создает бонусную транзакцию
     */
    public static void createBonusTxs(LoyTransactionEntity loyTx, long moneyToBonusesRate) {

        Map<Long, LoyBonusTransactionEntity> bonusTxMap = new HashMap<>();

        for (LoyDiscountPositionEntity ldpe : loyTx.getDiscountPositions()) {
            if (ldpe.getAdvAction().getActionType() != ActionType.BONUS_SR10) {
                continue;
            }

            LoyBonusTransactionEntity bte = bonusTxMap.get(ldpe.getAdvAction().getGuid());
            if (bte == null) {
                bte = new LoyBonusTransactionEntity();
                bte.setAdvAction(ldpe.getAdvAction());
                bte.setDiscountCard(ldpe.getCardNumber());
                bonusTxMap.put(ldpe.getAdvAction().getGuid(), bte);
                loyTx.getBonusTransactions().add(bte);
            }
            bte.setSumAmount(bte.getSumAmount() - ldpe.getDiscountAmount());
            bte.setBonusAmount(bte.getSumAmount() * moneyToBonusesRate);
        }

        for (LoyBonusPositionEntity lbpe : loyTx.getBonusPositions()) {
            LoyBonusTransactionEntity bte = bonusTxMap.get(lbpe.getAdvAction().getGuid());
            if (bte == null) {
                bte = new LoyBonusTransactionEntity();
                bte.setAdvAction(lbpe.getAdvAction());
                bonusTxMap.put(lbpe.getAdvAction().getGuid(), bte);
                loyTx.getBonusTransactions().add(bte);
            }
            bte.setBonusAmount(bte.getBonusAmount() + lbpe.getBonusAmount());
        }

    }

    /**
     * Создаст скидку на указанную позицию
     */
    public static LoyDiscountPositionEntity createDiscountPosition(PositionEntity pos, long discountQnty, long discountValue, long actionGuid, ActionType actionType, CurrencyHandler currencyHandler) {
        LoyDiscountPositionEntity result = new LoyDiscountPositionEntity();

        LoyAdvActionInPurchaseEntity appliedAction = new LoyAdvActionInPurchaseEntity();
        appliedAction.setGuid(actionGuid);
        appliedAction.setActionType(actionType);
        result.setAdvAction(appliedAction);

        long sumDiscount = currencyHandler.roundDown(discountValue);

        result.setPositionOrder(pos.getNumberInt());
        result.setGoodCode(pos.getItem());
        result.setDiscountAmount(sumDiscount);
        result.setQnty(discountQnty);

        pos.setSumDiscount(pos.getSumDiscount() + sumDiscount);
        pos.setSum(pos.getSum() - sumDiscount);

        return result;
    }

    /**
     * Бонусы начисленные согласно указанной позиции
     */
    public static LoyBonusPositionEntity createBonusPosition(PositionEntity pos, long bonusValue, long actionGuid) {
        LoyBonusPositionEntity result = new LoyBonusPositionEntity();

        LoyAdvActionInPurchaseEntity appliedAction = new LoyAdvActionInPurchaseEntity();
        appliedAction.setGuid(actionGuid);
        result.setAdvAction(appliedAction);

        result.setPositionOrder(pos.getNumberInt());
        result.setBonusAmount(bonusValue);

        return result;
    }

    /**
     * Создает чек продажи.
     */
    public static PurchaseEntity createReceipt(Collection<PositionEntity> positions) {
        PurchaseEntity result = new PurchaseEntity();

        long sum = 0;
        for (PositionEntity pos : positions) {
            result.getPositions().add(pos);
            sum += pos.getSum();
        }
        result.setCheckSumStart(sum);
        result.setSale();
        result.setDateCreate(new Date());

        return result;
    }

    /**
     * Создает чек продажи (из дескриптора)
     */
    public static PurchaseEntity createReceipt(
            CurrencyHandler currencyHandler,
            PurchaseDescriptor purchaseDescriptor) {

        List<PositionEntity> positionEntities = new ArrayList<>();

        for(PositionDescriptor positionDescriptor : purchaseDescriptor.positions){
            positionEntities.add(
                    createPosition(currencyHandler, positionDescriptor.number, positionDescriptor.qnty, positionDescriptor.price, positionDescriptor.id)
            );
        }

        PurchaseEntity result = createReceipt(positionEntities);

        result.setId(purchaseDescriptor.id);

        return result;
    }

    public static PurchaseEntity createReceipt(
            PurchaseDescriptor purchaseDescriptor) {
        return createReceipt(new CurrencyHandlerFactory().getCurrencyHandler(null), purchaseDescriptor);
    }

    /**
     * Создает позицию
     */
    public static PositionEntity createPosition(CurrencyHandler currencyHandler, long number, long qnty, long price, long id) {
        return createPosition(currencyHandler, number, qnty, price, id, null);
    }

    /**
     * Создает позицию
     */
    public static PositionEntity createPosition(CurrencyHandler currencyHandler, long number, long qnty, long price, long id, String item) {
        PositionEntity result = new PositionEntity();

        result.setId(id);
        result.setNumber(number);
        result.setItem(item);
        result.setQnty(qnty);
        result.setPriceStart(price);
        result.setSum(currencyHandler.getPositionSum(price, qnty));
        result.setSumDiscount(0L);
        result.setDateTime(new Date());
        result.setProductSilent(new ProductEntity());
        result.getProduct().setProductConfig(new ProductConfig());
        result.getProduct().setItem(item);

        return result;
    }

    /**
     * Создает алкопозицию
     */
    public static PositionEntity createSpiritsPosition(CurrencyHandler currencyHandler, long number, long qnty, long price, long minPrice, long id, String item) {
        PositionSpiritsEntity result = new PositionSpiritsEntity();

        result.setId(id);
        result.setNumber(number);
        result.setItem(item);
        result.setQnty(qnty);
        result.setPriceStart(price);
        result.setAlcoMinPrice(minPrice);
        result.setSum(currencyHandler.getPositionSum(price, qnty));
        result.setSumDiscount(0L);
        result.setDateTime(new Date());

        return result;
    }

}
