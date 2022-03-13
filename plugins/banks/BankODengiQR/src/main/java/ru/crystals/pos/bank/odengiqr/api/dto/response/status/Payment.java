package ru.crystals.pos.bank.odengiqr.api.dto.response.status;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class Payment {

    /**
     * Уникальный идентификатор транзакции
     */
    @JsonProperty("trans_id")
    private String transactionId;

    /**
     * Дата платежа
     */
    @JsonProperty("date_pay")
    private LocalDateTime datePay;

    /**
     * Сумма платежа в копейках
     */
    @JsonProperty("amount")
    private int amount;

    /**
     * Исходная сумма платежа, если amount изменился при пересчете коммисии
     */
    @JsonProperty("amount_old")
    private int amountOld;

    /**
     * Уникальный номер заказа на стороне торговца
     */
    @JsonProperty("order_id")
    private String orderId;

    /**
     * Статус транзакции (processing – в процессе оплаты; canceled – закончилось время жизни счета (date_life) или плательщик отменил;
     * approved – платеж зачислен/оплачен; waitingauthcomplete – деньги заблокированы на карте и ждут снятия или отмены платежа)
     */
    @JsonProperty
    private Status status;

    /**
     * Описание платежа
     */
    @JsonProperty("description")
    private String description;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public LocalDateTime getDatePay() {
        return datePay;
    }

    public void setDatePay(LocalDateTime datePay) {
        this.datePay = datePay;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getAmountOld() {
        return amountOld;
    }

    public void setAmountOld(int amountOld) {
        this.amountOld = amountOld;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
