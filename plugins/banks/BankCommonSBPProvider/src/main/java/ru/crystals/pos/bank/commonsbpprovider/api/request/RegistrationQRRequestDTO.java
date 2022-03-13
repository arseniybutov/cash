package ru.crystals.pos.bank.commonsbpprovider.api.request;


import java.time.OffsetDateTime;

public class RegistrationQRRequestDTO {

    private String orderId;

    /**
     * Сумма оплаты в копейках
     */
    private long amount;

    /**
     * Валюта оплаты
     */
    private String currency;

    /**
     * Дата и время создания операции оплаты
     */
    private OffsetDateTime createDate;

    /**
     * Дата и время просроченности QR кода
     */
    private OffsetDateTime qrExpirationDate;

    /**
     * Идентификатор ТСП
     */
    private String sbpMerchantId;

    /**
     * ID дисконтной карты
     */
    private String discountCardNumber;

    public String getDiscountCardNumber() {
        return discountCardNumber;
    }

    public String getOrderId() {
        return orderId;
    }

    public long getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public OffsetDateTime getCreateDate() {
        return createDate;
    }

    public OffsetDateTime getQrExpirationDate() {
        return qrExpirationDate;
    }

    public String getSbpMerchantId() {
        return sbpMerchantId;
    }

    public static RegistrationRequestDTOBuilder builder() {
        return new RegistrationRequestDTOBuilder();
    }

    public static class RegistrationRequestDTOBuilder {

        private RegistrationQRRequestDTO registrationRequestDTO;

        public RegistrationRequestDTOBuilder() {
            registrationRequestDTO = new RegistrationQRRequestDTO();
        }


        public RegistrationRequestDTOBuilder setAmount(long amount) {
            registrationRequestDTO.amount = amount;
            return this;
        }

        public RegistrationRequestDTOBuilder setCurrency(String currency) {
            registrationRequestDTO.currency = currency;
            return this;
        }


        public RegistrationRequestDTOBuilder setCreateDate(OffsetDateTime createDate) {
            registrationRequestDTO.createDate = createDate;
            return this;
        }

        public RegistrationRequestDTOBuilder setQrExpirationDate(OffsetDateTime qrExpirationDate) {
            registrationRequestDTO.qrExpirationDate = qrExpirationDate;
            return this;
        }

        public RegistrationRequestDTOBuilder setOrderId(String orderId) {
            registrationRequestDTO.orderId = orderId;
            return this;
        }

        public RegistrationRequestDTOBuilder setSbpMerchantId(String sbpMerchantId) {
            registrationRequestDTO.sbpMerchantId = sbpMerchantId;
            return this;
        }

        public RegistrationRequestDTOBuilder setDiscountCardNumber(String discountCardNumber) {
            registrationRequestDTO.discountCardNumber = discountCardNumber;
            return this;
        }


        public RegistrationQRRequestDTO build() {
            return registrationRequestDTO;
        }

    }
}
