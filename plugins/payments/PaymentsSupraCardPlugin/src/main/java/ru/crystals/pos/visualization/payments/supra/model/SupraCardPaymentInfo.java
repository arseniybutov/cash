package ru.crystals.pos.visualization.payments.supra.model;

import ru.crystals.pos.visualization.payments.common.DefaultPaymentInfo;

/**
 * Created by s.pavlikhin on 08.06.2017.
 */
public class SupraCardPaymentInfo extends DefaultPaymentInfo {

    /**
     * Номер карты
     */
    private String cardNumber;

    /**
     * Последние 4 цифры
     */
    private String lastFour;

    /**
     * Номер кассы
     */
    private int posNumber;

    /**
     * Сумма
     */
    private long amount;

    /**
     * Ошибка
     */
    private String errorMessage;


    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getLastFour() {
        return lastFour;
    }

    public void setLastFour(String lastFour) {
        this.lastFour = lastFour;
    }

    public int getPosNumber() {
        return posNumber;
    }

    public void setPosNumber(int posNumber) {
        this.posNumber = posNumber;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
