package ru.crystals.pos.bank.gazpromsbp.api.response;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.bank.commonsbpprovider.api.status.Status;

public enum PaymentStatus {

    /**
     * NOT_STARTED: операции по QR коду не существует
     */
    @JsonProperty("NOT_STARTED")
    NOT_STARTED(Status.NOT_STARTED),

    /**
     * RECEIVED: операция в обработке
     */
    @JsonProperty("RECEIVED")
    RECEIVED(Status.PROCESSING),

    /**
     * IN_PROGRESS: операция в обработке
     */
    @JsonProperty("IN_PROGRESS")
    IN_PROGRESS(Status.PROCESSING),

    /**
     * ACCEPTED: операция завершена успешно
     */
    @JsonProperty("ACCEPTED")
    ACCEPTED(Status.SUCCESS),

    /**
     * REJECTED: операция отклонена
     */
    @JsonProperty("REJECTED")
    REJECTED(Status.REJECTED),

    /**
     * Любое другое значение
     */
    @JsonEnumDefaultValue
    @JsonProperty("UNKNOWN")
    UNKNOWN(Status.UNKNOWN);

    private final Status commonStatus;

    PaymentStatus(Status commonStatus) {
        this.commonStatus = commonStatus;
    }

    public Status getCommonStatus() {
        return commonStatus;
    }
}
