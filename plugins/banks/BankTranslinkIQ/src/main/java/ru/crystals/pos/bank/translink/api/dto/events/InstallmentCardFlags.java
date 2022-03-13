package ru.crystals.pos.bank.translink.api.dto.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class InstallmentCardFlags {

    /**
     * По карте можно взять рассрочку от банка-эмитента (Issuer Instalment).
     */
    private final boolean allowInstallmentIssuer;

    /**
     * Ограничения по рассрочке банка-эмитента (Issuer Instalment). Eсли null, то можно взять рассрочку с любым положительным числом платежей.
     */
    private final InstallmentForm installmentFormIssuer;

    /**
     * По карте можно взять рассрочку от магазина (Merchant Instalment).
     */
    private final boolean allowInstallmentMerchant;

    /**
     * Ограничения по рассрочке магазина (Merchant Instalment). Eсли null, то можно взять рассрочку с любым положительным числом платежей.
     */
    private final InstallmentForm installmentFormMerchant;

    /**
     * По карте можно взять рассрочку от банка-эквайрера (Acquirer Instalment).
     */
    private final boolean allowInstallmentAcquirer;

    /**
     * Ограничения по рассрочке банка-эквайрера (Acquirer Instalment). Eсли null, то можно взять рассрочку с любым положительным числом платежей.
     */
    private final InstallmentForm installmentFormAcquirer;

    @JsonCreator
    public InstallmentCardFlags(@JsonProperty("allowInstallmentIssuer") boolean allowInstallmentIssuer,
                                @JsonProperty("installmentFormIssuer") InstallmentForm installmentFormIssuer,
                                @JsonProperty("allowInstallmentMerchant") boolean allowInstallmentMerchant,
                                @JsonProperty("installmentFormMerchant") InstallmentForm installmentFormMerchant,
                                @JsonProperty("allowInstallmentAcquirer") boolean allowInstallmentAcquirer,
                                @JsonProperty("installmentFormAcquirer") InstallmentForm installmentFormAcquirer) {
        this.allowInstallmentIssuer = allowInstallmentIssuer;
        this.installmentFormIssuer = installmentFormIssuer;
        this.allowInstallmentMerchant = allowInstallmentMerchant;
        this.installmentFormMerchant = installmentFormMerchant;
        this.allowInstallmentAcquirer = allowInstallmentAcquirer;
        this.installmentFormAcquirer = installmentFormAcquirer;
    }

    public boolean isAllowInstallmentIssuer() {
        return allowInstallmentIssuer;
    }

    public InstallmentForm getInstallmentFormIssuer() {
        return installmentFormIssuer;
    }

    public boolean isAllowInstallmentMerchant() {
        return allowInstallmentMerchant;
    }

    public InstallmentForm getInstallmentFormMerchant() {
        return installmentFormMerchant;
    }

    public boolean isAllowInstallmentAcquirer() {
        return allowInstallmentAcquirer;
    }

    public InstallmentForm getInstallmentFormAcquirer() {
        return installmentFormAcquirer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InstallmentCardFlags that = (InstallmentCardFlags) o;
        return allowInstallmentIssuer == that.allowInstallmentIssuer &&
                allowInstallmentMerchant == that.allowInstallmentMerchant &&
                allowInstallmentAcquirer == that.allowInstallmentAcquirer &&
                Objects.equals(installmentFormIssuer, that.installmentFormIssuer) &&
                Objects.equals(installmentFormMerchant, that.installmentFormMerchant) &&
                Objects.equals(installmentFormAcquirer, that.installmentFormAcquirer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allowInstallmentIssuer, installmentFormIssuer, allowInstallmentMerchant, installmentFormMerchant, allowInstallmentAcquirer,
                installmentFormAcquirer);
    }

    @Override
    public String toString() {
        return "InstallmentCardFlags{" +
                "allowInstallmentIssuer=" + allowInstallmentIssuer +
                ", installmentFormIssuer=" + installmentFormIssuer +
                ", allowInstallmentMerchant=" + allowInstallmentMerchant +
                ", installmentFormMerchant=" + installmentFormMerchant +
                ", allowInstallmentAcquirer=" + allowInstallmentAcquirer +
                ", installmentFormAcquirer=" + installmentFormAcquirer +
                '}';
    }

    public static InstallmentCardFlagsBuilder builder() {
        return new InstallmentCardFlagsBuilder();
    }

    public static class InstallmentCardFlagsBuilder {
        private boolean allowInstallmentIssuer;
        private InstallmentForm installmentFormIssuer;
        private boolean allowInstallmentMerchant;
        private InstallmentForm installmentFormMerchant;
        private boolean allowInstallmentAcquirer;
        private InstallmentForm installmentFormAcquirer;

        InstallmentCardFlagsBuilder() {
        }

        public InstallmentCardFlagsBuilder allowInstallmentIssuer(boolean allowInstallmentIssuer) {
            this.allowInstallmentIssuer = allowInstallmentIssuer;
            return this;
        }

        public InstallmentCardFlagsBuilder installmentFormIssuer(InstallmentForm installmentFormIssuer) {
            this.installmentFormIssuer = installmentFormIssuer;
            return this;
        }

        public InstallmentCardFlagsBuilder allowInstallmentMerchant(boolean allowInstallmentMerchant) {
            this.allowInstallmentMerchant = allowInstallmentMerchant;
            return this;
        }

        public InstallmentCardFlagsBuilder installmentFormMerchant(InstallmentForm installmentFormMerchant) {
            this.installmentFormMerchant = installmentFormMerchant;
            return this;
        }

        public InstallmentCardFlagsBuilder allowInstallmentAcquirer(boolean allowInstallmentAcquirer) {
            this.allowInstallmentAcquirer = allowInstallmentAcquirer;
            return this;
        }

        public InstallmentCardFlagsBuilder installmentFormAcquirer(InstallmentForm installmentFormAcquirer) {
            this.installmentFormAcquirer = installmentFormAcquirer;
            return this;
        }

        public InstallmentCardFlags build() {
            return new InstallmentCardFlags(allowInstallmentIssuer,
                    installmentFormIssuer,
                    allowInstallmentMerchant,
                    installmentFormMerchant,
                    allowInstallmentAcquirer,
                    installmentFormAcquirer);
        }
    }
}
