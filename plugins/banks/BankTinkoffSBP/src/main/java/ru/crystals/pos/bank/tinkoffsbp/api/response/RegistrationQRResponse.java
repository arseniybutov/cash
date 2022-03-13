package ru.crystals.pos.bank.tinkoffsbp.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RegistrationQRResponse {
    private String terminalKey;

    private String orderId;

    private boolean success;

    /**
     * Payload
     */
    private String data;

    private String paymentId;

    private String errorCode;

    private String message;

    private String details;

    public RegistrationQRResponse(@JsonProperty("TerminalKey") String terminalKey,
                                  @JsonProperty("OrderId") String orderId,
                                  @JsonProperty("Success") boolean success,
                                  @JsonProperty("Data") String data,
                                  @JsonProperty("PaymentId") String paymentId,
                                  @JsonProperty("ErrorCode") String errorCode,
                                  @JsonProperty(value = "Message", required = false) String message,
                                  @JsonProperty(value = "Details", required = false) String details) {
        this.terminalKey = terminalKey;
        this.orderId = orderId;
        this.success = success;
        this.data = data;
        this.paymentId = paymentId;
        this.errorCode = errorCode;
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

    public String getData() {
        return data;
    }

    public String getPaymentId() {
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
}
