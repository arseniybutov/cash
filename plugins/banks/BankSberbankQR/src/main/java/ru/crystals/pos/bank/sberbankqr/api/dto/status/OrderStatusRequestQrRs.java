package ru.crystals.pos.bank.sberbankqr.api.dto.status;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.bank.sberbankqr.api.dto.OrderOperationParamType;
import ru.crystals.pos.bank.sberbankqr.api.dto.OrderState;
import ru.crystals.pos.bank.sberbankqr.api.dto.SberbankResponse;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

public class OrderStatusRequestQrRs {

    private Status status;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public static class Status implements SberbankResponse {

        /**
         * Уникальный идентификатор запроса
         */
        @JsonProperty("rq_uid")
        private String rqUID;

        /**
         * ДатаВремя формирования запроса
         */
        @JsonProperty("rq_tm")
        private ZonedDateTime rqTm;

        /**
         * Уникальный идентификатор Торговой точки
         */
        @JsonProperty("mid")
        private String merchantId;

        /**
         * Уникальный идентификатор терминала
         */
        @JsonProperty("tid")
        private String terminalId;

        /**
         * IdQR устройства, на котором выполнена операция
         */
        @JsonProperty("id_qr")
        private String idQR;

        /**
         * Номер (ID) заказа в ППРБ.Карты (Сбербанк)
         */
        @JsonProperty("order_id")
        private String orderId;

        /**
         * Статус заказа
         */
        @JsonProperty("order_state")
        private OrderState orderState;

        /**
         * Блок с перечнем операций, привязанных к данному заказу с детализацией по каждой операции
         */
        @JsonProperty("order_operation_params")
        private List<OrderOperationParamType> orderOperationParams;

        /**
         * Код выполнения запроса
         */
        @JsonProperty("error_code")
        private String errorCode;

        /**
         * Описание ошибки выполнения запроса
         */
        @JsonProperty("error_description")
        private String errorDescription;

        public String getRqUID() {
            return rqUID;
        }

        public void setRqUID(String rqUID) {
            this.rqUID = rqUID;
        }

        public ZonedDateTime getRqTm() {
            return rqTm;
        }

        public void setRqTm(ZonedDateTime rqTm) {
            this.rqTm = rqTm;
        }

        public String getMerchantId() {
            return merchantId;
        }

        public void setMerchantId(String merchantId) {
            this.merchantId = merchantId;
        }

        public String getTerminalId() {
            return terminalId;
        }

        public void setTerminalId(String terminalId) {
            this.terminalId = terminalId;
        }

        public String getIdQR() {
            return idQR;
        }

        public void setIdQR(String idQR) {
            this.idQR = idQR;
        }

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public OrderState getOrderState() {
            return orderState;
        }

        public void setOrderState(OrderState orderState) {
            this.orderState = orderState;
        }

        public List<OrderOperationParamType> getOrderOperationParams() {
            return orderOperationParams;
        }

        public void setOrderOperationParams(List<OrderOperationParamType> orderOperationParams) {
            this.orderOperationParams = orderOperationParams;
        }

        @Override
        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public String getErrorDescription() {
            return errorDescription;
        }

        public void setErrorDescription(String errorDescription) {
            this.errorDescription = errorDescription;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Status status = (Status) o;
            return Objects.equals(rqUID, status.rqUID) &&
                    Objects.equals(rqTm, status.rqTm) &&
                    Objects.equals(merchantId, status.merchantId) &&
                    Objects.equals(terminalId, status.terminalId) &&
                    Objects.equals(idQR, status.idQR) &&
                    Objects.equals(orderId, status.orderId) &&
                    orderState == status.orderState &&
                    Objects.equals(orderOperationParams, status.orderOperationParams) &&
                    Objects.equals(errorCode, status.errorCode) &&
                    Objects.equals(errorDescription, status.errorDescription);
        }

        @Override
        public int hashCode() {
            return Objects.hash(rqUID, rqTm, merchantId, terminalId, idQR, orderId, orderState, orderOperationParams,
                    errorCode, errorDescription);
        }

        @Override
        public String toString() {
            return "Status{" +
                    "rqUID='" + rqUID + '\'' +
                    ", rqTm=" + rqTm +
                    ", merchantId='" + merchantId + '\'' +
                    ", terminalId='" + terminalId + '\'' +
                    ", idQR='" + idQR + '\'' +
                    ", orderId='" + orderId + '\'' +
                    ", orderState=" + orderState +
                    ", orderOperationParams=" + orderOperationParams +
                    ", errorCode='" + errorCode + '\'' +
                    ", errorDescription='" + errorDescription + '\'' +
                    '}';
        }
    }
}
