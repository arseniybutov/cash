package ru.crystals.loyal.providers;

import ru.crystals.discount.processing.entity.LoyExtProviderFeedback;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.pos.check.PurchaseEntity;

/**
 * Описывает данные, которые нужно предоставить провайдеру лояльности для отправки фидбека в внешнюю систему.<br>
 * Под фидбеком понимаются какие-либо данные, которые провайдер не смог отправить вовремя, посему они проставились на отложенную отправку
 * и теперь касса будет делать периодические попытки отправить их.
 * @since 10.2.71.0
 */
public class FeedbackBundle {
    /**
     * Сам фидбек, который нужно отправить.
     */
    private LoyExtProviderFeedback feedback;
    /**
     * Чек, в рамках которого фидбек образовался.
     */
    private PurchaseEntity purchase;
    /**
     * Транзакция лояльности чека.
     */
    private LoyTransactionEntity loyTransaction;

    /**
     * Конструктор класса. Создаёт новый экземпляр класса {@link FeedbackBundle}.
     * @param feedback сам фидбек, который необходимо отправить. Обязательный аргумент.
     * @param purchase чек, в рамках которого фидбек образовался.
     * @param loyTransaction транзакция лояльности, которая образовалась в рамках чека.
     * @throws IllegalArgumentException если фидбек null.
     */
    public FeedbackBundle(LoyExtProviderFeedback feedback, PurchaseEntity purchase, LoyTransactionEntity loyTransaction) {
        if(feedback == null) {
            throw new IllegalArgumentException("Feedback cannot be null");
        }
        this.feedback = feedback;
        this.purchase = purchase;
        this.loyTransaction = loyTransaction;
    }

    /**
     * Возвращает фидбек, который следует отправить.
     * @return фидбек, который следует отправить.
     */
    public LoyExtProviderFeedback getFeedback() {
        return feedback;
    }

    /**
     * Устанавливает фидбек, который следует отправить.
     * @param feedback фидбек, который следует отправить.
     */
    public void setFeedback(LoyExtProviderFeedback feedback) {
        this.feedback = feedback;
    }

    /**
     * Возвращает чек, в рамках которого появился данный фидбек.
     * @return чек, в рамках которого появился данный фидбек или null, если такого нет.
     */
    public PurchaseEntity getPurchase() {
        return purchase;
    }

    /**
     * Устанавливает чек, в рамках которого появился данный фидбек.
     * @param purchase чек, в рамках которого появился данный фидбек или null, если такого нет.
     */
    public void setPurchase(PurchaseEntity purchase) {
        this.purchase = purchase;
    }

    /**
     * Возвращает транзакцию лояльности, которая появилась в рамках чека.
     * @return транзакция лояльности, которая появилась в рамках чека или null, если такой нет.
     */
    public LoyTransactionEntity getLoyTransaction() {
        return loyTransaction;
    }

    /**
     * Устанавливает транзакцию лояльности, которая появилась в рамках чека.
     * @param loyTransaction транзакция лояльности, которая появилась в рамках чека или null, если такой нет.
     */
    public void setLoyTransaction(LoyTransactionEntity loyTransaction) {
        this.loyTransaction = loyTransaction;
    }
}
