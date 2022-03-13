package ru.crystals.pos.bank.odengiqr.api.dto.request.status;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.bank.odengiqr.api.dto.Data;

public class StatusPaymentRq extends Data {

    /**
     * Уникальный идентификатор выставленного счета
     */
    @JsonProperty("invoice_id")
    private String invoiceId;

    /**
     * Уникальный номер заказа на стороне торговца
     */
    @JsonProperty("order_id")
    private String orderId;

    /**
     * Получить список платежей по указанному маркеру, работает только для долговременных QR long_term
     * (если маркер не указан или 0, то вернет статус платежа, если указать 1, то вернет список оплаченных платежей с маркером 1)
     */
    @JsonProperty("mark")
    private String mark;

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }
}
