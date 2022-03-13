package ru.crystals.pos.bank.translink.api.dto.commands;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Оплата в рассрочку
 * <p>
 * The command triggers the consumer credit procedure for the payment transaction in progress. To prepare the terminal for this operation, UNLOCKDEVICE with
 * Operation code 0 (NOOPERATION) with sum 0 must be sent
 */
public class InstallmentCommand implements CommandParams {

    /**
     * Общая сумма транзакции (включая CashBack)
     */
    private final long amount;

    /**
     * Код валюты
     */
    private final String currencyCode;

    /**
     * Количество платежей по рассрочке
     */
    private final int installmentPaymentCount;

    /**
     * Провайдер рассрочки
     */
    private final InstallmentProvider installmentProvider;

    /**
     * Номер платежного документа (уникальный номер в течение банковского дня)
     */
    private final String documentNr;

    /**
     * 4 последние цифры PAN, которые введены на устройстве ECR, оператором кассы. Данное требование может быть инициировано POS, при передаче события ONCARD.
     */
    private final String panL4Digit;

    public InstallmentCommand(long amount,
                              int installmentPaymentCount,
                              InstallmentProvider installmentProvider,
                              String currencyCode,
                              String documentNr,
                              String panL4Digit) {
        this.amount = amount;
        this.installmentPaymentCount = installmentPaymentCount;
        this.installmentProvider = installmentProvider;
        this.currencyCode = Objects.requireNonNull(currencyCode);
        this.documentNr = Objects.requireNonNull(documentNr);
        this.panL4Digit = panL4Digit;
    }

    @JsonProperty("amount")
    public long getAmount() {
        return amount;
    }

    @JsonProperty("currencyCode")
    public String getCurrencyCode() {
        return currencyCode;
    }

    @JsonProperty("installmentPaymentCount")
    public int getInstallmentPaymentCount() {
        return installmentPaymentCount;
    }

    @JsonProperty("installmentProvider")
    public InstallmentProvider getInstallmentProvider() {
        return installmentProvider;
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
        InstallmentCommand that = (InstallmentCommand) o;
        return amount == that.amount &&
                installmentPaymentCount == that.installmentPaymentCount &&
                installmentProvider == that.installmentProvider &&
                Objects.equals(currencyCode, that.currencyCode) &&
                Objects.equals(documentNr, that.documentNr) &&
                Objects.equals(panL4Digit, that.panL4Digit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, installmentPaymentCount, installmentProvider, currencyCode, documentNr, panL4Digit);
    }

    @Override
    public String toString() {
        return "InstallmentCommand{" +
                "amount=" + amount +
                ", currencyCode='" + currencyCode + '\'' +
                ", installmentPaymentCount=" + installmentPaymentCount +
                ", installmentProvider=" + installmentProvider +
                ", documentNr='" + documentNr + '\'' +
                ", panL4Digit='" + panL4Digit + '\'' +
                '}';
    }

    public static InstallmentCommandBuilder builder() {
        return new InstallmentCommandBuilder();
    }

    public static class InstallmentCommandBuilder {

        private long amount;
        private int installmentPaymentCount;
        private InstallmentProvider installmentProvider;
        private String currencyCode;
        private String documentNr;
        private String panL4Digit;

        InstallmentCommandBuilder() {
        }

        public InstallmentCommandBuilder amount(long amount) {
            this.amount = amount;
            return this;
        }

        public InstallmentCommandBuilder installmentPaymentCount(int installmentPaymentCount) {
            this.installmentPaymentCount = installmentPaymentCount;
            return this;
        }

        public InstallmentCommandBuilder installmentProvider(InstallmentProvider installmentProvider) {
            this.installmentProvider = installmentProvider;
            return this;
        }

        public InstallmentCommandBuilder currencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
            return this;
        }

        public InstallmentCommandBuilder documentNr(String documentNr) {
            this.documentNr = documentNr;
            return this;
        }

        public InstallmentCommandBuilder panL4Digit(String panL4Digit) {
            this.panL4Digit = panL4Digit;
            return this;
        }

        public InstallmentCommand build() {
            return new InstallmentCommand(amount, installmentPaymentCount, installmentProvider, currencyCode, documentNr, panL4Digit);
        }
    }
}
