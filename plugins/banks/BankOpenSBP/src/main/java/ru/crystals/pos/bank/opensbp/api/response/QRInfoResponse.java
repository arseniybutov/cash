package ru.crystals.pos.bank.opensbp.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;

import java.util.Objects;

/**
 * Ответ на регистрацию QR кода
 */
public class QRInfoResponse {

    /**
     * Идентификатор зарегистрированного QRС в СБП
     */
    private String qrId;

    /**
     * Данные для самостоятельной генерации изображения зарегистрированного QR-кода в СБП.
     */
    private String payload;

    /**
     * Http-статус ответа
     */
    private HttpStatus httpStatus;


    @JsonCreator
    public QRInfoResponse(@JsonProperty("qrcId") String qrId,
                          @JsonProperty("payload") String payload) {
        this.qrId = qrId;
        this.payload = payload;
    }


    public String getQrId() {
        return qrId;
    }

    public String getPayload() {
        return payload;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setQrId(String qrId) {
        this.qrId = qrId;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
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
        return Objects.equals(qrId, that.qrId) &&
                Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(qrId, payload);
    }

    @Override
    public String toString() {
        return "QRInfoResponse{" +
                ", qrId='" + qrId + '\'' +
                ", payload='" + payload +
                '}';
    }
}
