package ru.crystals.pos.bank.gazpromsbp.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RegistrationQRResponse {

    /**
     * Код ответа на запрос
     */
    private String code;

    /**
     * Описание кода ответа на запрос
     */
    private String message;

    /**
     * Id операции со тсороны банка
     */
    private String transactionId;

    /**
     * Информация о QR
     */
    private QRData data;

    @JsonCreator
    public RegistrationQRResponse(@JsonProperty("code") String code,
                                  @JsonProperty("message") String message,
                                  @JsonProperty("transactionId") String transactionId,
                                  @JsonProperty("data") QRData data) {
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

    public QRData getData() {
        return data;
    }
}
