package ru.crystals.pos.bank.opensbp;

import ru.crystals.pos.bank.commonsbpprovider.SBPProviderConfig;

import java.util.Objects;

/**
 * Конфигурация
 */
public class OpenSBPConfig implements SBPProviderConfig {

    /**
     * Адрес службы
     */
    private OpenSBPURL url = OpenSBPURL.PRODUCTION;

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
     * Перерыв между повторным запросом
     */
    private long delayInSeconds;

    /**
     * Сколько одинаковых запросов возомжно сделать
     */
    private int maxNumberOfRequest;

    /**
     * Сколько попыток повторного запроса можно сделать при TimeoutException
     */
    private int maxNumberOfRetries;

    @Override
    public String getUrl() {
        return url.getValue();
    }

    @Override
    public void setUrl(String url) {
        if (Objects.isNull(url)){
            this.url = OpenSBPURL.PRODUCTION;
        } else {
            try {
                this.url = OpenSBPURL.valueOf(url);
            } catch (IllegalArgumentException e) {
                this.url = OpenSBPURL.PRODUCTION;
            }
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

    @Override
    public String getSbpMerchantId() {
        return sbpMerchantId;
    }

    public void setSbpMerchantId(String sbpMerchantId) {
        this.sbpMerchantId = sbpMerchantId;
    }

    @Override
    public Long getQrExpiration() {
        return qrExpiration;
    }

    public void setQrExpiration(Long qrExpiration) {
        this.qrExpiration = qrExpiration;
    }

    @Override
    public long getDelayInSeconds() {
        return delayInSeconds;
    }

    public void setDelayInSeconds(String delayInSeconds) {
        this.delayInSeconds = Long.parseLong(delayInSeconds);
    }

    @Override
    public int getMaxNumberOfRequest() {
        return maxNumberOfRequest;
    }

    public void setMaxNumberOfRequest(String maxNumberOfRequest) {
        this.maxNumberOfRequest = Integer.parseInt(maxNumberOfRequest);
    }

    @Override
    public int getMaxNumberOfRetries() {
        return maxNumberOfRetries;
    }

    public void setMaxNumberOfRetries(String maxNumberOfRetries) {
        this.maxNumberOfRetries = Integer.parseInt(maxNumberOfRetries);
    }
}
