package ru.crystals.pos.fiscalprinter.retailforce.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Objects;

public class DocumentPayment {
    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("isCash")
    private boolean cash;

    @JsonProperty("paymentType")
    private PaymentTypes paymentType;

    @JsonProperty("currencyIsoCode")
    private String currencyIsoCode;

    @JsonProperty("caption")
    private String caption;

    @JsonProperty("uniqueReadablePaymentIdentifier")
    private String uniqueReadablePaymentIdentifier;

    @JsonProperty("foreignAmount")
    private BigDecimal foreignAmount;

    @JsonProperty("foreignAmountExchangeRate")
    private BigDecimal foreignAmountExchangeRate;

    public DocumentPayment() {
    }

    DocumentPayment(BigDecimal amount,
                    boolean cash,
                    PaymentTypes paymentType,
                    String currencyIsoCode,
                    String caption,
                    String uniqueReadablePaymentIdentifier,
                    BigDecimal foreignAmount,
                    BigDecimal foreignAmountExchangeRate) {
        this.amount = amount;
        this.cash = cash;
        this.paymentType = paymentType;
        this.currencyIsoCode = currencyIsoCode;
        this.caption = caption;
        this.uniqueReadablePaymentIdentifier = uniqueReadablePaymentIdentifier;
        this.foreignAmount = foreignAmount;
        this.foreignAmountExchangeRate = foreignAmountExchangeRate;
    }

    public static DocumentPaymentBuilder builder() {
        return new DocumentPaymentBuilder();
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public boolean isCash() {
        return cash;
    }

    public PaymentTypes getPaymentType() {
        return paymentType;
    }

    public String getCurrencyIsoCode() {
        return currencyIsoCode;
    }

    public String getCaption() {
        return caption;
    }

    public String getUniqueReadablePaymentIdentifier() {
        return uniqueReadablePaymentIdentifier;
    }

    public BigDecimal getForeignAmount() {
        return foreignAmount;
    }

    public BigDecimal getForeignAmountExchangeRate() {
        return foreignAmountExchangeRate;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCash(boolean isCash) {
        this.cash = isCash;
    }

    public void setPaymentType(PaymentTypes paymentType) {
        this.paymentType = paymentType;
    }

    public void setCurrencyIsoCode(String currencyIsoCode) {
        this.currencyIsoCode = currencyIsoCode;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setUniqueReadablePaymentIdentifier(String uniqueReadablePaymentIdentifier) {
        this.uniqueReadablePaymentIdentifier = uniqueReadablePaymentIdentifier;
    }

    public void setForeignAmount(BigDecimal foreignAmount) {
        this.foreignAmount = foreignAmount;
    }

    public void setForeignAmountExchangeRate(BigDecimal foreignAmountExchangeRate) {
        this.foreignAmountExchangeRate = foreignAmountExchangeRate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DocumentPayment that = (DocumentPayment) o;
        return cash == that.cash
                && (amount != null && amount.compareTo(that.amount) == 0)
                && Objects.equals(paymentType, that.paymentType)
                && Objects.equals(currencyIsoCode, that.currencyIsoCode)
                && Objects.equals(caption, that.caption)
                && Objects.equals(uniqueReadablePaymentIdentifier, that.uniqueReadablePaymentIdentifier)
                && (foreignAmount != null && foreignAmount.compareTo(that.foreignAmount) == 0)
                && (foreignAmountExchangeRate != null && foreignAmountExchangeRate.compareTo(that.foreignAmountExchangeRate) == 0);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, cash, paymentType, currencyIsoCode, caption, uniqueReadablePaymentIdentifier, foreignAmount, foreignAmountExchangeRate);
    }

    @Override
    public String toString() {
        return "DocumentPayment{" +
                "amount=" + amount +
                ", isCash=" + cash +
                ", paymentType=" + paymentType.name() +
                ", currencyIsoCode='" + currencyIsoCode + '\'' +
                ", caption='" + caption + '\'' +
                ", uniqueReadablePaymentIdentifier='" + uniqueReadablePaymentIdentifier + '\'' +
                ", foreignAmount=" + foreignAmount +
                ", foreignAmountExchangeRate=" + foreignAmountExchangeRate +
                '}';
    }

    public static class DocumentPaymentBuilder {
        private BigDecimal amount;
        private boolean isCash;
        private PaymentTypes paymentType;
        private String currencyIsoCode;
        private String caption;
        private String uniqueReadablePaymentIdentifier;
        private BigDecimal foreignAmount;
        private BigDecimal foreignAmountExchangeRate;

        DocumentPaymentBuilder() {
        }

        public DocumentPaymentBuilder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public DocumentPaymentBuilder isCash(boolean isCash) {
            this.isCash = isCash;
            return this;
        }

        public DocumentPaymentBuilder paymentType(PaymentTypes paymentType) {
            this.paymentType = paymentType;
            return this;
        }

        public DocumentPaymentBuilder currencyIsoCode(String currencyIsoCode) {
            this.currencyIsoCode = currencyIsoCode;
            return this;
        }

        public DocumentPaymentBuilder caption(String caption) {
            this.caption = caption;
            return this;
        }

        public DocumentPaymentBuilder uniqueReadablePaymentIdentifier(String uniqueReadablePaymentIdentifier) {
            this.uniqueReadablePaymentIdentifier = uniqueReadablePaymentIdentifier;
            return this;
        }

        public DocumentPaymentBuilder foreignAmount(BigDecimal foreignAmount) {
            this.foreignAmount = foreignAmount;
            return this;
        }

        public DocumentPaymentBuilder foreignAmountExchangeRate(BigDecimal foreignAmountExchangeRate) {
            this.foreignAmountExchangeRate = foreignAmountExchangeRate;
            return this;
        }

        public DocumentPayment build() {
            return new DocumentPayment(amount,
                    isCash,
                    paymentType,
                    currencyIsoCode,
                    caption,
                    uniqueReadablePaymentIdentifier,
                    foreignAmount,
                    foreignAmountExchangeRate);
        }
    }
}

