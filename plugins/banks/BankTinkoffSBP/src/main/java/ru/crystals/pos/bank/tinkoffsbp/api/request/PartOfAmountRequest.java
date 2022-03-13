package ru.crystals.pos.bank.tinkoffsbp.api.request;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

public class PartOfAmountRequest implements TokenSetter {

    @JsonProperty("TerminalKey")
    @JsonView(Views.WithoutToken.class)
    private String terminalKey;

    @JsonProperty("PaymentId")
    @JsonView(Views.WithoutToken.class)
    private String paymentId;

    @JsonProperty("Token")
    @JsonView(Views.WithToken.class)
    private String token;

    @JsonProperty("Amount")
    @JsonView(Views.WithoutToken.class)
    private String amount;

    public PartOfAmountRequest(String terminalKey, String paymentId, String amount) {
        this.terminalKey = terminalKey;
        this.paymentId = paymentId;
        this.amount = amount;
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

    @JsonGetter
    public String getAmount() {
        return amount;
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

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
