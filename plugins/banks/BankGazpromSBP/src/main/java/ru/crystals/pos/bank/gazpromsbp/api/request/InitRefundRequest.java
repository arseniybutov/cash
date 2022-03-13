package ru.crystals.pos.bank.gazpromsbp.api.request;

import com.fasterxml.jackson.annotation.JsonGetter;

public class InitRefundRequest {

    /**
     * ID операции в СБПGate Master System
     */
    private String transactionId;

    /**
     * Сумма возврата в копейках
     */
    private String amount;

    /**
     * Валюта операции
     */
    private String currency;

    public InitRefundRequest(String transactionId, String amount, String currency) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.currency = currency;
    }

    @JsonGetter
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @JsonGetter
    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    @JsonGetter
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

}
