package ru.crystals.pos.bank.gazpromsbp.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BlockQRResponse {

    /**
     * Код ответа
     */
    private String code;

    /**
     * Описание кода ответа
     */
    private String message;

    /**
     * ID операции блокировки QR в СБПGate Master System
     */
    private String transactionId;

    @JsonCreator
    public BlockQRResponse(@JsonProperty("code") String code,
                           @JsonProperty("message") String message,
                           @JsonProperty("transactionId") String transactionId) {
        this.code = code;
        this.message = message;
        this.transactionId = transactionId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
