package ru.crystals.pos.bank.tinkoffsbp;

import ru.crystals.pos.bank.commonsbpprovider.SBPProviderConfig;

import java.util.Objects;

public class TinkoffSBPConfig implements SBPProviderConfig {
    /**
     * Адрес службы
     */
    private TinkoffSBPUrl url = TinkoffSBPUrl.PRODUCTION;

    /**
     * Идентификатор терминала
     */
    private String terminalKey;

    /**
     * Используется для генерации токена
     */
    private String password;

    /**
     * Значение времени действия QR-кода в минутах
     */
    private Long qrExpiration;

    /**
     * Идентификатор зарегистрированного партнёра в СБП
     */
    private String sbpMerchantId;

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
            this.url = TinkoffSBPUrl.PRODUCTION;
        } else {
            try {
                this.url = TinkoffSBPUrl.valueOf(url);
            } catch (IllegalArgumentException e) {
                this.url = TinkoffSBPUrl.PRODUCTION;
            }
        }
    }

    public void setUrl(TinkoffSBPUrl url) {
        this.url = url;
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
    public int getMaxNumberOfRetries() {
        return maxNumberOfRetries;
    }

    public void setMaxNumberOfRetries(String maxNumberOfRetries) {
        this.maxNumberOfRetries = Integer.parseInt(maxNumberOfRetries);
    }

    public String getTerminalKey() {
        return terminalKey;
    }

    public void setTerminalKey(String terminalKey) {
        this.terminalKey = terminalKey;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
