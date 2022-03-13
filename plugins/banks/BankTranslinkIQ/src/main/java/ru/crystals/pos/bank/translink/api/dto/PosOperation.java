package ru.crystals.pos.bank.translink.api.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PosOperation {

    NOOPERATION(0),

    AUTHORIZE(1),

    PREAUTHORIZE(2),

    CREDIT(3),

    INQUIRY(4),

    CARDREAD(5),

    MANUALENTRY(6),
    ;

    private final int code;

    PosOperation(int code) {
        this.code = code;
    }

    @JsonValue
    public int getCode() {
        return code;
    }
}
