package ru.crystals.pos.bank.raiffeisensbp.api.response;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Код сообщения
 */
public enum ResponseStatusCode {

    /**
     * Успешно
     */
    @JsonProperty("SUCCESS")
    SUCCESS,

    /**
     * Ошибка смены статуса с EXPIRED на CANCELLED
     */
    @JsonProperty("ERROR.ERROR_WRONG_QR_STATUS")
    WRONG_QR,

    /**
     * Любое другое значение
     */
    @JsonEnumDefaultValue
    @JsonProperty("UNKNOWN")
    UNKNOWN;

}
