package ru.crystals.pos.bank.translink.api.dto.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class CardFlags {

    /**
     * Возможность использования для операций лояльности.
     */
    private final boolean loyCard;

    /**
     * Платежная карта.
     */
    private final boolean allowAuthorize;

    /**
     * Платежная карта с поддержкой операции прeавторизации.
     */
    private final boolean allowPreAuthorize;

    /**
     * Платежная карта с поддержкой операции refund.
     */
    private final boolean allowRefund;

    /**
     * Если этот фаг установлен, карту можно использовать для оплаты полной суммы покупки. (На данный момент не используется)
     */
    private final boolean fullAmountOnly;

    /**
     * Если этот фаг установлен, карту нельзя использовать для оплаты скидок.(На данный момент не используется)
     */
    private final boolean noDiscounts;

    /**
     * Если этот фаг установлен, кассиру ECR будет необходимо ввести 4 последние цифры номера карты. Дополнительно будет инициировано событие
     */
    private final boolean reqPANL4Digit;

    /**
     * Флаг определяет требование ввода данных об оригинальной транзакции (time/STAN) во время выполнения операции REFUND.
     * У нас пока не обрабатывается, потому что time нет в данных о транзакции продажи и предполагается, что кассир будет вводить его руками со слипа
     */
    private final boolean reqOriginalData;

    /**
     * Флаг определяет требование ввода RRN оригинальной транзакции во время выполнения операции REFUND
     */
    private final boolean reqOriginalRRN;

    /**
     * Платежная картa может быть использована для авторизаций с CashBack.
     */
    private final boolean allowCashBack;

    @JsonCreator
    public CardFlags(@JsonProperty("loyCard") boolean loyCard,
                     @JsonProperty("allowAuthorize") boolean allowAuthorize,
                     @JsonProperty("allowPreAuthorize") boolean allowPreAuthorize,
                     @JsonProperty("allowRefund") boolean allowRefund,
                     @JsonProperty("fullAmountOnly") boolean fullAmountOnly,
                     @JsonProperty("noDiscounts") boolean noDiscounts,
                     @JsonProperty("reqPANL4Digit") boolean reqPANL4Digit,
                     @JsonProperty("reqOriginalData") boolean reqOriginalData,
                     @JsonProperty("reqOriginalRRN") boolean reqOriginalRRN,
                     @JsonProperty("allowCashBack") boolean allowCashBack) {
        this.loyCard = loyCard;
        this.allowAuthorize = allowAuthorize;
        this.allowPreAuthorize = allowPreAuthorize;
        this.allowRefund = allowRefund;
        this.fullAmountOnly = fullAmountOnly;
        this.noDiscounts = noDiscounts;
        this.reqPANL4Digit = reqPANL4Digit;
        this.reqOriginalData = reqOriginalData;
        this.reqOriginalRRN = reqOriginalRRN;
        this.allowCashBack = allowCashBack;
    }

    public boolean isLoyCard() {
        return loyCard;
    }

    public boolean isAllowAuthorize() {
        return allowAuthorize;
    }

    public boolean isAllowPreAuthorize() {
        return allowPreAuthorize;
    }

    public boolean isAllowRefund() {
        return allowRefund;
    }

    public boolean isFullAmountOnly() {
        return fullAmountOnly;
    }

    public boolean isNoDiscounts() {
        return noDiscounts;
    }

    public boolean isReqPANL4Digit() {
        return reqPANL4Digit;
    }

    public boolean isReqOriginalData() {
        return reqOriginalData;
    }

    public boolean isReqOriginalRRN() {
        return reqOriginalRRN;
    }

    public boolean isAllowCashBack() {
        return allowCashBack;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CardFlags cardFlags = (CardFlags) o;
        return loyCard == cardFlags.loyCard &&
                allowAuthorize == cardFlags.allowAuthorize &&
                allowPreAuthorize == cardFlags.allowPreAuthorize &&
                allowRefund == cardFlags.allowRefund &&
                fullAmountOnly == cardFlags.fullAmountOnly &&
                noDiscounts == cardFlags.noDiscounts &&
                reqPANL4Digit == cardFlags.reqPANL4Digit &&
                reqOriginalData == cardFlags.reqOriginalData &&
                reqOriginalRRN == cardFlags.reqOriginalRRN &&
                allowCashBack == cardFlags.allowCashBack;
    }

    @Override
    public String toString() {
        return "CardFlags{" +
                "loyCard=" + loyCard +
                ", allowAuthorize=" + allowAuthorize +
                ", allowPreAuthorize=" + allowPreAuthorize +
                ", allowRefund=" + allowRefund +
                ", fullAmountOnly=" + fullAmountOnly +
                ", noDiscounts=" + noDiscounts +
                ", reqPANL4Digit=" + reqPANL4Digit +
                ", reqOriginalData=" + reqOriginalData +
                ", reqOriginalRRN=" + reqOriginalRRN +
                ", allowCashBack=" + allowCashBack +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(loyCard, allowAuthorize, allowPreAuthorize, allowRefund, fullAmountOnly, noDiscounts, reqPANL4Digit,
                reqOriginalData, reqOriginalRRN, allowCashBack);
    }

    public static CardFlagsBuilder builder() {
        return new CardFlagsBuilder();
    }

    public static class CardFlagsBuilder {
        private boolean loyCard;
        private boolean allowAuthorize;
        private boolean allowPreAuthorize;
        private boolean allowRefund;
        private boolean fullAmountOnly;
        private boolean noDiscounts;
        private boolean reqPANL4Digit;
        private boolean reqOriginalData;
        private boolean reqOriginalRRN;
        private boolean allowCashBack;

        CardFlagsBuilder() {
        }

        public CardFlagsBuilder loyCard(boolean loyCard) {
            this.loyCard = loyCard;
            return this;
        }

        public CardFlagsBuilder allowAuthorize(boolean allowAuthorize) {
            this.allowAuthorize = allowAuthorize;
            return this;
        }

        public CardFlagsBuilder allowPreAuthorize(boolean allowPreAuthorize) {
            this.allowPreAuthorize = allowPreAuthorize;
            return this;
        }

        public CardFlagsBuilder allowRefund(boolean allowRefund) {
            this.allowRefund = allowRefund;
            return this;
        }

        public CardFlagsBuilder fullAmountOnly(boolean fullAmountOnly) {
            this.fullAmountOnly = fullAmountOnly;
            return this;
        }

        public CardFlagsBuilder noDiscounts(boolean noDiscounts) {
            this.noDiscounts = noDiscounts;
            return this;
        }

        public CardFlagsBuilder reqPANL4Digit(boolean reqPANL4Digit) {
            this.reqPANL4Digit = reqPANL4Digit;
            return this;
        }

        public CardFlagsBuilder reqOriginalData(boolean reqOriginalData) {
            this.reqOriginalData = reqOriginalData;
            return this;
        }

        public CardFlagsBuilder reqOriginalRRN(boolean reqOriginalRRN) {
            this.reqOriginalRRN = reqOriginalRRN;
            return this;
        }

        public CardFlagsBuilder allowCashBack(boolean allowCashBack) {
            this.allowCashBack = allowCashBack;
            return this;
        }

        public CardFlags build() {
            return new CardFlags(loyCard,
                    allowAuthorize,
                    allowPreAuthorize,
                    allowRefund,
                    fullAmountOnly,
                    noDiscounts,
                    reqPANL4Digit,
                    reqOriginalData,
                    reqOriginalRRN,
                    allowCashBack);
        }
    }
}
