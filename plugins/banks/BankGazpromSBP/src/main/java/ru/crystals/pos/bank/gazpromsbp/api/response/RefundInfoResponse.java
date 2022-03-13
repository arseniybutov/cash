package ru.crystals.pos.bank.gazpromsbp.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RefundInfoResponse {


    /**
     * Код ответа
     */
    private String code;

    /**
     * Описание кода ответа
     */
    private String message;

    /**
     * ID операции возврата в СБПGate Master System
     */
    private String transactionId;

    /**
     * Тип операции
     */
    private String type;

    /**
     * Статус операции
     */
    private RefundStatus status;

    public RefundInfoResponse(@JsonProperty("code") String code,
                              @JsonProperty("message") String message,
                              @JsonProperty("transactionId") String transactionId,
                              @JsonProperty("type") String type,
                              @JsonProperty("status") RefundStatus status) {
        this.code = code;
        this.message = message;
        this.transactionId = transactionId;
        this.type = type;
        this.status = status;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public RefundStatus getStatus() {
        return status;
    }

    public void setStatus(RefundStatus status) {
        this.status = status;
    }
}
