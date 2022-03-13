package ru.crystals.loyal;

import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.discount.processing.entity.FeedbackTime;
import ru.crystals.discount.processing.entity.LoyExtProviderFeedback;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.pos.InternalCashPoolExecutor;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.loyal.cash.persistence.LoyFeedbackDao;
import ru.crystals.pos.service.ExtServiceSettings;
import ru.crystals.pos.service.ExternalService;

/**
 * Внешний потребитель чеков (Лояльность)
 * @author s.pavlikhin
 */
public abstract class ExternalLoyaltyPurchaseConsumer<T extends ExtServiceSettings> extends ExternalService<T> {

    @Autowired
    private InternalCashPoolExecutor executor;

    @Autowired
    private LoyFeedbackDao loyFeedbackDao;

    /**
     * Обработать чек
     *
     * @param purchaseEntity чек
     * @param loyTransactionEntity транзакция лояльности
     * @param feedbackProviderId Идентификатор обработчика фибдеков
     */
    public void consume(PurchaseEntity purchaseEntity, LoyTransactionEntity loyTransactionEntity, String feedbackProviderId) {
        consume(null, purchaseEntity, loyTransactionEntity, feedbackProviderId);
    }

    /**
     * Обработать чек
     *
     * @param existFeedback существующий фидбек
     * @param purchase чек
     * @param loyTransaction транзакция лояльности
     * @param feedbackProviderId Идентификатор обработчика фибдеков
     */
    public void consume(LoyExtProviderFeedback existFeedback, PurchaseEntity purchase, LoyTransactionEntity loyTransaction, String feedbackProviderId) {
        if (!this.isEnabled()) {
            getLog().debug("consume: Service is disabled");
            return;
        }

        if (purchase == null || loyTransaction == null) {
            return;
        }


        if (purchase.getMainPurchase() != null) {
            getLog().debug("consume: purchase was divided");
            purchase = purchase.getMainPurchase();
            loyTransaction = loyTransaction.getMainTransaction();
        }

        try {
            // Сформируем запрос
            String request = createRequestString(existFeedback, purchase, loyTransaction);

            if (request == null) {
                return;
            }

            planExecute(existFeedback, purchase, feedbackProviderId, request);

        } catch (Exception e) {
            getLog().error("Couldn't consume purchase. Purchase {}", purchase, e);
        }

    }

    protected void planExecute(LoyExtProviderFeedback existFeedback, PurchaseEntity purchase, String feedbackProviderId, String request) {
        getExecutor().submit(() -> {
            try {
                sendRequest(request);
                getLog().debug("planExecute: request was successfully sent");
                // Отправка прошла успешно, можно дропать фидбек из базы
                if (existFeedback != null) {
                    getLoyFeedbackDao().remove(existFeedback);
                }
            } catch (ExternalLoyaltyConsumerNetworkException e) {
                if (existFeedback == null) {
                    LoyExtProviderFeedback feedback = new LoyExtProviderFeedback();
                    feedback.setPayload(request);
                    feedback.setProviderId(feedbackProviderId);
                    feedback.setFeedbackTime(FeedbackTime.AS_SOON_AS_POSSIBLE);
                    if (purchase.getShift() != null) {
                        feedback.setShopNumber(purchase.getShift().getShopIndex());
                        feedback.setCashNumber(purchase.getShift().getCashNum());
                        feedback.setShiftNumber(purchase.getShift().getNumShift());
                    }
                    feedback.setDocNumber(purchase.getNumber());
                    feedback.setInn(purchase.getInn());
                    feedback.setDateCreate(purchase.getDateCreate());
                    feedback.setProcessingName(getProviderName());
                    getLoyFeedbackDao().saveOrUpdate(feedback);
                }
            }
        });
    }

    private String createRequestString(
            LoyExtProviderFeedback existFeedback,
            PurchaseEntity purchase,
            LoyTransactionEntity loyTransaction) {

        if (existFeedback != null) {
            if (!existFeedback.getProcessingName().equals(getProviderName())) {
                // если не этот сервис ответственнен за отправку этого фидбека
                return null;
            }
            getLog().debug("createRequestString: took request string from feedback");
            return existFeedback.getPayload();
        } else {
            return prepareRequest(purchase, loyTransaction);
        }

    }

    /**
     * Подготовить запрос
     * @param purchase чек
     * @param loyTransaction транзакция лояльности
     * @return Сериализованный запрос
     */
    protected abstract String prepareRequest(PurchaseEntity purchase, LoyTransactionEntity loyTransaction);

    /**
     * Отослать запрос во внешнюю систему
     * @param request запрос
     * @throws ExternalLoyaltyConsumerNetworkException если есть ошибка соединения
     */
    protected abstract void sendRequest(String request) throws ExternalLoyaltyConsumerNetworkException;


    private InternalCashPoolExecutor getExecutor() {
        return executor;
    }

    private LoyFeedbackDao getLoyFeedbackDao() {
        return loyFeedbackDao;
    }

}
