package ru.crystals.pos.bank.raiffeisensbp.api.request;


import com.fasterxml.jackson.annotation.JsonGetter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Запрос на регистрацию QR кода
 */
public class RegistrationQRRequest {

    /**
     * Счет юридического лица
     */
    private String account;

    /**
     * Дополнительная информация, заполняемая по желанию партнёра при генерации QR-кода.
     * Может быть доступна для пользователя в зависимости от банка. (<= 140 characters)
     */
    private String additionalInfo;

    /**
     * Сумма в рублях. Обязательно для заполнения, если Тип QR = QRDynamic.
     */
    private BigDecimal amount;

    /**
     * Время формирования заявки
     */
    private OffsetDateTime createDate;

    /**
     * Валюта платежа. Обязательно для заполнения, если заполнена сумма.
     */
    private String currency;

    /**
     * Уникальный идентификатор заказа в системе партнёра
     */
    private String order;

    /**
     * Назначение платежа. Необязательно для заполнения. (<= 185 characters)
     */
    private String paymentDetails;

    /**
     * Тип QR-кода
     */
    private QRType qrType;

    /**
     * Опциональный параметр для указания срока действия QR-кода.
     * При заполнении не может быть меньше текущей даты и времени.
     * После истечения срока действия QR-кода оплату по нему провести нельзя.
     */
    private OffsetDateTime qrExpirationDate;

    /**
     * Идентификатор зарегистрированного партнёра в СБП
     */
    private String sbpMerchantId;

    @JsonGetter
    public String getAccount() {
        return account;
    }

    @JsonGetter
    public String getAdditionalInfo() {
        return additionalInfo;
    }

    @JsonGetter
    public BigDecimal getAmount() {
        return amount;
    }

    @JsonGetter
    public OffsetDateTime getCreateDate() {
        return createDate;
    }

    @JsonGetter
    public String getCurrency() {
        return currency;
    }

    @JsonGetter
    public String getOrder() {
        return order;
    }

    @JsonGetter
    public String getPaymentDetails() {
        return paymentDetails;
    }

    @JsonGetter
    public QRType getQrType() {
        return qrType;
    }

    @JsonGetter
    public OffsetDateTime getQrExpirationDate() {
        return qrExpirationDate;
    }

    @JsonGetter
    public String getSbpMerchantId() {
        return sbpMerchantId;
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
                Objects.equals(additionalInfo, that.additionalInfo) &&
                amount != null && amount.compareTo(that.amount) == 0 &&
                Objects.equals(createDate, that.createDate) &&
                Objects.equals(currency, that.currency) &&
                Objects.equals(order, that.order) &&
                Objects.equals(paymentDetails, that.paymentDetails) &&
                Objects.equals(qrType, that.qrType) &&
                Objects.equals(qrExpirationDate, that.qrExpirationDate) &&
                Objects.equals(sbpMerchantId, that.sbpMerchantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account, additionalInfo, amount, createDate, currency, order, paymentDetails, qrType, qrExpirationDate, sbpMerchantId);
    }

    @Override
    public String toString() {
        return "RegistrationQRRequest{" +
                "account=" + account +
                ", additionalInfo='" + additionalInfo + '\'' +
                ", amount='" + amount + '\'' +
                ", createDate='" + createDate + '\'' +
                ", currency='" + currency + '\'' +
                ", order='" + order + '\'' +
                ", paymentDetails='" + paymentDetails + '\'' +
                ", qrType='" + qrType + '\'' +
                ", qrExpirationDate='" + qrExpirationDate + '\'' +
                ", sbpMerchantId='" + sbpMerchantId + '\'' +
                '}';
    }

    public static RegistrationRequestBuilder builder() {
        return new RegistrationRequestBuilder();
    }

    public static class RegistrationRequestBuilder {

        private RegistrationQRRequest registrationRequest;

        public RegistrationRequestBuilder() {
            registrationRequest = new RegistrationQRRequest();
        }

        public RegistrationRequestBuilder setAccount(String account) {
            registrationRequest.account = account;
            return this;
        }

        public RegistrationRequestBuilder setAdditionalInfo(String additionalInfo) {
            registrationRequest.additionalInfo = additionalInfo;
            return this;
        }

        public RegistrationRequestBuilder setAmount(BigDecimal amount) {
            registrationRequest.amount = amount;
            return this;
        }

        public RegistrationRequestBuilder setCreateDate(OffsetDateTime createDate) {
            registrationRequest.createDate = createDate;
            return this;
        }

        public RegistrationRequestBuilder setCurrency(String currency) {
            registrationRequest.currency = currency;
            return this;
        }

        public RegistrationRequestBuilder setOrder(String order) {
            registrationRequest.order = order;
            return this;
        }

        public RegistrationRequestBuilder setPaymentDetails(String paymentDetails) {
            registrationRequest.paymentDetails = paymentDetails;
            return this;
        }

        public RegistrationRequestBuilder setQrType(QRType qrType) {
            registrationRequest.qrType = qrType;
            return this;
        }

        public RegistrationRequestBuilder setQrExpirationDate(OffsetDateTime qrExpirationDate) {
            registrationRequest.qrExpirationDate = qrExpirationDate;
            return this;
        }

        public RegistrationRequestBuilder setSbpMerchantId(String sbpMerchantId) {
            registrationRequest.sbpMerchantId = sbpMerchantId;
            return this;
        }

        public RegistrationQRRequest build() {
            return registrationRequest;
        }

    }

}
