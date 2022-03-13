package ru.crystals.pos.fiscalprinter.retailforce.api.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DocumentPositionType {

    ITEM(0),
    SUB_ITEM(1),
    TEXT(2),
    BOOKING(3),
    TOTAL(10);

    private final int code;

    DocumentPositionType(int code) {
        this.code = code;
    }

    @JsonValue
    public int getCode() {
        return code;
    }
}

