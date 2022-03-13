package ru.crystals.pos.bank.commonsbpprovider.api.response;

import ru.crystals.pos.bank.commonsbpprovider.api.status.Status;

import java.time.ZonedDateTime;
import java.util.Objects;

public class RefundInfoResponseDTO {

    /**
     * Номер операции в платежной системе банка
     */
    private String operationId;

    /**
     * Информация о статусе запроса на возврат
     */
    private Status status;

    /**
     * Уникальный идентификатор операции в система продавца
     */
    private String id;

    /**
     * Данные о сумме возврата
     */
    private long amount;

    /**
     * Дата и время операции возврата
     */
    private ZonedDateTime operationTimestamp;

    public String getOperationId() {
        return operationId;
    }

    public Status getStatus() {
        return status;
    }

    public String getId() {
        return id;
    }

    public long getAmount() {
        return amount;
    }

    public ZonedDateTime getOperationTimestamp() {
        return operationTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RefundInfoResponseDTO that = (RefundInfoResponseDTO) o;
        return amount == that.amount && Objects.equals(operationId, that.operationId) && status == that.status && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operationId, status, id, amount);
    }

    public static RefundInfoResponseDTOBuilder builder() {
        return new RefundInfoResponseDTOBuilder();
    }


    public static class RefundInfoResponseDTOBuilder {
        private RefundInfoResponseDTO refundInfoResponseDTO;

        public RefundInfoResponseDTOBuilder() {
            refundInfoResponseDTO = new RefundInfoResponseDTO();
        }

        public RefundInfoResponseDTOBuilder setOperationId(String operationId) {
            refundInfoResponseDTO.operationId = operationId;
            return this;
        }

        public RefundInfoResponseDTOBuilder setStatus(Status status) {
            refundInfoResponseDTO.status = status;
            return this;
        }


        public RefundInfoResponseDTOBuilder setId(String id) {
            refundInfoResponseDTO.id = id;
            return this;
        }

        public RefundInfoResponseDTOBuilder setAmount(long amount) {
            refundInfoResponseDTO.amount = amount;
            return this;
        }

        public RefundInfoResponseDTOBuilder setOperationTimestamp(){
            refundInfoResponseDTO.operationTimestamp = ZonedDateTime.now();
            return this;
        }


        public RefundInfoResponseDTO build() {
            return refundInfoResponseDTO;
        }

    }
}
