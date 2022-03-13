package ru.crystals.pos.fiscalprinter.retailforce.api.dto.responses;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum EndOfDayPositionType {

    @JsonProperty("[3] = Booking")
    BOOKING,

    @JsonEnumDefaultValue()
    UNKNOWN;
}

