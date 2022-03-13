package ru.crystals.pos.bank.gazpromsbp.api.request;

import com.fasterxml.jackson.annotation.JsonGetter;

public class ProcessRefundRequest {

    /**
     * id операции со стороны банка
     */
    private String transactionId;

    public ProcessRefundRequest(String transactionId) {
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
