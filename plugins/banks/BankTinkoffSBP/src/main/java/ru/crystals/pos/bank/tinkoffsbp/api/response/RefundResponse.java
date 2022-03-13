package ru.crystals.pos.bank.tinkoffsbp.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RefundResponse {
    private String terminalKey;

    private boolean success;

    private ResponseStatus status;

    private Long paymentId;

    private String errorCode;

    private String orderId;

    private Long originalAmount;

    private Long newAmount;

    private String message;

    private String details;

    public RefundResponse(@JsonProperty("TerminalKey") String terminalKey,
                          @JsonProperty("Success") boolean success,
                          @JsonProperty("Status") ResponseStatus status,
                          @JsonProperty("PaymentId") Long paymentId,
                          @JsonProperty("ErrorCode") String errorCode,
                          @JsonProperty("OrderId") String orderId,
                          @JsonProperty("OriginalAmount") Long originalAmount,
                          @JsonProperty("NewAmount") Long newAmount,
                          @JsonProperty(value = "Message", required = false) String message,
                          @JsonProperty(value = "Details", required = false) String details) {
        this.terminalKey = terminalKey;
        this.success = success;
        this.status = status;
        this.paymentId = paymentId;
        this.errorCode = errorCode;
        this.orderId = orderId;
        this.originalAmount = originalAmount;
        this.newAmount = newAmount;
        this.message = message;
        this.details = details;
    }

    public String getTerminalKey() {
        return terminalKey;
    }

    public boolean isSuccess() {
        return success;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getOrderId() {
        return orderId;
    }

    public Long getOriginalAmount() {
        return originalAmount;
    }

    public Long getNewAmount() {
        return newAmount;
    }

    public String getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return "RefundResponse{" +
                "terminalKey='" + terminalKey + '\'' +
                ", success=" + success +
                ", status='" + status + '\'' +
                ", paymentId=" + paymentId +
                ", errorCode='" + errorCode + '\'' +
                ", orderId='" + orderId + '\'' +
                ", originalAmount=" + originalAmount +
                ", newAmount=" + newAmount +
                ", message='" + message + '\'' +
                ", details='" + details + '\'' +
                '}';
    }
}
