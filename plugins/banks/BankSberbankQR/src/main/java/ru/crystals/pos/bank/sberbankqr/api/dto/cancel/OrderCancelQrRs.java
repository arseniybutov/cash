package ru.crystals.pos.bank.sberbankqr.api.dto.cancel;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.bank.sberbankqr.api.dto.OrderState;
import ru.crystals.pos.bank.sberbankqr.api.dto.SberbankResponse;

import java.time.ZonedDateTime;
import java.util.Objects;

public class OrderCancelQrRs {

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
         * Номер (ID) заказа в ППРБ.Карты (Сбербанк)
         */
        @JsonProperty("order_id")
        private String orderId;

        /**
         * Статус заказа
         */
        @JsonProperty("order_status")
        private OrderState orderState;

        /**
         * Идентификатор операции в АС ППРБ.Карты (Сбербанк)
         */
        @JsonProperty("operation_id")
        private String operationId;

        /**
         * ДатаВремя совершения операции
         */
        @JsonProperty("operation_date_time")
        private ZonedDateTime operationDateTime;

        /**
         * Тип операции
         */
        @JsonProperty("operation_type")
        private String operationType;

        /**
         * Сумма операции в минмальных единицах валюты (копейках)
         */
        @JsonProperty("operation_sum")
        private int operationSum;

        /**
         * Валюта операции, цифровой код по ISO 4217
         */
        @JsonProperty("operation_currency")
        private String operationCurrency;

        /**
         * Код авторизации
         */
        @JsonProperty("auth_code")
        private String authCode;

        /**
         * RRN операции
         */
        @JsonProperty("rrn")
        private String rrn;

        /**
         * Уникальный идентификатор терминала
         */
        @JsonProperty("tid")
        private String terminalId;

        /**
         * IdQR устройства, на котором сформирован заказ
         */
        @JsonProperty("id_qr")
        private String idQR;

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

        public String getOperationId() {
            return operationId;
        }

        public void setOperationId(String operationId) {
            this.operationId = operationId;
        }

        public ZonedDateTime getOperationDateTime() {
            return operationDateTime;
        }

        public void setOperationDateTime(ZonedDateTime operationDateTime) {
            this.operationDateTime = operationDateTime;
        }

        public String getOperationType() {
            return operationType;
        }

        public void setOperationType(String operationType) {
            this.operationType = operationType;
        }

        public int getOperationSum() {
            return operationSum;
        }

        public void setOperationSum(int operationSum) {
            this.operationSum = operationSum;
        }

        public String getOperationCurrency() {
            return operationCurrency;
        }

        public void setOperationCurrency(String operationCurrency) {
            this.operationCurrency = operationCurrency;
        }

        public String getAuthCode() {
            return authCode;
        }

        public void setAuthCode(String authCode) {
            this.authCode = authCode;
        }

        public String getRrn() {
            return rrn;
        }

        public void setRrn(String rrn) {
            this.rrn = rrn;
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
            return operationSum == status.operationSum &&
                    Objects.equals(rqUID, status.rqUID) &&
                    Objects.equals(rqTm, status.rqTm) &&
                    Objects.equals(orderId, status.orderId) &&
                    orderState == status.orderState &&
                    Objects.equals(operationId, status.operationId) &&
                    Objects.equals(operationDateTime, status.operationDateTime) &&
                    Objects.equals(operationType, status.operationType) &&
                    Objects.equals(operationCurrency, status.operationCurrency) &&
                    Objects.equals(authCode, status.authCode) &&
                    Objects.equals(rrn, status.rrn) &&
                    Objects.equals(terminalId, status.terminalId) &&
                    Objects.equals(idQR, status.idQR) &&
                    Objects.equals(errorCode, status.errorCode) &&
                    Objects.equals(errorDescription, status.errorDescription);
        }

        @Override
        public int hashCode() {
            return Objects.hash(rqUID, rqTm, orderId, orderState, operationId, operationDateTime, operationType, operationSum,
                    operationCurrency, authCode, rrn, terminalId, idQR, errorCode, errorDescription);
        }

        @Override
        public String toString() {
            return "Status{" +
                    "rqUID='" + rqUID + '\'' +
                    ", rqTm=" + rqTm +
                    ", orderId='" + orderId + '\'' +
                    ", orderState=" + orderState +
                    ", operationId='" + operationId + '\'' +
                    ", operationDateTime=" + operationDateTime +
                    ", operationType='" + operationType + '\'' +
                    ", operationSum=" + operationSum +
                    ", operationCurrency='" + operationCurrency + '\'' +
                    ", authCode='" + authCode + '\'' +
                    ", rrn='" + rrn + '\'' +
                    ", terminalId='" + terminalId + '\'' +
                    ", idQR='" + idQR + '\'' +
                    ", errorCode='" + errorCode + '\'' +
                    ", errorDescription='" + errorDescription + '\'' +
                    '}';
        }
    }
}
