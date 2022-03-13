package ru.crystals.pos.bank.opensbp.api.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Amount {

    /**
     * Сумма в копейках
     */
    @JsonProperty("amount")
    private long amount;

    /**
     * Валюта операции
     */
    @JsonProperty("currencyCode")
    private String currencyCode;

    @JsonCreator
    public Amount(@JsonProperty("amount") long amount,
                  @JsonProperty("currencyCode") String currencyCode) {
        this.amount = amount;
        this.currencyCode = currencyCode;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Amount money = (Amount) o;
        return amount == money.amount && Objects.equals(currencyCode, money.currencyCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currencyCode);
    }

    @Override
    public String toString() {
        return "Money{" +
                "amount=" + amount +
                ", currencyCode='" + currencyCode + '\'' +
                '}';
    }
}
