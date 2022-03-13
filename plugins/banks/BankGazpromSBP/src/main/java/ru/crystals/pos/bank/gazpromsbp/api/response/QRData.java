package ru.crystals.pos.bank.gazpromsbp.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QRData {

    /**
     * Идентификатор зарегистрированного QR кода в СБП
     */
    private String qrcId;

    /**
     * Payload зарегистрированного QR кода в СБП
     */
    private String payload;

    /**
     * Статус регистрации QR кода в СБП
     * По документации может иметь только одно значение: CREATED
     */
    private RegistrationQRStatus status;

    public QRData(@JsonProperty("qrcId") String qrcId,
                  @JsonProperty("payload") String payload,
                  @JsonProperty("status") RegistrationQRStatus status) {
        this.qrcId = qrcId;
        this.payload = payload;
        this.status = status;
    }

    public String getQrcId() {
        return qrcId;
    }

    public String getPayload() {
        return payload;
    }

    public RegistrationQRStatus getStatus() {
        return status;
    }
}
