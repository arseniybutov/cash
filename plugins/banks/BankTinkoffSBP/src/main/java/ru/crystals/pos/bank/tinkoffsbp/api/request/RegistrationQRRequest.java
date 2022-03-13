package ru.crystals.pos.bank.tinkoffsbp.api.request;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

public class RegistrationQRRequest implements TokenSetter {

    @JsonProperty("TerminalKey")
    @JsonView(Views.WithoutToken.class)
    private String terminalKey;

    @JsonProperty("PaymentId")
    @JsonView(Views.WithoutToken.class)
    private String paymentId;

    @JsonProperty("Token")
    @JsonView(Views.WithToken.class)
    private String token;

    public RegistrationQRRequest(String terminalKey, String paymentId) {
        this.terminalKey = terminalKey;
        this.paymentId = paymentId;
    }

    @JsonGetter
    public String getTerminalKey() {
        return terminalKey;
    }

    @JsonGetter
    public String getPaymentId() {
        return paymentId;
    }

    @JsonGetter
    public String getToken() {
        return token;
    }

    public void setTerminalKey(String terminalKey) {
        this.terminalKey = terminalKey;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    @Override
    public void setToken(String token) {
        this.token = token;
    }
}
