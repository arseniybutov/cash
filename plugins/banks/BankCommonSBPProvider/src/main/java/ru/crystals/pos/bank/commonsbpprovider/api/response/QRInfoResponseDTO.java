package ru.crystals.pos.bank.commonsbpprovider.api.response;


import org.springframework.http.HttpStatus;
import ru.crystals.pos.bank.commonsbpprovider.api.status.Status;

import java.util.Objects;

public class QRInfoResponseDTO {
    /**
     * Id qr/платежа в системе банка
     */
    private String qrId;

    /**
     * Информация о QR коде
     */
    private String payload;

    /**
     * Статус запроса
     */
    private Status status;


    public QRInfoResponseDTO(String qrId, String payload, HttpStatus status) {
        this.payload = payload;
        this.qrId = qrId;
        if (status == HttpStatus.OK || status == HttpStatus.CREATED) {
            this.status = Status.SUCCESS;
        } else {
            this.status = Status.UNKNOWN;
        }
    }

    public QRInfoResponseDTO(String qrId, String payload, Status status) {
        this.payload = payload;
        this.qrId = qrId;
        this.status = status;
    }

    public String getQrId() {
        return qrId;
    }

    public String getPayload() {
        return payload;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setQrId(String qrId) {
        this.qrId = qrId;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QRInfoResponseDTO that = (QRInfoResponseDTO) o;
        return Objects.equals(qrId, that.qrId) && Objects.equals(payload, that.payload) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(qrId, payload, status);
    }
}
