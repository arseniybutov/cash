package ru.crystals.pos.bank.sberbankqr.api.dto.status;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;

public class OrderStatusRequestQrRq {

    /**
     * Уникальный ID запроса, формируемый инициатором
     */
    @JsonProperty("rq_uid")
    private String rqUID;

    /**
     * Время формирования операции (запроса)
     */
    @JsonProperty("rq_tm")
    private ZonedDateTime rqTm;

    /**
     * Номер заказа в ППРБ.Карты (Сбербанк)
     */
    @JsonProperty("order_id")
    private String orderId;

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
}
