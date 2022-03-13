package ru.crystals.pos.bank.translink.api.dto.commands;

import com.fasterxml.jackson.annotation.JsonGetter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Команда инициирует на POS процедуру возврата средств на счета карты.
 */
public class RefundCommand implements CommandParams {

    /**
     * Cумма транзакции
     */
    private final long amount;

    /**
     * Код валюты
     */
    private final String currencyCode;

    /**
     * Номер кассового чека
     */
    private final String documentNr;

    /**
     * 4 последние цифры PAN , которые введены на устройстве ECR, оператором кассы. Данное требование может быть инициировано POS, при передаче события ONCARD.
     */
    private final String panL4Digit;

    /**
     * Время операции, для которой выполняется Refund формат yyyyMMddHHmmss
     */
    private final LocalDateTime time;

    /**
     * STAN Для которой выполняется Refund
     * формат NNNNNN
     */
    private final String stan;

    /**
     * RRN Для которой выполняется Refund
     * UUUUUUUUUUUU
     */
    private final String rrn;

    public RefundCommand(long amount, String currencyCode, String documentNr, String panL4Digit, LocalDateTime time, String stan, String rrn) {
        this.amount = amount;
        this.currencyCode = Objects.requireNonNull(currencyCode);
        this.documentNr = Objects.requireNonNull(documentNr);
        this.panL4Digit = panL4Digit;
        this.time = time;
        this.stan = stan;
        this.rrn = rrn;
    }

    public static RefundCommandBuilder builder() {
        return new RefundCommandBuilder();
    }

    @JsonGetter("amount")
    public long getAmount() {
        return amount;
    }

    @JsonGetter("currencyCode")
    public String getCurrencyCode() {
        return currencyCode;
    }

    @JsonGetter("documentNr")
    public String getDocumentNr() {
        return documentNr;
    }

    @JsonGetter("panL4Digit")
    public String getPanL4Digit() {
        return panL4Digit;
    }

    @JsonGetter("time")
    public LocalDateTime getTime() {
        return time;
    }

    @JsonGetter("STAN")
    public String getStan() {
        return stan;
    }

    @JsonGetter("RRN")
    public String getRrn() {
        return rrn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RefundCommand that = (RefundCommand) o;
        return amount == that.amount &&
                Objects.equals(currencyCode, that.currencyCode) &&
                Objects.equals(documentNr, that.documentNr) &&
                Objects.equals(panL4Digit, that.panL4Digit) &&
                Objects.equals(time, that.time) &&
                Objects.equals(stan, that.stan) &&
                Objects.equals(rrn, that.rrn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currencyCode, documentNr, panL4Digit, time, stan, rrn);
    }

    @Override
    public String toString() {
        return "RefundCommand{" +
                "amount=" + amount +
                ", currencyCode='" + currencyCode + '\'' +
                ", documentNr='" + documentNr + '\'' +
                ", panL4Digit='" + panL4Digit + '\'' +
                ", time=" + time +
                ", stan='" + stan + '\'' +
                ", rrn='" + rrn + '\'' +
                '}';
    }

    public static class RefundCommandBuilder {
        private long amount;
        private String currencyCode;
        private String documentNr;
        private String panL4Digit;
        private LocalDateTime time;
        private String stan;
        private String rrn;

        RefundCommandBuilder() {
        }

        public RefundCommandBuilder amount(long amount) {
            this.amount = amount;
            return this;
        }

        public RefundCommandBuilder currencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
            return this;
        }

        public RefundCommandBuilder documentNr(String documentNr) {
            this.documentNr = documentNr;
            return this;
        }

        public RefundCommandBuilder panL4Digit(String panL4Digit) {
            this.panL4Digit = panL4Digit;
            return this;
        }

        public RefundCommandBuilder time(LocalDateTime time) {
            this.time = time;
            return this;
        }

        public RefundCommandBuilder stan(String stan) {
            this.stan = stan;
            return this;
        }

        public RefundCommandBuilder rrn(String rrn) {
            this.rrn = rrn;
            return this;
        }

        public RefundCommand build() {
            return new RefundCommand(amount, currencyCode, documentNr, panL4Digit, time, stan, rrn);
        }
    }
}
