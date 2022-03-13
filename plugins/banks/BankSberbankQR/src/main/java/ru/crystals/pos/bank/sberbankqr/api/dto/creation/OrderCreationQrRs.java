package ru.crystals.pos.bank.sberbankqr.api.dto.creation;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.bank.sberbankqr.api.dto.OrderState;
import ru.crystals.pos.bank.sberbankqr.api.dto.SberbankResponse;

import java.time.ZonedDateTime;
import java.util.Objects;

public class OrderCreationQrRs {

    private Status status;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public static class Status implements SberbankResponse {

        /**
         * "Уникальный идентификатор запроса
         */
        @JsonProperty("rq_uid")
        private String rqUID;

        /**
         * Дата/время формирования запроса
         */
        @JsonProperty("rq_tm")
        private ZonedDateTime rqTm;

        /**
         * Номер заказа в CRM Клиента
         */
        @JsonProperty("order_number")
        private String orderNumber;

        /**
         * ID заказа
         */
        @JsonProperty("order_id")
        private String orderId;

        /**
         * Статус заказа
         */
        @JsonProperty("order_state")
        private OrderState orderState;

        /**
         * Ссылка на считывание QR code
         */
        @JsonProperty("order_form_url")
        private String orderFormUrl;

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

        public String getOrderNumber() {
            return orderNumber;
        }

        public void setOrderNumber(String orderNumber) {
            this.orderNumber = orderNumber;
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

        public String getOrderFormUrl() {
            return orderFormUrl;
        }

        public void setOrderFormUrl(String orderFormUrl) {
            this.orderFormUrl = orderFormUrl;
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
                    Objects.equals(orderNumber, status.orderNumber) &&
                    Objects.equals(orderId, status.orderId) &&
                    orderState == status.orderState &&
                    Objects.equals(orderFormUrl, status.orderFormUrl) &&
                    Objects.equals(errorCode, status.errorCode) &&
                    Objects.equals(errorDescription, status.errorDescription);
        }

        @Override
        public int hashCode() {
            return Objects.hash(rqUID, rqTm, orderNumber, orderId, orderState, orderFormUrl, errorCode, errorDescription);
        }

        @Override
        public String toString() {
            return "Status{" +
                    "rqUID='" + rqUID + '\'' +
                    ", rqTm=" + rqTm +
                    ", orderNumber='" + orderNumber + '\'' +
                    ", orderId='" + orderId + '\'' +
                    ", orderState=" + orderState +
                    ", orderFormUrl='" + orderFormUrl + '\'' +
                    ", errorCode='" + errorCode + '\'' +
                    ", errorDescription='" + errorDescription + '\'' +
                    '}';
        }
    }
}
