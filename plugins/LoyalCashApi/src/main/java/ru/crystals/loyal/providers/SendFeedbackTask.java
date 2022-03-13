package ru.crystals.loyal.providers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.discount.processing.entity.LoyExtProviderFeedback;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.SentToServerStatus;
import ru.crystals.pos.check.ShiftEntity;
import ru.crystals.pos.loyal.Loyal;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.pos.techprocess.TechProcessStage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

/**
 * Таска по отправке фидбеков через провайдеры лояльности.
 */
class SendFeedbackTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(SendFeedbackTask.class);
    /**
     * Коллекция фидбеков, которые таске необходимо отправить в течение своего жизненного цикла.
     */
    private Collection<LoyExtProviderFeedback> feedbackCollection;
    /**
     * Провайдер, через который будут отправляться фидбеки.
     */
    private LoyProvider provider;

    private Loyal loyalService;
    private TechProcessInterface techProcess;

    /**
     * Конструктор класса. Создаёт новый экземпляр класса {@link SendFeedbackTask}
     * @param techProcess техпроцесс кассы, таска использует его для получения чеков и всего такого
     * @param loyalService сервис лояльности для получения транзакций лояльности
     * @param feedbacks коллекция фидбеков, которые таска должна отправить в течение своего жизненного цикла.
     * @param targetProvider целевой провайдер лояльности, через который будут отправляться фидбеки
     */
    SendFeedbackTask(TechProcessInterface techProcess, Loyal loyalService, Collection<LoyExtProviderFeedback> feedbacks, LoyProvider targetProvider) {
        this.feedbackCollection = feedbacks;
        this.provider = targetProvider;
        this.techProcess = techProcess;
        this.loyalService = loyalService;
    }

    @Override
    public void run() {
        if(feedbackCollection == null || feedbackCollection.isEmpty()) {
            return;
        }
        boolean isLoyTransactionRequiredForFeedback = provider.isLoyTransactionRequiredForStage(TechProcessStage.FEEDBACK);
        try {
            doSendFeedback(feedbackCollection, isLoyTransactionRequiredForFeedback);
        } catch (Exception ex) {
            logger.error("Failed to send feedback", ex);
        }
    }

    private void doSendFeedback(Collection<LoyExtProviderFeedback> feedbacks, boolean loyTransactionRequired) {
        List<FeedbackBundle> feedbackBundles = new ArrayList<>(feedbacks.size());
        for(LoyExtProviderFeedback feedback : feedbacks) {
            FeedbackBundle feedbackBundle = new FeedbackBundle(feedback, null, null);
            if(!loyTransactionRequired) {
                feedbackBundles.add(feedbackBundle);
                continue;
            }
            feedbackBundle.setPurchase(restorePurchase(feedback));
            if(feedbackBundle.getPurchase() != null) {
                LoyTransactionEntity loyTransaction = loyalService.findLoyTransaction(feedbackBundle.getPurchase());
                if (loyTransaction != null) {
                    if (isCommitted(loyTransaction)) {
                        if (feedbackBundle.getPurchase().isReturn()) {
                            loyTransaction.setSuperTransaction(
                                    loyalService.findLoyTransaction(feedbackBundle.getPurchase().getSuperPurchase()));
                        }
                        feedbackBundle.setLoyTransaction(loyTransaction);
                    } else {
                        logger.warn("Skipped not committed transaction: {}", loyTransaction);
                        continue;
                    }
                }
            }
            feedbackBundles.add(feedbackBundle);
        }
        provider.sendFeedback(feedbackBundles);
        for(FeedbackBundle b : feedbackBundles) {
            loyalService.updateAdvActions(b.getPurchase(), b.getLoyTransaction());
        }
    }

    private PurchaseEntity restorePurchase(LoyExtProviderFeedback feedback) {
        if (feedback.getReceiptId() != null) {
            long purchaseId = feedback.getReceiptId();
            return techProcess.getPurchaseByID(purchaseId);
        }
        PurchaseEntity purchase = new PurchaseEntity();
        purchase.setNumber(feedback.getDocNumber());
        purchase.setShift(new ShiftEntity());
        purchase.getShift().setNumShift(feedback.getShiftNumber());
        purchase.getShift().setCashNum(feedback.getCashNumber());
        purchase.getShift().setShopIndex(feedback.getShopNumber());
        purchase.setInn(feedback.getInn());
        purchase.setDateCreate(feedback.getDateCreate());
        purchase.setNumber(feedback.getDocNumber());
        return techProcess.getExistingPurchase(purchase);
    }

    private boolean isCommitted(LoyTransactionEntity loyTransaction) {
        return !EnumSet.of(SentToServerStatus.UNCOMMITED, SentToServerStatus.PENDING).contains(loyTransaction.getSentToServerStatus());
    }

}
