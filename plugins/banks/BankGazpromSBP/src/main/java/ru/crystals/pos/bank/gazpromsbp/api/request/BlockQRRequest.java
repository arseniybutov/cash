package ru.crystals.pos.bank.gazpromsbp.api.request;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BlockQRRequest {

    /**
     * Идентификатор зарегистрированного QR кода в СБП
     */
    @JsonProperty("qrcId")
    private String qrcId;

    public BlockQRRequest(String qrcId) {
        this.qrcId = qrcId;
    }

    @JsonGetter
    public String getQrcId() {
        return qrcId;
    }

    public void setQrcId(String qrcId) {
        this.qrcId = qrcId;
    }
}
