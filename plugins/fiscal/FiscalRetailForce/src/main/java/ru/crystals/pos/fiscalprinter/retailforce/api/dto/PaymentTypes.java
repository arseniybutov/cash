package ru.crystals.pos.fiscalprinter.retailforce.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum PaymentTypes {
    @JsonProperty("cash")
    CASH,
    @JsonProperty("ecCard")
    EC_CARD,
    @JsonProperty("creditCard")
    CREDIT_CARD,
    @JsonProperty("singlePurposeVoucher")
    SINGLE_PURPOSE_VOUCHER,
    @JsonProperty("multiPurposeVoucher")
    MULTI_PURPOSE_VOUCHER,
    @JsonProperty("paymentProvider")
    PAYMENT_PROVIDER,
    @JsonProperty("deposit")
    DEPOSIT,
    @JsonProperty("noCash")
    NO_CASH,
    @JsonProperty("none")
    NONE;
}
