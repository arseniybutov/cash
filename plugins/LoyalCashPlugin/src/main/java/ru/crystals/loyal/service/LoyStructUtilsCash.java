package ru.crystals.loyal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.cards.CardBonusBalance;
import ru.crystals.discount.processing.entity.LoySetApiPluginTransactionEntity;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.loyal.interfaces.LoyTransactionProviderCash;
import ru.crystals.pos.check.PurchaseCardsEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.SentToServerStatus;

import java.util.Date;
import java.util.Objects;

public class LoyStructUtilsCash {
    private static final Logger logger = LoggerFactory.getLogger(LoyStructUtilsCash.class);

    public static LoyTransactionEntity map(PurchaseEntity purchase, LoyTransactionEntity loyTx) {
        if (loyTx == null) {
            loyTx = new LoyTransactionEntity();
        }

        Date nowDate = new Date();

        if (purchase.getShift() != null) {
            loyTx.setCashNumber(purchase.getShift().getCashNum() == null ? -1 : purchase.getShift().getCashNum());
            loyTx.setShiftNumber(purchase.getShift().getNumShift() == null ? -1 : purchase.getShift().getNumShift());
            loyTx.setShopNumber(purchase.getShift().getShopIndex() == null ? -1 : purchase.getShift().getShopIndex());
        } else {
            loyTx.setCashNumber(-1);
            loyTx.setShiftNumber(-1);
            loyTx.setShopNumber(-1);
        }
        loyTx.setSentToServerStatus(SentToServerStatus.UNCOMMITED);
        loyTx.setSaleTime(purchase.getDateCommit() != null ? purchase.getDateCommit() : nowDate);
        loyTx.setInn(purchase.getInn());
        loyTx.setPurchaseNumber(purchase.getNumber() == null ? -1 : purchase.getNumber());
        loyTx.setTransactionTime(nowDate);
        loyTx.setOperationType(purchase.getOperationType());
        loyTx.setDiscountValueTotal(purchase.getDiscountValueTotal() != null ? purchase.getDiscountValueTotal() : 0L);
        loyTx.setPurchaseAmount(purchase.getCheckSumEnd());

        return loyTx;
    }

    /**
     * Добавление транзакции списания SetApi
     *
     * @param purchase       чек
     * @param purchaseCard   карта SetApi
     * @param cardBonusBalance  баланс
     * @param writeOffAmount сумма списания в бонусах (бонусокопейках)
     * @param txId           идентификатор транзакции
     */
    public static void appendSetApiTransaction(PurchaseEntity purchase, PurchaseCardsEntity purchaseCard, CardBonusBalance cardBonusBalance,
                                               LoyTransactionProviderCash loyTransactionProvider, long writeOffAmount, String txId) {
        if (loyTransactionProvider == null) {
            logger.error("Unable to append transaction: failed to acquire instance of \"{}\".", LoyTransactionProviderCash.class);
            return;
        }
        LoyTransactionEntity transactionEntity = loyTransactionProvider.findLoyTransaction(purchase);
        if (transactionEntity == null) {
            logger.error("No loyal transaction found for purchase {}", purchase);
            return;
        }
        LoySetApiPluginTransactionEntity transaction = loyTransactionProvider.createSetApiTransaction(purchaseCard,
                cardBonusBalance, writeOffAmount, txId);
        transaction.setTransaction(transactionEntity);
        transactionEntity.getSetApiLoyaltyTransactions().add(transaction);
        transactionEntity.getDiscountPositions().stream()
                .filter(dp -> dp.getAdvAction() != null && Objects.equals(dp.getAdvAction().getGuid(), transaction.getAdvertisingActionGuid()))
                .forEach(dp -> transactionEntity.getBonusPositions().add(LoyStructUtils.convertDiscountToBonusPosition(dp)));
        loyTransactionProvider.updateLoyTransaction(transactionEntity);
        logger.info("Transaction state of purchase {} updated.", purchase.getId());
    }
}
