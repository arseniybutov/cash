package ru.crystals.pos.bank.tinkoffsbp.api.request;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

public class InitRequest {

    @JsonView(Views.WithoutCard.class)
    @JsonProperty("TerminalKey")
    private String terminalKey;

    @JsonView(Views.WithoutCard.class)
    @JsonProperty("Amount")
    private String amount;

    @JsonView(Views.WithoutCard.class)
    @JsonProperty("OrderId")
    private String orderId;

    @JsonView(Views.WithCard.class)
    @JsonProperty("CustomerKey")
    private String cardId;

    public InitRequest(String terminalKey, String amount, String orderId, String cardId) {
        this.terminalKey = terminalKey;
        this.amount = amount;
        this.orderId = orderId;
        this.cardId = cardId;
    }

    public InitRequest(String terminalKey, String amount, String orderId) {
        this.terminalKey = terminalKey;
        this.amount = amount;
        this.orderId = orderId;
    }

    @JsonGetter
    public String getTerminalKey() {
        return terminalKey;
    }

    public void setTerminalKey(String terminalKey) {
        this.terminalKey = terminalKey;
    }

    @JsonGetter
    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    @JsonGetter
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @JsonGetter
    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }
}
