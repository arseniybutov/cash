package ru.crystals.pos.bank.raiffeisensbp.api.status;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Информация о статусе платежа
 */
public enum PaymentStatus {

    /**
     * Оплата совершена
     */
    @JsonProperty("SUCCESS")
    SUCCESS,

    /**
     * Оплата отклонена
     */
    @JsonProperty("DECLINED")
    DECLINED,

    /**
     * Не оплачено
     */
    @JsonProperty("NO_INFO")
    NO_INFO,

    /**
     * В процессе оплаты
     */
    @JsonProperty("IN_PROGRESS")
    IN_PROGRESS,

    @JsonEnumDefaultValue
    UNKNOWN

}
