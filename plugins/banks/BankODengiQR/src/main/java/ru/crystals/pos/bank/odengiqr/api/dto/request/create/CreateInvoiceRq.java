package ru.crystals.pos.bank.odengiqr.api.dto.request.create;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.bank.odengiqr.api.dto.Data;

public class CreateInvoiceRq extends Data {

    /**
     * Уникальный номер заказа в магазине
     */
    @JsonProperty("order_id")
    private String orderId;

    /**
     * Описание запроса
     */
    @JsonProperty("desc")
    private String desc;

    /**
     * Сумма платежа в копейках (если 0, тогда плательщик сам вводит сумму в приложении)
     */
    @JsonProperty("amount")
    private int amount;

    /**
     * Валюта платежа (UAH, EUR, RUR и т.д.)
     */
    @JsonProperty("currency")
    private String currency;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
