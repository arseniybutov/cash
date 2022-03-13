package ru.crystals.pos.bank.gazpromsbp;

import ru.crystals.pos.bank.commonsbpprovider.SBPProviderConfig;

public class GazpromSBPConfig implements SBPProviderConfig {

    /**
     * Адрес службы
     */
    private GazpromSBPUrl url = GazpromSBPUrl.PRODUCTION;


    /**
     * Значение времени действия QR-кода в минутах
     */
    private Long qrExpiration;

    /**
     * Пароль для терминала
     */
    private String password;

    /**
     * Наименование ТСП (номер мазазина и/или номер кассы)
     */
    private String brandName;

    /**
     * Идентификатор сети магазинов
     */
    private String sbpMerchantId;

    /**
     * Идентификатор юридического лица
     */
    private String legalId;

    /**
     * Счет юридического лица
     */
    private String account;

    @Override
    public String getSbpMerchantId() {
        return sbpMerchantId;
    }

    @Override
    public Long getQrExpiration() {
        return qrExpiration;
    }

    @Override
    public String getUrl() {
        return url.getValue();
    }

    public String getPassword() {
        return password;
    }

    public String getBrandName() {
        return brandName;
    }

    public String getLegalId() {
        return legalId;
    }

    public String getAccount() {
        return account;
    }

    @Override
    public void setUrl(String url) {
        if (url == null){
            this.url = GazpromSBPUrl.PRODUCTION;
        } else {
            try {
                this.url = GazpromSBPUrl.valueOf(url);
            } catch (IllegalArgumentException e) {
                this.url = GazpromSBPUrl.PRODUCTION;
            }
        }
    }

    public void setQrExpiration(Long qrExpiration) {
        this.qrExpiration = qrExpiration;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public void setSbpMerchantId(String sbpMerchantId) {
        this.sbpMerchantId = sbpMerchantId;
    }

    public void setLegalId(String legalId) {
        this.legalId = legalId;
    }

    public void setAccount(String account) {
        this.account = account;
    }
}
