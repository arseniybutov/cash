package ru.crystals.pos.bank.raiffeisensbp.api.response;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Ответ на регистрацию QR кода
 */
public class QRInfoResponse implements ResponseWithMessage {

    /**
     * Код сообщения
     */
    private final ResponseStatusCode code;

    /**
     * Текст сообщения об ошибке
     */
    private final String message;

    /**
     * Идентификатор зарегистрированного QRС в СБП
     */
    private String qrId;

    /**
     * Данные для самостоятельной генерации изображения зарегистрированного QR-кода в СБП.
     */
    private String payload;

    /**
     * URL с изображением зарегистрированного QR-кода в СБП
     */
    private String qrUrl;

    @JsonCreator
    public QRInfoResponse(@JsonProperty("code") ResponseStatusCode code,
                          @JsonProperty("message") String message,
                          @JsonProperty("qrId") String qrId,
                          @JsonProperty("payload") String payload,
                          @JsonProperty("qrUrl") String qrUrl) {
        this.code = code;
        this.message = message;
        this.qrId = qrId;
        this.payload = payload;
        this.qrUrl = qrUrl;
    }

    @Override
    public ResponseStatusCode getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String getQrId() {
        return qrId;
    }

    public String getPayload() {
        return payload;
    }

    public String getQrUrl() {
        return qrUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QRInfoResponse that = (QRInfoResponse) o;
        return code == that.code &&
                Objects.equals(message, that.message) &&
                Objects.equals(qrId, that.qrId) &&
                Objects.equals(payload, that.payload) &&
                Objects.equals(qrUrl, that.qrUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message, qrId, payload, qrUrl);
    }

    @Override
    public String toString() {
        return "QRInfoResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", qrId='" + qrId + '\'' +
                ", payload='" + payload + '\'' +
                ", qrUrl='" + qrUrl + '\'' +
                '}';
    }
}
