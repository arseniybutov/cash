package ru.crystals.pos.bank.tinkoffsbp.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QRInfoResponse {

    private String terminalKey;

    private String orderId;

    private boolean success;

    private ResponseStatus status;

    private Long paymentId;

    private String errorCode;

    private Long amount;

    private String message;

    private String details;

    public QRInfoResponse(@JsonProperty("TerminalKey") String terminalKey,
                          @JsonProperty("OrderId") String orderId,
                          @JsonProperty("Success") boolean success,
                          @JsonProperty("Status") ResponseStatus status,
                          @JsonProperty("PaymentId") Long paymentId,
                          @JsonProperty("ErrorCode") String errorCode,
                          @JsonProperty(value = "Amount", required = false) Long amount,
                          @JsonProperty(value = "Message", required = false) String message,
                          @JsonProperty(value = "Details", required = false) String details) {
        this.terminalKey = terminalKey;
        this.orderId = orderId;
        this.success = success;
        this.status = status;
        this.paymentId = paymentId;
        this.errorCode = errorCode;
        this.amount = amount;
        this.message = message;
        this.details = details;
    }

    public String getTerminalKey() {
        return terminalKey;
    }

    public String getOrderId() {
        return orderId;
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

    public Long getAmount() {
        return amount;
    }

    public String getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
    }
}
