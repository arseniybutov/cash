package ru.crystals.pos.bank.commonsbpprovider.api.response;

import ru.crystals.pos.bank.commonsbpprovider.api.status.Status;

import java.time.ZonedDateTime;
import java.util.Objects;

public class PaymentInfoResponseDTO {
    /**
     * Статус операции
     */
    private Status status;

    /**
     * Дополнительная информация об операции
     */
    private String message;

    /**
     * Id операции в системе банка
     */
    private String operationId;

    /**
     * Время проведения операции
     */
    private ZonedDateTime operationTimestamp;

    public PaymentInfoResponseDTO(Status status, String message, String operationId, ZonedDateTime operationTimestamp) {
        this.status = status;
        this.message = message;
        this.operationId = operationId;
        this.operationTimestamp = operationTimestamp;
    }

    public PaymentInfoResponseDTO() {
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getOperationId() {
        return operationId;
    }

    public ZonedDateTime getOperationTimestamp() {
        return operationTimestamp;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public void setOperationTimestamp(String operationTimestamp) {
        this.operationTimestamp = ZonedDateTime.parse(operationTimestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PaymentInfoResponseDTO that = (PaymentInfoResponseDTO) o;
        return status == that.status && Objects.equals(message, that.message) && Objects.equals(operationId, that.operationId) && Objects.equals(operationTimestamp,
                that.operationTimestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, message, operationId, operationTimestamp);
    }
}
