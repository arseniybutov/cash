package ru.crystals.pos.bank.opensbp.api.request;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RefundRequest {

    @JsonProperty("referenceId")
    private String referenceId;

    public RefundRequest(String referenceId) {
        this.referenceId = referenceId;
    }

    @JsonGetter
    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }
}
