package ru.crystals.pos.bank.gazpromsbp.api.request;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RefundInfoRequest {

    /**
     * ID операции в СБПGate Master System
     */
    @JsonProperty("transactionId")
    private String transactionId;

    public RefundInfoRequest(String transactionId) {
        this.transactionId = transactionId;
    }

    @JsonGetter
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
