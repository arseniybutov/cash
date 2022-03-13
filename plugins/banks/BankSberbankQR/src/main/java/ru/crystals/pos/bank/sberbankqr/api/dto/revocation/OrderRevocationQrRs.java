package ru.crystals.pos.bank.sberbankqr.api.dto.revocation;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.bank.sberbankqr.api.dto.OrderState;
import ru.crystals.pos.bank.sberbankqr.api.dto.SberbankResponse;

import java.time.ZonedDateTime;
import java.util.Objects;

public class OrderRevocationQrRs {

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
        @JsonProperty("order_state")
        private OrderState orderState;

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
                    Objects.equals(orderId, status.orderId) &&
                    orderState == status.orderState &&
                    Objects.equals(errorCode, status.errorCode) &&
                    Objects.equals(errorDescription, status.errorDescription);
        }

        @Override
        public int hashCode() {
            return Objects.hash(rqUID, rqTm, orderId, orderState, errorCode, errorDescription);
        }

        @Override
        public String toString() {
            return "Status{" +
                    "rqUID='" + rqUID + '\'' +
                    ", rqTm=" + rqTm +
                    ", orderId='" + orderId + '\'' +
                    ", orderState=" + orderState +
                    ", errorCode='" + errorCode + '\'' +
                    ", errorDescription='" + errorDescription + '\'' +
                    '}';
        }
    }
}
