package ru.crystals.pos.bank.raiffeisensbp;

/**
 * Конфигурация
 */
public class SBPConfig {

    /**
     * Адрес службы
     */
    private RaiffeisenSBPURL url = RaiffeisenSBPURL.PRODUCTION;

    /**
     * Секретный ключ
     */
    private String secretKey;

    /**
     * Счет юридического лица
     */
    private String account;

    /**
     * Идентификатор зарегистрированного партнёра в СБП
     */
    private String sbpMerchantId;

    /**
     * Значение времени действия QR-кода в минутах
     */
    private Long qrExpiration;

    /**
     * Назначение платежа
     */
    private String paymentDetails;

    /**
     * Дополнительная информация, заполняемая по желанию партнёра при генерации QR-кода. Может быть доступна для пользователя в зависимости от банка.
     */
    private String additionalInfo;

    public RaiffeisenSBPURL getUrl() {
        return url;
    }

    public void setUrl(String url) {
        try {
            this.url = RaiffeisenSBPURL.valueOf(url);
        } catch (IllegalArgumentException e) {
            this.url = RaiffeisenSBPURL.PRODUCTION;
        }
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getSbpMerchantId() {
        return sbpMerchantId;
    }

    public void setSbpMerchantId(String sbpMerchantId) {
        this.sbpMerchantId = sbpMerchantId;
    }

    public Long getQrExpiration() {
        return qrExpiration;
    }

    public void setQrExpiration(Long qrExpiration) {
        this.qrExpiration = qrExpiration;
    }

    public String getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(String paymentDetails) {
        this.paymentDetails = paymentDetails;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
