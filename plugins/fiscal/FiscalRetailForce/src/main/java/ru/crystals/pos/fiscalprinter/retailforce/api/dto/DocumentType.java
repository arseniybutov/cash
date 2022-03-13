package ru.crystals.pos.fiscalprinter.retailforce.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DocumentType {

    @JsonProperty("[0] = Receipt")
    RECEIPT,

    @JsonProperty("[1] = Invoice")
    INVOICE,

    @JsonProperty("[2] = DeliveryNote")
    DELIVERY_NOTE,

    @JsonProperty("[10] = PayOut")
    PAYOUT,

    @JsonProperty("[11] = PayIn")
    PAYIN,

    @JsonProperty("[80] = TableOrder")
    TABLE_ORDER,

    @JsonProperty("[99] = EndOfDay")
    END_OF_DAY,

    @JsonProperty("[1000] = NullReceipt")
    NULL_RECEIPT

}

