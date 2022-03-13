package ru.crystals.pos.bank.odengiqr.api.dto.request.cancel;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.bank.odengiqr.api.dto.Data;

public class InvoiceCancelRq extends Data {

    /**
     * Уникальный идентификатор выставленного счета
     */
    @JsonProperty("invoice_id")
    private String invoiceId;

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }
}
