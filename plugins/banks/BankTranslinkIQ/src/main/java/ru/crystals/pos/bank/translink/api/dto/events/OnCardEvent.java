package ru.crystals.pos.bank.translink.api.dto.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.List;
import java.util.Objects;

/**
 * Событие информирует ECR, о том что POS считал информацию о предоставленной карте.
 * <p>
 * В случае, когда не установлен флаг AllowAuthorize означает, что карта является дисконтной картой, либо картой лояльности.
 */
public class OnCardEvent implements EventProperties {
    /**
     * Хешированное значение PAN.
     */
    @JsonProperty("hash")
    private String hash;

    /**
     * Track1, Track2, Track3 и основной номер карты (PAN) или эквивалентные данные, считанные с карты, если чтение данных параметров разрешаемо политикой карты. Поля
     * Track1, Track2 и Track2 всегда отсутствуют для банковских карт. PAN может передаваться виде маскированного номера карточки.
     */
    @JsonProperty("track1")
    private String track1;

    @JsonProperty("track2")
    private String track2;

    @JsonProperty("track3")
    private String track3;

    /**
     * Основной номер карты (PAN) или эквивалентные данные, считанные с карты. PAN может передаваться виде маскированного номера карточки.
     */
    @JsonProperty("PAN")
    private String pan;

    /**
     * Тип карты
     */
    @JsonProperty("cardType")
    private String cardType;

    /**
     * Код основной валюты карточки по стандарту ISO 4217.
     */
    @JsonProperty("currencyCode")
    private String currencyCode;

    @JsonProperty("additionalCurrencyCodes")
    private List<String> additionalCurrencyCodes;

    /**
     * Возможности при использовании карт
     */
    @JsonUnwrapped
    private CardFlags flags;

    /**
     * Возможности при использовании карт, касающиеся рассрочки
     */
    @JsonUnwrapped
    private InstallmentCardFlags installmentFlags;

    public OnCardEvent() {
    }

    public OnCardEvent(String hash,
                       String track1,
                       String track2,
                       String track3,
                       String pan,
                       String cardType,
                       String currencyCode,
                       List<String> additionalCurrencyCodes,
                       CardFlags flags,
                       InstallmentCardFlags installmentFlags) {
        this.hash = hash;
        this.track1 = track1;
        this.track2 = track2;
        this.track3 = track3;
        this.pan = pan;
        this.cardType = cardType;
        this.currencyCode = currencyCode;
        this.additionalCurrencyCodes = additionalCurrencyCodes;
        this.flags = flags;
        this.installmentFlags = installmentFlags;
    }

    public static OnCardEventBuilder builder() {
        return new OnCardEventBuilder();
    }

    public String getHash() {
        return hash;
    }

    public String getTrack1() {
        return track1;
    }

    public String getTrack2() {
        return track2;
    }

    public String getTrack3() {
        return track3;
    }

    public String getPan() {
        return pan;
    }

    public String getCardType() {
        return cardType;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public List<String> getAdditionalCurrencyCodes() {
        return additionalCurrencyCodes;
    }

    public CardFlags getFlags() {
        return flags;
    }

    public InstallmentCardFlags getInstallmentFlags() {
        return installmentFlags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OnCardEvent that = (OnCardEvent) o;
        return Objects.equals(hash, that.hash) &&
                Objects.equals(track1, that.track1) &&
                Objects.equals(track2, that.track2) &&
                Objects.equals(track3, that.track3) &&
                Objects.equals(pan, that.pan) &&
                Objects.equals(cardType, that.cardType) &&
                Objects.equals(currencyCode, that.currencyCode) &&
                Objects.equals(additionalCurrencyCodes, that.additionalCurrencyCodes) &&
                Objects.equals(flags, that.flags) &&
                Objects.equals(installmentFlags, that.installmentFlags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash, track1, track2, track3, pan, cardType, currencyCode, additionalCurrencyCodes, flags, installmentFlags);
    }

    @Override
    public String toString() {
        return "OnCardEvent{" +
                "hash='" + hash + '\'' +
                ", track1='" + track1 + '\'' +
                ", track2='" + track2 + '\'' +
                ", track3='" + track3 + '\'' +
                ", pan='" + pan + '\'' +
                ", cardType='" + cardType + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                ", additionalCurrencyCodes=" + additionalCurrencyCodes +
                ", flags=" + flags +
                ", installmentCardFlags=" + installmentFlags +
                '}';
    }

    public static class OnCardEventBuilder {
        private String hash;
        private String track1;
        private String track2;
        private String track3;
        private String pan;
        private String cardType;
        private String currencyCode;
        private List<String> additionalCurrencyCodes;
        private CardFlags flags;
        private InstallmentCardFlags installmentFlags;

        OnCardEventBuilder() {
        }

        public OnCardEventBuilder hash(String hash) {
            this.hash = hash;
            return this;
        }

        public OnCardEventBuilder track1(String track1) {
            this.track1 = track1;
            return this;
        }

        public OnCardEventBuilder track2(String track2) {
            this.track2 = track2;
            return this;
        }

        public OnCardEventBuilder track3(String track3) {
            this.track3 = track3;
            return this;
        }

        public OnCardEventBuilder pan(String pan) {
            this.pan = pan;
            return this;
        }

        public OnCardEventBuilder cardType(String cardType) {
            this.cardType = cardType;
            return this;
        }

        public OnCardEventBuilder currencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
            return this;
        }

        public OnCardEventBuilder additionalCurrencyCodes(List<String> additionalCurrencyCodes) {
            this.additionalCurrencyCodes = additionalCurrencyCodes;
            return this;
        }

        public OnCardEventBuilder flags(CardFlags flags) {
            this.flags = flags;
            return this;
        }

        public OnCardEventBuilder installmentCardFlags(InstallmentCardFlags installmentFlags) {
            this.installmentFlags = installmentFlags;
            return this;
        }

        public OnCardEvent build() {
            return new OnCardEvent(hash, track1, track2, track3, pan, cardType, currencyCode, additionalCurrencyCodes, flags, installmentFlags);
        }
    }
}
