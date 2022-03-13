package ru.crystals.pos.loyal.cash.service;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import ru.crystals.discount.processing.entity.LoyAdvActionInPurchaseEntity;
import ru.crystals.discount.processing.entity.LoyBonusDiscountTransactionEntity;
import ru.crystals.discount.processing.entity.LoyBonusPlastekTransactionEntity;
import ru.crystals.discount.processing.entity.LoyBonusPositionEntity;
import ru.crystals.discount.processing.entity.LoyBonusSberbankTransactionEntity;
import ru.crystals.discount.processing.entity.LoyBonusTransactionEntity;
import ru.crystals.discount.processing.entity.LoyChequeCouponEntity;
import ru.crystals.discount.processing.entity.LoyDiscountCardEntity;
import ru.crystals.discount.processing.entity.LoyDiscountPositionEntity;
import ru.crystals.discount.processing.entity.LoyProcessingCouponEntity;
import ru.crystals.discount.processing.entity.LoyPurchaseCardEntity;
import ru.crystals.discount.processing.entity.LoyPurchaseEntity;
import ru.crystals.discount.processing.entity.LoyPurchasePaymentEntity;
import ru.crystals.discount.processing.entity.LoyPurchasePositionEntity;
import ru.crystals.discount.processing.entity.LoyQuestionaryEntity;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.discounts.ActionType;
import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.discounts.ApplyMode;
import ru.crystals.loyal.actions.provider.LoyActionsProvider;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.SentToServerStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Утилиты для тестирования.
 * 
 * @author aperevozchikov
 */
@SuppressWarnings("WeakerAccess")
public abstract class LoyalServiceTestUtils {

    /**
     * просто создает и возвращает некую "болванку" транзакции лояльности - транзакцию лояльности с произвольно заполненными полями.
     * 
     * @return произвольная (возможно бессмысленная) транзакция лояльности - но со всеми заполненными полями (собственными и в связанных
     *         полях-коллекциях).
     */
    public static LoyTransactionEntity buildLoyTx() {
        LoyTransactionEntity tx = new LoyTransactionEntity();
        tx.setSaleTime(new Date());
        tx.setTransactionTime(tx.getSaleTime());
        tx.setCashNumber(RandomUtils.nextInt());
        tx.setDiscountValueTotal(RandomUtils.nextLong());
        tx.setFilename("file://" + RandomStringUtils.randomAlphabetic(16));
        tx.setNeedSendAccumulation(RandomUtils.nextBoolean());
        tx.setNeedSendBonus(RandomUtils.nextBoolean());
        tx.setNeedSendToErp(RandomUtils.nextBoolean());
        tx.setOperationType(RandomUtils.nextBoolean());
        tx.setPurchaseAmount(RandomUtils.nextInt());
        tx.setPurchaseNumber(RandomUtils.nextLong());
        tx.setSentToServerStatus(SentToServerStatus.UNCOMMITED);
        tx.setShiftNumber(RandomUtils.nextLong());
        tx.setShopNumber(RandomUtils.nextLong());
        tx.setStatus(RandomUtils.nextInt());
        int collectionsSize = 10;

        //
        for (int i = 0; i < collectionsSize; i++) {
            tx.getBonusDiscountTransactions().add(new LoyBonusDiscountTransactionEntity());
            // у нас поле loy_bonusdiscount_transactions.bonusTransactionId длиной всего лишь 255 (по дефолту),
            //  а String#getBytes может на каждый символ 3-4 байта вернуть (разные кодировки могут быть еще "тяжелее"):
            //tx.getBonusDiscountTransactions().get(i).setBonusTransactionId(RandomStringUtils.random(255).getBytes());
            tx.getBonusDiscountTransactions().get(i).setBonusTransactionId(RandomStringUtils.random(50).getBytes());
            tx.getBonusDiscountTransactions().get(i).setBonusTransactionIdAsString(RandomStringUtils.random(255));
        }
        //
        for (int i = 0; i < collectionsSize; i++) {
            tx.getBonusPlastekTransactions().add(new LoyBonusPlastekTransactionEntity());
            tx.getBonusPlastekTransactions().get(i).setBnsChange(RandomUtils.nextLong());
            tx.getBonusPlastekTransactions().get(i).setCardNumber(RandomStringUtils.random(16));
            tx.getBonusPlastekTransactions().get(i).setCheckNumber(RandomUtils.nextInt());
            tx.getBonusPlastekTransactions().get(i).setShiftNumber(RandomUtils.nextInt());
            tx.getBonusPlastekTransactions().get(i).setTransaction(tx);
        }
        //
        for (int i = 0; i < collectionsSize; i++) {
            tx.getBonusPositions().add(new LoyBonusPositionEntity());
            tx.getBonusPositions().get(i).setAdvAction(buildLoyAdvActionInPurchase());
            tx.getBonusPositions().get(i).setBonusAmount(RandomUtils.nextLong());
            tx.getBonusPositions().get(i).setGoodCode(RandomStringUtils.random(16));
            tx.getBonusPositions().get(i).setPositionOrder(i + 1);
        }
        //
        for (int i = 0; i < collectionsSize; i++) {
            tx.getBonusSberbankTransactions().add(new LoyBonusSberbankTransactionEntity());
            tx.getBonusSberbankTransactions().get(i).setAmount(RandomUtils.nextLong());
            tx.getBonusSberbankTransactions().get(i).setBnsChange(RandomUtils.nextLong());
            tx.getBonusSberbankTransactions().get(i).setBnsDelayChange(RandomUtils.nextLong());
            tx.getBonusSberbankTransactions().get(i).setClientId(RandomStringUtils.random(16));
            tx.getBonusSberbankTransactions().get(i).setDateTime(new Date());
            tx.getBonusSberbankTransactions().get(i).setLocation(RandomStringUtils.random(16));
            tx.getBonusSberbankTransactions().get(i).setMode(LoyBonusSberbankTransactionEntity.Mode.ONLINE);
            tx.getBonusSberbankTransactions().get(i).setPan4(RandomStringUtils.random(16));
            tx.getBonusSberbankTransactions().get(i).setPartnerId(RandomUtils.nextLong());
            tx.getBonusSberbankTransactions().get(i).setTerminal(RandomStringUtils.random(16));
            tx.getBonusSberbankTransactions().get(i).setTransactionId(RandomStringUtils.random(16));
            tx.getBonusSberbankTransactions().get(i).setTransactionType(LoyBonusSberbankTransactionEntity.Type.NONSIGNIFICANT);
        }
        //
        for (int i = 0; i < collectionsSize; i++) {
            tx.getBonusTransactions().add(new LoyBonusTransactionEntity());
            tx.getBonusTransactions().get(i).setAdvAction(tx.getBonusPositions().get(i).getAdvAction());
            tx.getBonusTransactions().get(i).setBonusAccountType(RandomUtils.nextLong());
            tx.getBonusTransactions().get(i).setBonusAmount(RandomUtils.nextLong());
            tx.getBonusTransactions().get(i).setBonusPeriodFinish(new Date());
            tx.getBonusTransactions().get(i).setBonusPeriodStart(new Date());
            tx.getBonusTransactions().get(i).setDiscountCard(RandomStringUtils.random(16));
        }
        //
        for (int i = 0; i < collectionsSize; i++) {
            tx.getChequeCoupons().add(new LoyChequeCouponEntity());
            tx.getChequeCoupons().get(i).setAdvAction(tx.getBonusPositions().get(i).getAdvAction());
            tx.getChequeCoupons().get(i).setCouponBarcode(RandomStringUtils.random(16));
            tx.getChequeCoupons().get(i).setDiscountProcent(RandomUtils.nextInt());
            tx.getChequeCoupons().get(i).setDiscountSum(RandomUtils.nextLong());
        }
        //
        for (int i = 0; i < collectionsSize; i++) {
            tx.getDiscountCards().add(new LoyDiscountCardEntity());
            tx.getDiscountCards().get(i).setAdvAction(tx.getBonusPositions().get(i).getAdvAction());
            tx.getDiscountCards().get(i).setCardNumber(RandomStringUtils.randomNumeric(16));
            tx.getDiscountCards().get(i).setCardType(RandomStringUtils.randomNumeric(16));
        }
        //
        for (int i = 0; i < collectionsSize; i++) {
            tx.getDiscountPositions().add(buildLoyDiscountPosition(tx.getBonusPositions().get(i).getAdvAction()));
        }
        //
        for (int i = 0; i < collectionsSize; i++) {
            tx.getProcessingCoupons().add(new LoyProcessingCouponEntity());
            tx.getProcessingCoupons().get(i).setAdvAction(tx.getBonusPositions().get(i).getAdvAction());
            tx.getProcessingCoupons().get(i).setCardNumber(RandomStringUtils.randomNumeric(16));
            tx.getProcessingCoupons().get(i).setCouponBarcode(RandomStringUtils.randomNumeric(16));
            tx.getProcessingCoupons().get(i).setCouponPeriodFinish(new Date());
            tx.getProcessingCoupons().get(i).setCouponPeriodStart(new Date());
            tx.getProcessingCoupons().get(i).setCouponPrefix(RandomStringUtils.randomNumeric(16));
            tx.getProcessingCoupons().get(i).setDiscountAmount(RandomUtils.nextLong());
            tx.getProcessingCoupons().get(i).setDiscountType(RandomUtils.nextInt());
            tx.getProcessingCoupons().get(i).setMaxDiscount(RandomUtils.nextLong());
            tx.getProcessingCoupons().get(i).setUsed(RandomUtils.nextBoolean());
        }
        //
        tx.setPurchase(new LoyPurchaseEntity());
        LoyPurchasePaymentEntity paymentEntity = new LoyPurchasePaymentEntity();
        paymentEntity.setPurchase(tx.getPurchase());
        tx.getPurchase().getPayments().add(paymentEntity);
        LoyPurchasePositionEntity positionEntity = new LoyPurchasePositionEntity();
        positionEntity.setPurchase(tx.getPurchase());
        tx.getPurchase().getPositions().add(positionEntity);
        //
        for (int i = 0; i < collectionsSize; i++) {
            tx.getPurchaseCards().add(new LoyPurchaseCardEntity());
            tx.getPurchaseCards().get(i).setCardNumber(RandomStringUtils.randomNumeric(16));
            tx.getPurchaseCards().get(i).setCardType(RandomStringUtils.randomNumeric(16));
        }
        //
        for (int i = 0; i < collectionsSize; i++) {
            tx.getQuestionaries().add(new LoyQuestionaryEntity());
            tx.getQuestionaries().get(i).setAdvAction(tx.getBonusPositions().get(i).getAdvAction());
            tx.getQuestionaries().get(i).setAnswerBoolean(RandomUtils.nextBoolean());
            tx.getQuestionaries().get(i).setAnswerNumber(RandomUtils.nextInt());
            tx.getQuestionaries().get(i).setQuestionNumber(RandomUtils.nextInt());
        }
        return tx;
    }

    public static LoyDiscountPositionEntity buildLoyDiscountPosition(LoyAdvActionInPurchaseEntity action) {
        LoyDiscountPositionEntity discountPositionEntity = new LoyDiscountPositionEntity();
        discountPositionEntity.setAdvAction(action);
        discountPositionEntity.setDiscountAmount(RandomUtils.nextLong()%100000);
        discountPositionEntity.setDiscountIdentifier(RandomStringUtils.randomAlphanumeric(16));
        discountPositionEntity.setDiscountPurchase(RandomUtils.nextBoolean());
        discountPositionEntity.setGoodCode(RandomStringUtils.randomAlphanumeric(16));
        discountPositionEntity.setPositionOrder(1);
        discountPositionEntity.setQnty(RandomUtils.nextLong());
        return discountPositionEntity;
    }

    public static LoyAdvActionInPurchaseEntity buildLoyAdvActionInPurchase() {
        LoyAdvActionInPurchaseEntity action = new LoyAdvActionInPurchaseEntity();
        action.setGuid(RandomUtils.nextLong());
        action.setActionName(RandomStringUtils.random(16));
        action.setActionType(ActionType.BONUS_CFT);
        action.setApplyMode(ApplyMode.AUTOMATIC);
        return action;
    }

    public static LoyActionsProvider generateLoyActionsProvider(List<AdvertisingActionEntity> actions) {
        return new LoyActionsProvider() {
            @Override
            public void onDiscountCalculationStarted(Purchase startPurchase, Map<String, String> purchaseExtData) {
            }

            @Override
            public void onPurchaseProcessingFinished() {
            }

            @Override
            public Collection<AdvertisingActionEntity> getActions(PurchaseEntity receipt) {
                return new ArrayList<>(actions);
            }

            @Override
            public Collection<AdvertisingActionEntity> getActions(Purchase purchase) {
                return new ArrayList<>(actions);
            }
        };
    }

}
