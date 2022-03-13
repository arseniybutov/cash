package ru.crystals.pos.bank.opensbp.api.status;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum PaymentOperationStatus {
    /**
     * NOT_STARTED, операции по QR коду не существует
     */
    @JsonProperty("NTST")
    NTST,

    /**
     * RECEIVED операция в обработке
     */
    @JsonProperty("RCVD")
    RCVD,

    /**
     * IN_PROGRESS операция в обработке
     */
    @JsonProperty("ACTC")
    ACTC,

    /**
     * ACCEPTED операция завершена успешно
     */
    @JsonProperty("ACWP")
    ACWP,

    /**
     * REJECTED операция отклонена
     */
    @JsonProperty("RJCT")
    RJCT,
}
