package ru.crystals.pos.bank.gazpromsbp.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentInfoData {

    /**
     * Идентификатор QR кода, запрос по которому проводился
     */
    private String qrcId;

    /**
     * Код ответа на запрос статуса QR кода
     */
    private String code;

    /**
     * Описание кода ответа на запрос
     */
    private String message;

    /**
     * Статус операции, инициированной Dynamic QR кодом
     */
    private PaymentStatus status;

    /**
     * Id операции в системе банка
     */
    private String trxId;

    @JsonCreator
    public PaymentInfoData(@JsonProperty("qrcId") String qrcId,
                           @JsonProperty("code") String code,
                           @JsonProperty("message") String message,
                           @JsonProperty("status") PaymentStatus status,
                           @JsonProperty("trxId") String trxId) {
        this.qrcId = qrcId;
        this.code = code;
        this.message = message;
        this.status = status;
        this.trxId = trxId;
    }

    public String getQrcId() {
        return qrcId;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getTrxId() {
        return trxId;
    }
}
