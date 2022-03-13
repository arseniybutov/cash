package ru.crystals.pos.bank.tinkoffsbp.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class InitResponse {

    private String terminalKey;

    private Long amount;

    private String orderId;

    private boolean success;

    private String status;

    private Long paymentId;

    private String errorCode;

    private String message;

    private String details;

    public InitResponse(@JsonProperty("TerminalKey") String terminalKey,
                        @JsonProperty("Amount") Long amount,
                        @JsonProperty("OrderId") String orderId,
                        @JsonProperty("Success") boolean success,
                        @JsonProperty("Status") String status,
                        @JsonProperty("PaymentId") Long paymentId,
                        @JsonProperty("ErrorCode") String errorCode,
                        @JsonProperty(value = "Message", required = false) String message,
                        @JsonProperty(value = "Details", required = false) String details) {
        this.terminalKey = terminalKey;
        this.amount = amount;
        this.orderId = orderId;
        this.success = success;
        this.status = status;
        this.paymentId = paymentId;
        this.errorCode = errorCode;
        this.message = message;
        this.details = details;
    }

    public String getTerminalKey() {
        return terminalKey;
    }

    public Long getAmount() {
        return amount;
    }

    public String getOrderId() {
        return orderId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getStatus() {
        return status;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InitResponse that = (InitResponse) o;
        return success == that.success && Objects.equals(terminalKey, that.terminalKey) && Objects.equals(amount, that.amount)
                && Objects.equals(orderId, that.orderId) && Objects.equals(status, that.status) && Objects.equals(paymentId, that.paymentId)
                && Objects.equals(errorCode, that.errorCode) && Objects.equals(message, that.message) && Objects.equals(details, that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(terminalKey, amount, orderId, success, status, paymentId, errorCode, message, details);
    }
}
