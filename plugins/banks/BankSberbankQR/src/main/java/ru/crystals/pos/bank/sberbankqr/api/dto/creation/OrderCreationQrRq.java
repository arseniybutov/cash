package ru.crystals.pos.bank.sberbankqr.api.dto.creation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;
import java.util.List;

public class OrderCreationQrRq {

    /**
     * "Уникальный идентификатор запроса. Параметр должен совпадать с передаваемым в HTTP-заголовке параметром x-Introspect-RqUID"
     */
    @JsonProperty("rq_uid")
    private String rqUID;

    /**
     * Дата/время формирования запроса
     */
    @JsonProperty("rq_tm")
    private ZonedDateTime rqTm;

    /**
     * Идентификатор клиента
     */
    @JsonProperty("member_id")
    private String memberId;

    /**
     * Номер заказа в CRM Клиента
     */
    @JsonProperty("order_number")
    private String orderNumber;

    /**
     * Дата/время формирования заказа
     */
    @JsonProperty("order_create_date")
    private ZonedDateTime orderCreateDate;

    /**
     * Блок с параметрами заказа
     */
    @JsonProperty("order_params_type")
    private List<OrderParamsType> orderParams;

    /**
     * IdQR устройства, на котором сформирован заказ
     */
    @JsonProperty("id_qr")
    private String idQR;

    /**
     * Сумма заказа
     */
    @JsonProperty("order_sum")
    private int orderSum;

    /**
     * Валюта операции согласно ISO 4217, цифровой код
     */
    private String currency;

    /**
     * Описание заказа для отображения в отчетности
     */
    private String description;

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

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public ZonedDateTime getOrderCreateDate() {
        return orderCreateDate;
    }

    public void setOrderCreateDate(ZonedDateTime orderCreateDate) {
        this.orderCreateDate = orderCreateDate;
    }

    public List<OrderParamsType> getOrderParams() {
        return orderParams;
    }

    public void setOrderParams(List<OrderParamsType> orderParams) {
        this.orderParams = orderParams;
    }

    public String getIdQR() {
        return idQR;
    }

    public void setIdQR(String idQR) {
        this.idQR = idQR;
    }

    public int getOrderSum() {
        return orderSum;
    }

    public void setOrderSum(int orderSum) {
        this.orderSum = orderSum;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
