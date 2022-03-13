package ru.crystals.pos.bank.sberbankqr.api.dto.cancel;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;

public class OrderCancelQrRq {

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
     * Номер (ID) заказа в ППРБ.Карты (Сбербанк)
     */
    @JsonProperty("order_id")
    private String orderId;

    /**
     * Уникальный идентификатор операции в ППРБ.Карты (прямой/возврата), которую необходимо отменить.
     */
    @JsonProperty("operation_id")
    private String operationId;

    /**
     * Код авторизации
     */
    @JsonProperty("auth_code")
    private String authCode;

    /**
     * IdQR устройства, на котором сформирован заказ
     */
    @JsonProperty("id_qr")
    private String idQR;

    /**
     * Сумма в минимальных единицах валюты (копейках), которую нужно отменить/возвратить
     */
    @JsonProperty("cancel_operation_sum")
    private int cancelOperationSum;

    /**
     * Валюта, в которой нужно выполнить отмену/возврат
     */
    @JsonProperty("operation_currency")
    private String operationCurrency;

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

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getIdQR() {
        return idQR;
    }

    public void setIdQR(String idQR) {
        this.idQR = idQR;
    }

    public int getCancelOperationSum() {
        return cancelOperationSum;
    }

    public void setCancelOperationSum(int cancelOperationSum) {
        this.cancelOperationSum = cancelOperationSum;
    }

    public String getOperationCurrency() {
        return operationCurrency;
    }

    public void setOperationCurrency(String operationCurrency) {
        this.operationCurrency = operationCurrency;
    }
}
