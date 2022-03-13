package ru.crystals.pos.bank.opensbp.api.request;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.bank.commonsbpprovider.api.request.RegistrationQRRequestDTO;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Запрос на регистрацию QR кода
 */
public class RegistrationQRRequest {
    /**
     * Счет юридического лица
     */
    @JsonProperty("account")
    private String account;

    /**
     * Идентификатор ТСП
     */
    @JsonProperty("merchantId")
    private String merchantId;

    /**
     * Версия payload QR кода
     */
    @JsonProperty("templateVersion")
    private TemplateVersion templateVersion;

    /**
     * Тип QR-кода
     */
    @JsonProperty("qrcType")
    private QrcType qrcType;

    /**
     * Сумма в рублях. Обязательно для заполнения, если Тип QR = QRDynamic.
     */
    @JsonProperty("amount")
    private long amount;

    /**
     * Валюта платежа. Обязательно для заполнения, если заполнена сумма.
     */
    @JsonProperty("currency")
    private String currency;

    /**
     * Дополнительная информация от торговой точки для покупателя (опциональное).
     * Будет демонстрироваться покупателю на экране смартфона после сканирования QR-кода.
     * Максимальная длина - 140 символов.
     */
    @JsonProperty("paymentPurpose")
    private String paymentPurpose;

    /**
     * Произвольные параметры
     */
    @JsonProperty("params")
    private Params params;

    /**
     * Время формирования заявки
     */
    @JsonProperty("createDate")
    private OffsetDateTime createDate;

    /**
     * Опциональный параметр для указания срока действия QR-кода.
     * При заполнении не может быть меньше текущей даты и времени.
     * После истечения срока действия QR-кода оплату по нему провести нельзя.
     */
    @JsonProperty("qrExpirationDate")
    private OffsetDateTime qrExpirationDate;

    /**
     * Уникальный идентификатор заказа в системе партнёра
     */
    @JsonProperty("order")
    private String order;

    public RegistrationQRRequest(RegistrationQRRequestDTO registrationQRRequestDTO) {
        this.order = registrationQRRequestDTO.getOrderId();
        this.amount = registrationQRRequestDTO.getAmount();
        this.currency = registrationQRRequestDTO.getCurrency();
        this.createDate = registrationQRRequestDTO.getCreateDate();
        this.qrExpirationDate = registrationQRRequestDTO.getQrExpirationDate();
        this.merchantId = registrationQRRequestDTO.getSbpMerchantId();
    }

    public RegistrationQRRequest() {
    }

    @JsonGetter
    public String getAccount() {
        return account;
    }

    @JsonGetter
    public String getMerchantId() {
        return merchantId;
    }

    @JsonGetter
    public TemplateVersion getTemplateVersion() {
        return templateVersion;
    }

    @JsonGetter
    public QrcType getQrcType() {
        return qrcType;
    }

    @JsonGetter
    public long getAmount() {
        return amount;
    }

    @JsonGetter
    public String getCurrency() {
        return currency;
    }

    @JsonGetter
    public String getPaymentPurpose() {
        return paymentPurpose;
    }

    @JsonGetter
    public Params getParams() {
        return params;
    }

    @JsonGetter
    public OffsetDateTime getCreateDate() {
        return createDate;
    }

    @JsonGetter
    public OffsetDateTime getQrExpirationDate() {
        return qrExpirationDate;
    }

    @JsonGetter
    public String getOrder() {
        return order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RegistrationQRRequest that = (RegistrationQRRequest) o;
        return Objects.equals(account, that.account) &&
                Objects.equals(merchantId, that.merchantId) &&
                Objects.equals(templateVersion, that.templateVersion) &&
                Objects.equals(qrcType, that.qrcType) &&
                amount == that.amount &&
                Objects.equals(currency, that.currency) &&
                Objects.equals(paymentPurpose, that.paymentPurpose) &&
                Objects.equals(params, that.params) &&
                Objects.equals(createDate, that.createDate) &&
                Objects.equals(order, that.order) &&
                Objects.equals(qrExpirationDate, that.qrExpirationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account, merchantId, templateVersion, qrcType, amount, currency, paymentPurpose, params, createDate, qrExpirationDate, order);
    }

    @Override
    public String toString() {
        return "RegistrationQRRequest{" +
                "account='" + account + '\'' +
                ", merchantId='" + merchantId + '\'' +
                ", templateVersion=" + templateVersion + '\'' +
                ", qrcType=" + qrcType + '\'' +
                ", amount=" + amount + '\'' +
                ", currency='" + currency + '\'' +
                ", paymentPurpose='" + paymentPurpose + '\'' +
                ", params=" + params + '\'' +
                ", createDate='" + createDate + '\'' +
                ", order='" + order + '\'' +
                ", qrExpirationDate='" + qrExpirationDate +
                '}';
    }

    public static RegistrationRequestBuilder builder() {
        return new RegistrationRequestBuilder();
    }

    public static RegistrationRequestBuilder builder(RegistrationQRRequest registrationQRRequest) {
        return new RegistrationRequestBuilder(registrationQRRequest);
    }

    public static class RegistrationRequestBuilder {

        private RegistrationQRRequest registrationRequest;

        public RegistrationRequestBuilder() {
            registrationRequest = new RegistrationQRRequest();
        }

        public RegistrationRequestBuilder(RegistrationQRRequest registrationQRRequest) {
            this.registrationRequest = registrationQRRequest;
        }

        public RegistrationRequestBuilder setAccount(String account) {
            registrationRequest.account = account;
            return this;
        }

        public RegistrationRequestBuilder setMerchantId(String merchantId) {
            registrationRequest.merchantId = merchantId;
            return this;
        }

        public RegistrationRequestBuilder setTemplateVersion(TemplateVersion templateVersion) {
            registrationRequest.templateVersion = templateVersion;
            return this;
        }

        public RegistrationRequestBuilder setQrcType(QrcType qrType) {
            registrationRequest.qrcType = qrType;
            return this;
        }

        public RegistrationRequestBuilder setAmount(long amount) {
            registrationRequest.amount = amount;
            return this;
        }

        public RegistrationRequestBuilder setCurrency(String currency) {
            registrationRequest.currency = currency;
            return this;
        }

        public RegistrationRequestBuilder setPaymentPurpose(String paymentPurpose) {
            registrationRequest.paymentPurpose = paymentPurpose;
            return this;
        }

        public RegistrationRequestBuilder setParams(Params params) {
            registrationRequest.params = params;
            return this;
        }

        public RegistrationRequestBuilder setCreateDate(OffsetDateTime createDate) {
            registrationRequest.createDate = createDate;
            return this;
        }

        public RegistrationRequestBuilder setQrExpirationDate(OffsetDateTime qrExpirationDate) {
            registrationRequest.qrExpirationDate = qrExpirationDate;
            return this;
        }

        public RegistrationRequestBuilder setOrder(String order) {
            registrationRequest.order = order;
            return this;
        }

        public RegistrationQRRequest build() {
            return registrationRequest;
        }

    }
}
