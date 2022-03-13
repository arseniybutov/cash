package ru.crystals.pos.bank.gazpromsbp.api.response;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.bank.commonsbpprovider.api.status.Status;

public enum RefundStatus {
    /**
     * Создан черновик операции
     */
    @JsonProperty("DRAFT")
    DRAFT(Status.PROCESSING),

    /**
     * Операция в процессе
     */
    @JsonProperty("IN_PROGRESS")
    IN_PROGRESS(Status.PROCESSING),

    /**
     * Операция завершена
     */
    @JsonProperty("PERFORMED")
    PERFORMED(Status.SUCCESS),

    /**
     * Операция отменена
     */
    @JsonProperty("CANCELLED")
    CANCELLED(Status.REJECTED),

    /**
     * Ошибка
     */
    @JsonProperty("ERROR")
    ERROR(Status.REJECTED),

    /**
     * Ошибка на этапе предпроверок
     */
    @JsonProperty("ERROR_CHECK")
    ERROR_CHECK(Status.REJECTED),

    /**
     * Операция не завершена (прервалась по какой-то причине)
     */
    @JsonProperty("INCOMPLETE")
    INCOMPLETE(Status.REJECTED),

    /**
     * Ошибка на этапе обработки операции
     */
    @JsonProperty("NOT_STARTED")
    ERROR_EXECUTE(Status.REJECTED),

    /**
     * Ошибка, требуется ручная обработка операции
     */
    @JsonProperty("ERROR_MANUAL_HANDLING")
    ERROR_MANUAL_HANDLING(Status.REJECTED),

    /**
     * Возврат в процессе
     */
    @JsonProperty("ROLLBACK_IN_PROGRESS")
    ROLLBACK_IN_PROGRESS(Status.PROCESSING),

    /**
     * Возврат завершен
     */
    @JsonProperty("ROLLBACK_PERFORMED")
    ROLLBACK_PERFORMED(Status.SUCCESS),

    /**
     * Любое другое значение
     */
    @JsonEnumDefaultValue
    UNKNOWN(Status.UNKNOWN);

    private final Status commonStatus;

    RefundStatus(Status commonStatus) {
        this.commonStatus = commonStatus;
    }

    public Status getCommonStatus() {
        return commonStatus;
    }
}
