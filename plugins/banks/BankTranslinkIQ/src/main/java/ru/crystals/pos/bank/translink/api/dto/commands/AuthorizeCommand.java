package ru.crystals.pos.bank.translink.api.dto.commands;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Команда инициирует процедуру авторизации (оплата покупок) на POS
 */
public class AuthorizeCommand implements CommandParams {

    /**
     * Общая сумма транзакции (включая CashBack)
     */
    private final long amount;

    /**
     * Сумма Cashback
     */
    private final long cashBackAmount;

    /**
     * Код валюты
     */
    private final String currencyCode;

    /**
     * Номер платежного документа (уникальный номер в течение банковского дня)
     */
    private final String documentNr;

    /**
     * 4 последние цифры PAN , которые введены на устройстве ECR, оператором кассы. Данное требование может быть инициировано POS, при передаче события ONCARD.
     */
    private final String panL4Digit;

    public AuthorizeCommand(long amount, long cashBackAmount, String currencyCode, String documentNr, String panL4Digit) {
        this.amount = amount;
        this.cashBackAmount = cashBackAmount;
        this.currencyCode = Objects.requireNonNull(currencyCode);
        this.documentNr = Objects.requireNonNull(documentNr);
        this.panL4Digit = panL4Digit;
    }

    public AuthorizeCommand(long amount, long cashBackAmount, String currencyCode, String documentNr) {
        this(amount, cashBackAmount, currencyCode, documentNr, null);
    }

    public static AuthorizeCommandBuilder builder() {
        return new AuthorizeCommandBuilder();
    }

    @JsonProperty("amount")
    public long getAmount() {
        return amount;
    }

    @JsonProperty("cashBackAmount")
    public long getCashBackAmount() {
        return cashBackAmount;
    }

    @JsonProperty("currencyCode")
    public String getCurrencyCode() {
        return currencyCode;
    }

    @JsonProperty("documentNr")
    public String getDocumentNr() {
        return documentNr;
    }

    @JsonProperty("panL4Digit")
    public String getPanL4Digit() {
        return panL4Digit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthorizeCommand that = (AuthorizeCommand) o;
        return amount == that.amount &&
                cashBackAmount == that.cashBackAmount &&
                Objects.equals(currencyCode, that.currencyCode) &&
                Objects.equals(documentNr, that.documentNr) &&
                Objects.equals(panL4Digit, that.panL4Digit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, cashBackAmount, currencyCode, documentNr, panL4Digit);
    }

    @Override
    public String toString() {
        return "AuthorizeCommand{" +
                "amount=" + amount +
                ", cashBackAmount=" + cashBackAmount +
                ", currencyCode='" + currencyCode + '\'' +
                ", documentNr='" + documentNr + '\'' +
                ", panL4Digit='" + panL4Digit + '\'' +
                '}';
    }

    public static class AuthorizeCommandBuilder {
        private long amount;
        private long cashBackAmount;
        private String currencyCode;
        private String documentNr;
        private String panL4Digit;

        AuthorizeCommandBuilder() {
        }

        public AuthorizeCommandBuilder amount(long amount) {
            this.amount = amount;
            return this;
        }

        public AuthorizeCommandBuilder cashBackAmount(long cashBackAmount) {
            this.cashBackAmount = cashBackAmount;
            return this;
        }

        public AuthorizeCommandBuilder currencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
            return this;
        }

        public AuthorizeCommandBuilder documentNr(String documentNr) {
            this.documentNr = documentNr;
            return this;
        }

        public AuthorizeCommandBuilder panL4Digit(String panL4Digit) {
            this.panL4Digit = panL4Digit;
            return this;
        }

        public AuthorizeCommand build() {
            return new AuthorizeCommand(amount, cashBackAmount, currencyCode, documentNr, panL4Digit);
        }
    }
}
