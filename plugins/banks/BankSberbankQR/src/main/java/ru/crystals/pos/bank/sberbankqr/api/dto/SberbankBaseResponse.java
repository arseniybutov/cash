package ru.crystals.pos.bank.sberbankqr.api.dto;

import java.time.ZonedDateTime;

public interface SberbankBaseResponse {

    default String getAuthCode() {
        return null;
    }

    default String getOrderId() {
        return null;
    }

    default String getRrn() {
        return null;
    }

    String getOperationId();

    ZonedDateTime getRqTm();

}
