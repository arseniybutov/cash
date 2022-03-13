package ru.crystals.pos.bank.translink.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Результат выполнения транзакции
 */
public class TransactionResult {

    /**
     * Идентификатор сессии оплаты
     */
    private final String operationId;
    /**
     * Авторизированная сумма платежа, 0 передается, для отклонённой авторизации
     */
    private final long amountAuthorized;
    /**
     * Переданный в запрос номер кассового чека (documentNr)
     */
    private final String documentNr;
    /**
     * Криптограмма транзакции
     */
    private final String cryptogram;
    /**
     * Код авторизации, полученный от процессингового центра
     */
    private final String authCode;
    /**
     * RRN значение присутствует в случае  предоставления процессинговом центром
     */
    private final String rrn;
    /**
     * STAN значение присутствует в случае  предоставления процессинговым центром
     */
    private final String stan;
    /**
     * Название карты
     */
    private final String cardType;
    /**
     * Предназначено для передачи информации о дополнительных суммах: (чаевые, DCC (Dynamic Currency Convertor))
     */
    private final List<AmountAdditional> amountAdditional;
    /**
     * Статус платежной транзакции
     */
    private final TrnState state;
    /**
     * Результат выполнения команды
     */
    private final Result result;


    @JsonCreator
    public TransactionResult(@JsonProperty("operationId") String operationId,
                             @JsonProperty("amountAuthorized") long amountAuthorized,
                             @JsonProperty("documentNr") String documentNr,
                             @JsonProperty("cryptogram") String cryptogram,
                             @JsonProperty("authCode") String authCode,
                             @JsonProperty("RRN") String rrn,
                             @JsonProperty("STAN") String stan,
                             @JsonProperty("cardType") String cardType,
                             @JsonProperty("amountAdditional") List<AmountAdditional> amountAdditional,
                             @JsonProperty("state") TrnState state,
                             @JsonProperty("result") Result result) {
        this.operationId = operationId;
        this.amountAuthorized = amountAuthorized;
        this.documentNr = documentNr;
        this.cryptogram = cryptogram;
        this.authCode = authCode;
        this.rrn = rrn;
        this.stan = stan;
        this.cardType = cardType;
        this.amountAdditional = amountAdditional != null ? amountAdditional : Collections.emptyList();
        this.state = state;
        this.result = result;
    }

    public String getOperationId() {
        return operationId;
    }

    public long getAmountAuthorized() {
        return amountAuthorized;
    }

    public String getDocumentNr() {
        return documentNr;
    }

    public String getCryptogram() {
        return cryptogram;
    }

    public String getAuthCode() {
        return authCode;
    }

    public String getRrn() {
        return rrn;
    }

    public String getStan() {
        return stan;
    }

    public String getCardType() {
        return cardType;
    }

    public List<AmountAdditional> getAmountAdditional() {
        return amountAdditional;
    }

    public TrnState getState() {
        return state;
    }

    public Result getResult() {
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TransactionResult that = (TransactionResult) o;
        return amountAuthorized == that.amountAuthorized &&
                Objects.equals(operationId, that.operationId) &&
                Objects.equals(documentNr, that.documentNr) &&
                Objects.equals(cryptogram, that.cryptogram) &&
                Objects.equals(authCode, that.authCode) &&
                Objects.equals(rrn, that.rrn) &&
                Objects.equals(stan, that.stan) &&
                Objects.equals(cardType, that.cardType) &&
                Objects.equals(amountAdditional, that.amountAdditional) &&
                Objects.equals(state, that.state) &&
                Objects.equals(result, that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operationId, amountAuthorized, documentNr, cryptogram, authCode,
                rrn, stan, cardType, amountAdditional, state, result);
    }

    @Override
    public String toString() {
        return "TransactionResult{" +
                "operationId='" + operationId + '\'' +
                ", amountAuthorized=" + amountAuthorized +
                ", documentNr='" + documentNr + '\'' +
                ", cryptogram='" + cryptogram + '\'' +
                ", authCode='" + authCode + '\'' +
                ", rrn='" + rrn + '\'' +
                ", stan='" + stan + '\'' +
                ", cardType='" + cardType + '\'' +
                ", amountAdditional=" + amountAdditional +
                ", state='" + state + '\'' +
                ", result=" + result +
                '}';
    }

    public static TransactionResultBuilder builder() {
        return new TransactionResultBuilder();
    }

    public static class TransactionResultBuilder {
        private String operationId;
        private long amountAuthorized;
        private String documentNr;
        private String cryptogram;
        private String authCode;
        private String rrn;
        private String stan;
        private String cardType;
        private List<AmountAdditional> amountAdditional;
        private TrnState state;
        private Result result;

        TransactionResultBuilder() {
        }

        public TransactionResultBuilder operationId(String operationId) {
            this.operationId = operationId;
            return this;
        }

        public TransactionResultBuilder amountAuthorized(long amountAuthorized) {
            this.amountAuthorized = amountAuthorized;
            return this;
        }

        public TransactionResultBuilder documentNr(String documentNr) {
            this.documentNr = documentNr;
            return this;
        }

        public TransactionResultBuilder cryptogram(String cryptogram) {
            this.cryptogram = cryptogram;
            return this;
        }

        public TransactionResultBuilder authCode(String authCode) {
            this.authCode = authCode;
            return this;
        }

        public TransactionResultBuilder rrn(String rrn) {
            this.rrn = rrn;
            return this;
        }

        public TransactionResultBuilder stan(String stan) {
            this.stan = stan;
            return this;
        }

        public TransactionResultBuilder cardType(String cardType) {
            this.cardType = cardType;
            return this;
        }

        public TransactionResultBuilder amountAdditional(List<AmountAdditional> amountAdditional) {
            this.amountAdditional = amountAdditional;
            return this;
        }

        public TransactionResultBuilder state(TrnState state) {
            this.state = state;
            return this;
        }

        public TransactionResultBuilder result(Result result) {
            this.result = result;
            return this;
        }

        public TransactionResult build() {
            return new TransactionResult(operationId, amountAuthorized, documentNr, cryptogram, authCode, rrn, stan, cardType, amountAdditional, state, result);
        }
    }
}
