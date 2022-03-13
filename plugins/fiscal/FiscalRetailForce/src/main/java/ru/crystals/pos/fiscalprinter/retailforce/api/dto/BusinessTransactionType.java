package ru.crystals.pos.fiscalprinter.retailforce.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum BusinessTransactionType {

    @JsonProperty("[0] = Revenue")
    REVENUE,

    @JsonProperty("[1] = Discount")
    DISCOUNT,

    @JsonProperty("[10] = PayOut")
    PAY_OUT,

    @JsonProperty("[11] = PayIn")
    PAY_IN,

    @JsonProperty("[90] = MoneyTransfer")
    MONEY_TRANSFER,

    @JsonProperty("[91] = CashDifference")
    CASH_DIFFERENCE;
}

