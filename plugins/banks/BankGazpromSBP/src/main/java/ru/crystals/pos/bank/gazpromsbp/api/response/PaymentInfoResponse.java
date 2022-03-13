package ru.crystals.pos.bank.gazpromsbp.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PaymentInfoResponse {
    /**
     * Код ответа на запрос
     */
    private String code;

    /**
     * Описание кода ответа на запрос
     */
    private String message;

    /**
     * ID операции в СБПGate Master System
     */
    private String transactionId;

    /**
     * Информация по платежу
     */
    private List<PaymentInfoData> data;

    @JsonCreator
    public PaymentInfoResponse(@JsonProperty("code") String code,
                               @JsonProperty("message") String message,
                               @JsonProperty("transactionId") String transactionId,
                               @JsonProperty("data") List<PaymentInfoData> data) {
        this.code = code;
        this.message = message;
        this.transactionId = transactionId;
        this.data = data;
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

    public List<PaymentInfoData> getData() {
        return data;
    }
}
