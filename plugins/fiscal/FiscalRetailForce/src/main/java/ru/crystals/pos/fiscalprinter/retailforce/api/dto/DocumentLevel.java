package ru.crystals.pos.fiscalprinter.retailforce.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DocumentLevel {

    @JsonProperty("header")
    HEADER,

    @JsonProperty("position")
    POSITION,

    @JsonProperty("payment")
    PAYMENT;

}

