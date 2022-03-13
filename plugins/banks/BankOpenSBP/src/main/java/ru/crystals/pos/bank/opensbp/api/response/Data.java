package ru.crystals.pos.bank.opensbp.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.bank.opensbp.api.request.Params;
import ru.crystals.pos.bank.opensbp.api.status.PaymentOperationStatus;

import java.util.Objects;

public class Data {

    /**
     * Идентификатор QR кода, запрос по которому проводился
     */
    private String qrcId;

    /**
     * Код ответа на запрос статуса QR кода, example: RQ00000
     */
    private String code;

    /**
     * Описание кода ответа на запрос
     */
    private String message;

    /**
     * Статус операции, инициированной Dynamic QR кодом
     */
    private PaymentOperationStatus status;

    /**
     * Номер операции в платежной системе банка, возвращается для успешной операции
     */
    private int operationId;

    /**
     * Идентификатор операции, инициированной Dynamic QR кодом
     */
    private String trxId;

    /**
     * Сумма операции в копейках
     */
    private long amount;

    /**
     * Дата и время операции в UTC, возвращается для успешной операции
     */
    private String operationTimestamp;

    /**
     * Произвольные параметры
     */
    private Params params;

    /**
     * Уникальный идентификатор заказа в системе партнёра
     */
    private String order;

    @JsonCreator
    public Data(@JsonProperty("qrcId") String qrcId,
                @JsonProperty("message") String message,
                @JsonProperty("status") PaymentOperationStatus status,
                @JsonProperty("operationId") int operationId,
                @JsonProperty("trxId") String trxId,
                @JsonProperty("amount") long amount,
                @JsonProperty("operationTimestamp") String operationTimestamp,
                @JsonProperty("params") Params params,
                @JsonProperty("order") String order,
                @JsonProperty("code") String code) {
        this.qrcId = qrcId;
        this.message = message;
        this.status = status;
        this.operationId = operationId;
        this.trxId = trxId;
        this.amount = amount;
        this.operationTimestamp = operationTimestamp;
        this.params = params;
        this.code = code;
        this.order = order;
    }

    public Data(String qrcId, String code, String message, PaymentOperationStatus status, int operationId, String operationTimestamp) {
        this.qrcId = qrcId;
        this.code = code;
        this.message = message;
        this.status = status;
        this.operationId = operationId;
        this.operationTimestamp = operationTimestamp;
    }

    public String getInfoMessage() {
        return "Code: " + code + "; Message: " + message + "; Status: " + status + ";";
    }

    public String getQrcId() {
        return qrcId;
    }

    public void setQrcId(String qrcId) {
        this.qrcId = qrcId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String responseCode) {
        this.code = responseCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public PaymentOperationStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentOperationStatus status) {
        this.status = status;
    }

    public int getOperationId() {
        return operationId;
    }

    public void setOperationId(int operationId) {
        this.operationId = operationId;
    }

    public String getTrxId() {
        return trxId;
    }

    public void setTrxId(String trxId) {
        this.trxId = trxId;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getOperationTimestamp() {
        return operationTimestamp;
    }

    public void setOperationTimestamp(String operationTimestamp) {
        this.operationTimestamp = operationTimestamp;
    }

    public Params getParams() {
        return params;
    }

    public void setParams(Params params) {
        this.params = params;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Data data = (Data) o;
        return operationId == data.operationId &&
                amount == data.amount &&
                Objects.equals(qrcId, data.qrcId) &&
                Objects.equals(message, data.message) &&
                status == data.status &&
                Objects.equals(trxId, data.trxId) &&
                Objects.equals(operationTimestamp, data.operationTimestamp) &&
                Objects.equals(code, data.code) &&
                Objects.equals(order, data.order) &&
                Objects.equals(params, data.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(qrcId, code, message, status, operationId, trxId, amount, operationTimestamp, params, order);
    }

    @Override
    public String toString() {
        return "Data{" +
                "qrcId='" + qrcId + '\'' +
                ", code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", paymentOperationStatus=" + status +
                ", operationId=" + operationId +
                ", trxId='" + trxId + '\'' +
                ", amount=" + amount +
                ", operationTimestamp='" + operationTimestamp + '\'' +
                ", code='" + code + '\'' +
                ", order='" + order + '\'' +
                ", params=" + params +
                '}';
    }
}
