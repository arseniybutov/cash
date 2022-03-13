package ru.crystals.pos.bank.raiffeisensbp.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CancelQrResponse implements ResponseWithMessage {

    /**
     * Код сообщения
     */
    private final ResponseStatusCode code;

    /**
     * Текст сообщения об ошибке
     */
    private final String message;

    @JsonCreator
    public CancelQrResponse(@JsonProperty("code") ResponseStatusCode code,
                            @JsonProperty("message") String message) {
        this.code = code;
        this.message = message;
    }

    public ResponseStatusCode getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
