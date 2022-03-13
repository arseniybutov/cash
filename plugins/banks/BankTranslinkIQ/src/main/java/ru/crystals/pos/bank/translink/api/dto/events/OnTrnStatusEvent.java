package ru.crystals.pos.bank.translink.api.dto.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.bank.translink.api.dto.AmountAdditional;
import ru.crystals.pos.bank.translink.api.dto.TrnState;

import java.util.List;
import java.util.Objects;

/**
 * Событие информирует ECR o изменении статуса выполняемой транзакции.
 */
public class OnTrnStatusEvent implements EventProperties {

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

    private final String text;

    @JsonCreator
    public OnTrnStatusEvent(@JsonProperty("operationId") String operationId,
                            @JsonProperty("amountAuthorized") long amountAuthorized,
                            @JsonProperty("documentNr") String documentNr,
                            @JsonProperty("cryptogram") String cryptogram,
                            @JsonProperty("authCode") String authCode,
                            @JsonProperty("RRN") String rrn,
                            @JsonProperty("STAN") String stan,
                            @JsonProperty("cardType") String cardType,
                            @JsonProperty("amountAdditional") List<AmountAdditional> amountAdditional,
                            @JsonProperty("state") TrnState state,
                            @JsonProperty("text") String text) {
        this.operationId = operationId;
        this.amountAuthorized = amountAuthorized;
        this.documentNr = documentNr;
        this.cryptogram = cryptogram;
        this.authCode = authCode;
        this.rrn = rrn;
        this.stan = stan;
        this.cardType = cardType;
        this.amountAdditional = amountAdditional;
        this.state = state;
        this.text = text;
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

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OnTrnStatusEvent that = (OnTrnStatusEvent) o;
        return amountAuthorized == that.amountAuthorized &&
                Objects.equals(operationId, that.operationId) &&
                Objects.equals(documentNr, that.documentNr) &&
                Objects.equals(cryptogram, that.cryptogram) &&
                Objects.equals(authCode, that.authCode) &&
                Objects.equals(rrn, that.rrn) &&
                Objects.equals(stan, that.stan) &&
                Objects.equals(cardType, that.cardType) &&
                Objects.equals(amountAdditional, that.amountAdditional) &&
                state == that.state &&
                Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operationId, amountAuthorized, documentNr, cryptogram, authCode, rrn, stan, cardType, amountAdditional, state, text);
    }

    @Override
    public String toString() {
        return "OnTrnStatusEvent{" +
                "operationId='" + operationId + '\'' +
                ", amountAuthorized=" + amountAuthorized +
                ", documentNr='" + documentNr + '\'' +
                ", cryptogram='" + cryptogram + '\'' +
                ", authCode='" + authCode + '\'' +
                ", rrn='" + rrn + '\'' +
                ", stan='" + stan + '\'' +
                ", cardType='" + cardType + '\'' +
                ", amountAdditional=" + amountAdditional +
                ", state=" + state +
                ", text='" + text + '\'' +
                '}';
    }

    public static OnTrnStatusEventBuilder builder() {
        return new OnTrnStatusEventBuilder();
    }

    public static class OnTrnStatusEventBuilder {
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
        private String text;

        OnTrnStatusEventBuilder() {
        }

        public OnTrnStatusEventBuilder operationId(String operationId) {
            this.operationId = operationId;
            return this;
        }

        public OnTrnStatusEventBuilder amountAuthorized(long amountAuthorized) {
            this.amountAuthorized = amountAuthorized;
            return this;
        }

        public OnTrnStatusEventBuilder documentNr(String documentNr) {
            this.documentNr = documentNr;
            return this;
        }

        public OnTrnStatusEventBuilder cryptogram(String cryptogram) {
            this.cryptogram = cryptogram;
            return this;
        }

        public OnTrnStatusEventBuilder authCode(String authCode) {
            this.authCode = authCode;
            return this;
        }

        public OnTrnStatusEventBuilder rrn(String rrn) {
            this.rrn = rrn;
            return this;
        }

        public OnTrnStatusEventBuilder stan(String stan) {
            this.stan = stan;
            return this;
        }

        public OnTrnStatusEventBuilder cardType(String cardType) {
            this.cardType = cardType;
            return this;
        }

        public OnTrnStatusEventBuilder amountAdditional(List<AmountAdditional> amountAdditional) {
            this.amountAdditional = amountAdditional;
            return this;
        }

        public OnTrnStatusEventBuilder state(TrnState state) {
            this.state = state;
            return this;
        }

        public OnTrnStatusEventBuilder text(String text) {
            this.text = text;
            return this;
        }

        public OnTrnStatusEvent build() {
            return new OnTrnStatusEvent(operationId, amountAuthorized, documentNr, cryptogram, authCode, rrn, stan, cardType, amountAdditional, state, text);
        }
    }
}
