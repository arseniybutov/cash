package ru.crystals.pos.bank.odengiqr.api.dto.response.create;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.bank.odengiqr.api.dto.Data;

public class CreateInvoiceRs extends Data {

    /**
     * Уникальный номер заказа в магазине
     */
    @JsonProperty("order_id")
    private String orderId;

    /**
     * Уникальный идентификатор выставленного счета (12 символов)
     */
    @JsonProperty("invoice_id")
    private String invoiceId;

    /**
     * URL картинки QR кода
     */
    @JsonProperty("qr")
    private String qr;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getQr() {
        return qr;
    }

    public void setQr(String qr) {
        this.qr = qr;
    }
}
