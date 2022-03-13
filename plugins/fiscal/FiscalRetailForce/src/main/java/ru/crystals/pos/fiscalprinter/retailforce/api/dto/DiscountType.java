package ru.crystals.pos.fiscalprinter.retailforce.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DiscountType {

    @JsonProperty("[0] = Allowance")
    ALLOWANCE,

    @JsonProperty("[1] = Discount")
    DISCOUNT;
}

