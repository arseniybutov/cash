package ru.crystals.pos.bank.gazpromsbp.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProcessRefundResponse {

    /**
     * Код ответа на запрос
     */
    private String code;

    /**
     * Описание кода ответа на запрос
     */
    private String message;

    /**
     * id операции в система банка
     */
    private String transactionId;

    @JsonCreator
    public ProcessRefundResponse(@JsonProperty("code") String code,
                                 @JsonProperty("message") String message,
                                 @JsonProperty("transactionId") String transactionId) {
        this.code = code;
        this.message = message;
        this.transactionId = transactionId;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getTransactionId() {
        return transactionId;
    }
}
