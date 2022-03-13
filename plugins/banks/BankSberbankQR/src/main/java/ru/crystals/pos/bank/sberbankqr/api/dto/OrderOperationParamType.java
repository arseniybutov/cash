package ru.crystals.pos.bank.sberbankqr.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;
import java.util.Objects;

public class OrderOperationParamType {

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
     * RRN операции
     */
    @JsonProperty("rrn")
    private String rrn;

    /**
     * Тип операции
     */
    @JsonProperty("operation_type")
    private OperationType operationType;

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
     * Код ответа на операцию (код успешности авторизации)
     */
    @JsonProperty("response_code")
    private String responseCode;

    /**
     * Описание кода ответа
     */
    @JsonProperty("response_desc")
    private String responseDesc;

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

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
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

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseDesc() {
        return responseDesc;
    }

    public void setResponseDesc(String responseDesc) {
        this.responseDesc = responseDesc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OrderOperationParamType that = (OrderOperationParamType) o;
        return operationSum == that.operationSum &&
                Objects.equals(operationId, that.operationId) &&
                Objects.equals(operationDateTime, that.operationDateTime) &&
                Objects.equals(rrn, that.rrn) &&
                Objects.equals(operationType, that.operationType) &&
                Objects.equals(operationCurrency, that.operationCurrency) &&
                Objects.equals(authCode, that.authCode) &&
                Objects.equals(responseCode, that.responseCode) &&
                Objects.equals(responseDesc, that.responseDesc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operationId, operationDateTime, rrn, operationType, operationSum, operationCurrency, authCode,
                responseCode, responseDesc);
    }

    @Override
    public String toString() {
        return "OrderOperationParamType{" +
                "operationId='" + operationId + '\'' +
                ", operationDateTime=" + operationDateTime +
                ", rrn='" + rrn + '\'' +
                ", operationType='" + operationType + '\'' +
                ", operationSum=" + operationSum +
                ", operationCurrency='" + operationCurrency + '\'' +
                ", authCode='" + authCode + '\'' +
                ", responseCode='" + responseCode + '\'' +
                ", responseDesc='" + responseDesc + '\'' +
                '}';
    }
}
