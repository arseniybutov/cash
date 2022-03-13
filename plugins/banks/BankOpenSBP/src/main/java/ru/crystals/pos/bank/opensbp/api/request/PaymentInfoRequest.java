package ru.crystals.pos.bank.opensbp.api.request;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentInfoRequest {
    @JsonProperty("qrcIds")
    private String[] qrcIds;

    public PaymentInfoRequest(String[] qrcIds) {
        this.qrcIds = qrcIds;
    }

    @JsonGetter
    public String[] getQrcIds() {
        return qrcIds;
    }


}
