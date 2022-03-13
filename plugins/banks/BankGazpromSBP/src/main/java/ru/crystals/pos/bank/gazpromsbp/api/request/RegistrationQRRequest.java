package ru.crystals.pos.bank.gazpromsbp.api.request;

import com.fasterxml.jackson.annotation.JsonGetter;

public class RegistrationQRRequest {

    /**
     * Счет юридического лица
     */
    private String account;

    /**
     * Идентификатор ТСП
     */
    private String merchantId;

    /**
     * Версия payload QR кода
     * По документации имеет только одно значение
     */
    private String templateVersion;

    /**
     * Тип QR кода:
     * 01 - QR-Static
     * 02 - QR-Dynamic
     */
    private String qrcType;

    /**
     * Сумма операции в копейках
     */
    private String amount;

    /**
     * Валюта операции:
     * RUB - Российский рубль
     */
    private String currency;

    public RegistrationQRRequest(String account, String merchantId, String amount) {
        this.account = account;
        this.merchantId = merchantId;
        this.qrcType = "02";
        this.amount = amount;
        this.currency = "RUB";
        this.templateVersion = "01";
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
    public String getTemplateVersion() {
        return templateVersion;
    }

    @JsonGetter
    public String getQrcType() {
        return qrcType;
    }

    @JsonGetter
    public String getAmount() {
        return amount;
    }

    @JsonGetter
    public String getCurrency() {
        return currency;
    }
}
