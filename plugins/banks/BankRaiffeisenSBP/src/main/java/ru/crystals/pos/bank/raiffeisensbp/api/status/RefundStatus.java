package ru.crystals.pos.bank.raiffeisensbp.api.status;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Информация о статусе возврата
 */
public enum RefundStatus {

    /**
     * В процессе возврата
     */
    @JsonProperty("IN_PROGRESS")
    IN_PROGRESS,

    /**
     * Возврат отклонен
     */
    @JsonProperty("DECLINED")
    DECLINED,

    /**
     * Возврат совершен
     */
    @JsonProperty("COMPLETED")
    COMPLETED,

    @JsonEnumDefaultValue
    UNKNOWN

}
