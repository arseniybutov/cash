package ru.crystals.pos.loyal.cash.converter;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.api.commons.ReceiptConverter;
import ru.crystals.discounts.ActionType;
import ru.crystals.loyal.check.Position;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.check.bonus.BonusPosition;
import ru.crystals.loyal.check.discount.AdvertisingActionResultEntity;
import ru.crystals.loyal.check.discount.DiscountBatchEntity;
import ru.crystals.loyal.check.discount.ExternalLoyaltyCheckMessageActionResult;
import ru.crystals.loyal.check.discount.ManualPositionAdvActionEntity;
import ru.crystals.pos.check.ManualPositionAdvertisingActionEntity;
import ru.crystals.pos.check.MinimalPriceAlarmType;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PositionProductionDateEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.currency.CurrencyHandler;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ExternalLoyaltyCheckMessageDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.NotUsedPositionalCouponInfo;
import ru.crystals.pos.fiscalprinter.datastruct.documents.PositionCouponsReportDocument;
import ru.crystals.utils.UnboxingUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Конвертация чека лояльности в кассовый чек
 *
 * @author ppavlov
 * @author aperevozchikov
 */
public class LoyalCashConverter {

    /**
     * черный ящик
     */
    private static final Logger log = LoggerFactory.getLogger(LoyalCashConverter.class);

    public static class PairPositions {
        Position position;

        PositionEntity positionEntity;
        private PairPositions(Position p1, PositionEntity p2) {
            this.position = p1;
            this.positionEntity = p2;
        }

        public Position getPosition() {
            return position;
        }

        public PositionEntity getPositionEntity() {
            return positionEntity;
        }
    }

    /**
     * Применение результатов расчета скидок (применение нового распределения товаров по позициям)
     *
     * @param cashPurchase    Кассовый чек для применения скидок
     * @param purchase        Чек калькулятора с расчитанными скидками
     * @param currencyHandler "округлятор" - знает как обращаться с деньгами
     */
    public static void applyPurchasePositions(PurchaseEntity cashPurchase, Purchase purchase, CurrencyHandler currencyHandler) {
        // Ассоциируем позиции оригинального чека с позициями чека лояльности
        List<PairPositions> associatedPositions;
        try {
            associatedPositions = getAssociatedPositions(cashPurchase.getPositions(), purchase.getPositions());
        } catch (PositionNotFoundException e) {
            log.error("Exception on associating positions.", e);
            return;
        }
        // Восстанавливаем последовательность номеров позиций
        restoreNumberSequence(associatedPositions);

        Collection<NotUsedPositionalCouponInfo> notUsedPositionalCoupons = NotUsedPositionalCouponsSearcher.getNotUsedCoupons(cashPurchase, purchase);
        if (CollectionUtils.isNotEmpty(notUsedPositionalCoupons)) {
            cashPurchase.getServiceDocs().add(new PositionCouponsReportDocument(notUsedPositionalCoupons));
        }

        // удалим все купоны внешних программы лояльности с прошлого расчета
        cashPurchase.getServiceDocs().removeIf(next -> next instanceof ExternalLoyaltyCheckMessageDocument);

        // найдем и добавим все купоны внешних программ лояльности
        for (AdvertisingActionResultEntity result : purchase.getAdvertisingActionResults()) {
            if (result instanceof ExternalLoyaltyCheckMessageActionResult) {
                ExternalLoyaltyCheckMessageDocument extLoyaltyDoc = new ExternalLoyaltyCheckMessageDocument(((ExternalLoyaltyCheckMessageActionResult) result).getText(), ((ExternalLoyaltyCheckMessageActionResult) result).getBarcode());
                extLoyaltyDoc.setSeparatePrint(result.isSeparatePrint());
                cashPurchase.getServiceDocs().add(extLoyaltyDoc);
            }
        }

        // Обновляем оригинальный чек в соответствии с чеком лояльности
        updateCashPurchase(cashPurchase, purchase, currencyHandler, associatedPositions);
    }

    private static void restoreNumberSequence(List<PairPositions> associatedPositions) {
        associatedPositions.sort((o1, o2) -> {
            int result = (int) Math.signum(o1.position.getNumber() - o2.position.getNumber());
            if (result == 0) {
                // номера позиций совпали. еще и по ID'шникам упорядочим. NULL'и (их не должно быть!) - в зад
                if (o1.position.getId() == null) {
                    result = 1;
                } else if (o2.position.getId() == null) {
                    result = -1;
                } else {
                    result = (int) Math.signum(o1.position.getId() - o2.position.getId());
                }
            }
            return result;
        });
        long number = 1;
        for (PairPositions pair : associatedPositions) {
            pair.position.setNumber(number);
            number++;
        }
    }

    public static List<PairPositions> getAssociatedPositions(List<PositionEntity> cashPositions, List<Position> positions) throws PositionNotFoundException {
        // Если в результате расчета скидок от позиции "отщепились" новые, то вынесем эту позицию наверх. Иначе отщепленные позиции могут встать перед ней, что
        // приведет к некорректному маппингу на кассовые позиции, т.к. обход списка последовательный - для отщепленной калькуляторной позиции подберется
        // исходная кассовая, а для исходной калькуляторной позиции будет создана клонированная кассовая позиция без id, который позже будет присвоен
        // и станет совершенно другим.
        positions.sort((o1, o2) -> {
            if (o1.getNumber() != o2.getNumber() || o1.getParentId() == null && o2.getParentId() == null) {
                return 0;
            }
            return o1.getParentId() != null && o2.getParentId() == null ? 1 : -1;
        });
        Set<Long> numbers = new HashSet<>();
        List<PairPositions> pairs = new ArrayList<>(positions.size());
        for (Position position : positions) {
            PositionEntity cashPositionByNumber = getCashPosition(cashPositions, positions, position);
            if (cashPositionByNumber == null) {
                throw new PositionNotFoundException(position, cashPositions);
            }
            if (numbers.contains(position.getNumber())) {
                cashPositionByNumber = cashPositionByNumber.cloneSplitted();
                cashPositionByNumber.clearProductionDates();
            }
            pairs.add(new PairPositions(position, cashPositionByNumber));
            numbers.add(position.getNumber());
        }
        return pairs;
    }

    private static PositionEntity getCashPositionByNumber(List<PositionEntity> cashPositions, long number) {
        for (PositionEntity position : cashPositions) {
            if (position.getNumber() == number) {
                return position;
            }
        }
        return null;
    }

    private static PositionEntity getCashPosition(List<PositionEntity> cashPositions, List<Position> positions, Position position) {
        PositionEntity result = getCashPositionByNumber(cashPositions, position.getNumber());
        if(result != null) {
            return result;
        }
        if(position.getParentId() == null) {
            return null;
        }
        Position p = null;
        for(Position pos : positions) {
            if(position.getParentId().equals(pos.getId())) {
                p = pos;
                break;
            }
        }
        return p == null ? null : getCashPositionByNumber(cashPositions, p.getNumber());
    }

    private static void updateCashPurchase(PurchaseEntity cashPurchase, Purchase purchase, CurrencyHandler currencyHandler, List<PairPositions> associatedPositions) {
        List<PositionEntity> newListForPurchase = new ArrayList<>();
        for (PairPositions associatedPosition : associatedPositions) {
            applyPositionDiscount(associatedPosition.positionEntity, associatedPosition.position, currencyHandler);
            ReceiptConverter.mergeExtendedAttributes(associatedPosition.positionEntity, associatedPosition.position.getPositionAttributeMap());
            newListForPurchase.add(associatedPosition.positionEntity);
        }

        // для разделённых позиций распределим бонусы
        Map<PairPositions, List<PairPositions>> keyMapDividedPositions = new HashMap<>();
        for (PairPositions associatedPosition : associatedPositions) {
            if(associatedPosition.position.getParentId() != null) { // это разделённая позиция
                PairPositions parentPosition = recursionParentFind(associatedPosition.position.getParentId(), associatedPositions, null);
                if(parentPosition != null) {
                    shareTwoPositionsProductionDate(parentPosition.positionEntity, associatedPosition.positionEntity);
                    if(!keyMapDividedPositions.containsKey(parentPosition)) {
                        keyMapDividedPositions.put(parentPosition, new ArrayList<>());
                    }
                    keyMapDividedPositions.get(parentPosition).add(associatedPosition);
                } // если не нашли парента, то странно весьма
            }
        }
        shareTwoPositionsBonuses(purchase, keyMapDividedPositions, currencyHandler);

        cashPurchase.setDiscountValueTotal(purchase.getDiscountValueTotal());
        cashPurchase.setPositions(newListForPurchase);
        cashPurchase.setNonDistributedDiscount(purchase.getNonDistributedDiscount());

        // SR-1758: в результате деления позиции у чека могла измениться стоимость ДО расчета скидок (оригинальная стоимость чека):
        long sumStart = cashPurchase.getPositions().stream().mapToLong(p -> p == null ? 0L : ((p.getSum() == null ? 0L : p.getSum()) + p.getSumDiscount())).sum();
        cashPurchase.setCheckSumStart(sumStart);
    }

    /**
     * Рекурсивно найдёт главного парента, от которого всё отпочковалось
     * @param parentId локальный ИД предка
     * @param associatedPositions все позиции
     * @param defaultReturn ответ, если не нашли ничего
     * @return самый дальний предок
     */
    private static PairPositions recursionParentFind(Long parentId, List<PairPositions> associatedPositions, PairPositions defaultReturn) {
        Optional<PairPositions> first = associatedPositions.stream().filter(item -> parentId.equals(item.position.getId()))
                .findFirst();
        if(first.isPresent()) {
            PairPositions pairPosition = first.get();
            if(pairPosition.position.getParentId() != null) {
                return recursionParentFind(pairPosition.position.getParentId(), associatedPositions, pairPosition);
            } else {
                return pairPosition;
            }
        }
        return defaultReturn;
    }

    /**
     * Обновляет посчитанные поля кассовой позиции из позиции калькуляторной
     *
     * @param cashPosition    Исходная кассовая позиция
     * @param position        Посчитаная калькуляторная позиция
     * @param currencyHandler "округлятор" - знает как обращаться с деньгами
     */
    private static void applyPositionDiscount(PositionEntity cashPosition, Position position, CurrencyHandler currencyHandler) {
        if ((cashPosition == null) || (position == null)) {
            return;
        }

        // Номер позиции
        cashPosition.setNumber(position.getNumber());

        // Идентификатор позиции-предка
        cashPosition.setParentId(position.getParentId());

        // возможно изменившееся количество позиций
        cashPosition.setQnty(position.getCount());
        cashPosition.setCollapsible(position.isCollapsible());

        DiscountBatchEntity discountBatch = position.getDiscountBatch();
        if (discountBatch != null && !discountBatch.isEmpty()) {
            // Итоговая стоимость одной единицы товара в позиции с учетом всех
            // скидок. Печатается на чековых формах.
            if (position.getCount() <= 0) {
                log.error("applyPositionDiscount: good [position: {}] quantity is non-positive [{}]!", position, position.getCount());
            } else {
                cashPosition.setPriceEnd(position.getEndPrice(currencyHandler));
                if (discountBatch.isDiscountLimited()) {
                    cashPosition.setMinimalPriceAlarm(MinimalPriceAlarmType.FIX_PRICE);
                }
            }

            // Общее значение позиционных скидок на позицию
            cashPosition.setSumDiscount(position.getDiscountValue());
        } else {
            cashPosition.setPriceEnd(cashPosition.getPriceStart());
            cashPosition.setSumDiscount(0L);
        }

        // Итоговая сумма позиции
        cashPosition.setSum(position.getSum());

        // сумма скидок на позицию на основании НСП
        long salesTaxDiscount = discountBatch == null ? 0L :
                discountBatch.getDiscounts().stream().filter(dp -> dp.getActionType() == ActionType.SALES_TAX_DISCOUNT)
                .mapToLong(dp -> UnboxingUtils.valueOf(dp.getValueCalculated())).sum();
        long salesTax = cashPosition.getProduct() != null ? UnboxingUtils.valueOf(cashPosition.getProduct().getSalesTax()) : 0L;
        float nds = UnboxingUtils.valueOf(cashPosition.getNds());
        cashPosition.setSalesTaxSum(CurrencyUtil.calculateSalesTaxSum(cashPosition.getSum(), salesTax, salesTaxDiscount, nds));

        //update manual discounts
        if (position.getManualAdvActions() != null && cashPosition.getManualAdvertisingActions() != null) {
            for (ManualPositionAdvActionEntity action : position.getManualAdvActions()) {
                for (ManualPositionAdvertisingActionEntity cashAction : cashPosition.getManualAdvertisingActions()) {
                    if (action.getActionGuid() != null && action.getActionGuid().equals(cashAction.getActionGuid())) {
                        cashAction.setQnty(action.getCount());
                    }
                }
            }
        } else if (position.getManualAdvActions() == null) {
            cashPosition.setManualAdvertisingActions(null);
        }

        if (log.isTraceEnabled()) {
            log.trace(cashPosition.getName() + " - \t"
                    + Math.round(cashPosition.getSumDiscount() / (double) (cashPosition.getSum() + cashPosition.getSumDiscount()) * 100) + " %");
        }
    }

    /**
     * Такое разделение в принципе никому не нужно, но нужно для Копилки и будет работать только там, т.к. там заполняется ИД позиции
     * @see BonusPosition#getPositionId
     */
    private static void shareTwoPositionsBonuses(Purchase purchase,
                                                 Map<PairPositions, List<PairPositions>> keyMapDividedPositions,
                                                 CurrencyHandler currencyHandler) {
        // если позиция поделилась, надо поделить и бонусы на эту позицию
        final List<BonusPosition> newPositions = new ArrayList<>();

        purchase.getBonusPositions().stream().filter(pos -> pos.getBonusAmount() > 0)
                .forEach(pos -> purchase.getPositions().stream()
                        .filter(it -> it.getId().equals(pos.getPositionId()))
                        .mapToLong(Position::getNumber)
                        .findFirst().ifPresent(pos::setPositionOrder));
        keyMapDividedPositions.forEach((parent, childs) ->
                purchase.getBonusPositions().stream()
                        .filter(pos -> parent.position.getId().equals(pos.getPositionId()) && pos.getBonusAmount() > 0)
                        .forEach(bp -> newPositions.addAll(processBonusPosition(bp, parent, childs, currencyHandler))));
        purchase.getBonusPositions().addAll(newPositions);
    }

    private static List<BonusPosition> processBonusPosition(
            BonusPosition bp, PairPositions parent, List<PairPositions> childs, CurrencyHandler currencyHandler) {
        List<BonusPosition> newPositions = new ArrayList<>();
        bp.setPositionOrder(parent.position.getNumber());
        long commonQuantity = bp.getQuantity() != null ? bp.getQuantity()
                : parent.position.getCount() + childs.stream().mapToLong(child -> child.position.getCount()).sum();
        if (commonQuantity <= parent.position.getCount()) {
            return newPositions;
        }

        long bonusAmount = bp.getBonusAmount();
        bp.setQuantity(parent.position.getCount());
        long quantityToDistr = commonQuantity - parent.position.getCount();
        for (PairPositions child : childs) {
            BonusPosition bonusPositionTwo = bp.cloneLight();
            long childPosQuantity = Math.min(quantityToDistr, child.position.getCount());
            long newBonusAmount = currencyHandler.round(1.0 * bonusAmount * childPosQuantity / commonQuantity);
            bonusPositionTwo.setPositionOrder(child.position.getNumber());
            bonusPositionTwo.setQuantity(childPosQuantity);
            bonusPositionTwo.setBonusAmount(newBonusAmount);
            bp.setBonusAmount(bp.getBonusAmount() - newBonusAmount);
            newPositions.add(bonusPositionTwo);
            quantityToDistr -= childPosQuantity;
            if (quantityToDistr <= 0) {
                break;
            }
        }
        return newPositions;
    }

    private static void shareTwoPositionsProductionDate(PositionEntity position1, PositionEntity position2) {
        if(position2.getQnty() <= 0) {
            return;
        }
        Set<PositionProductionDateEntity> productionDates = position1.getProductionDates();
        if(productionDates != null && !productionDates.isEmpty()) {
            Set<PositionProductionDateEntity> toSecondProductionDates = new HashSet<>();
            long index = 0;
            long splitLine = position2.getQnty(); // линия разграничения
            for(PositionProductionDateEntity productionDate : productionDates) {
                toSecondProductionDates.add(productionDate);
                index += productionDate.getQuantity();
                if(index > splitLine) {
                    long over = index - splitLine; // на сколько заступили за линию
                    productionDate.setQuantity(productionDate.getQuantity() - over);
                    PositionProductionDateEntity dateEntity = new PositionProductionDateEntity(productionDate);
                    dateEntity.setPosition(position1);
                    dateEntity.setQuantity(over);
                    productionDates.add(dateEntity);
                }
                if(index >= splitLine) { // если превысили или вписались
                    break;
                }
            }
            productionDates.removeAll(toSecondProductionDates); // отсеим те, что перекочевало
            toSecondProductionDates.forEach(productionDate -> productionDate.setPosition(position2));
            position2.getProductionDates().addAll(toSecondProductionDates);
        }
    }

}
