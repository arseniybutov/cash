package ru.crystals.pos.bank.odengiqr.api.dto.response.status;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Статус транзакции
 */
public enum Status {

    /**
     * В процессе оплаты
     */
    @JsonProperty("processing")
    PROCESSING,

    /**
     * Закончилось время жизни счета date_life или плательщик отменил
     */
    @JsonProperty("canceled")
    CANCELED,

    /**
     * Платеж зачислен/оплачен
     */
    @JsonProperty("approved")
    APPROVED,

    /**
     * Неизвестный статус
     */
    @JsonEnumDefaultValue
    UNKNOWN
}
