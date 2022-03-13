package ru.crystals.pos.bank.translink;

import ru.crystals.pos.bank.TerminalConfiguration;

public class TranslinkConfig {

    private TerminalConfiguration baseConfig;

    private String licenseToken;

    private long cardReadTimeout = 130;
    private long closeDayTimeout = 130;

    public TerminalConfiguration getBaseConfig() {
        return baseConfig;
    }

    public void setBaseConfig(TerminalConfiguration baseConfig) {
        this.baseConfig = baseConfig;
    }

    public String getLicenseToken() {
        return licenseToken;
    }

    public void setLicenseToken(String licenseToken) {
        this.licenseToken = licenseToken;
    }

    public long getCardReadTimeout() {
        return cardReadTimeout;
    }

    public void setCardReadTimeout(long cardReadTimeout) {
        this.cardReadTimeout = cardReadTimeout;
    }

    public long getCloseDayTimeout() {
        return closeDayTimeout;
    }

    public void setCloseDayTimeout(long closeDayTimeout) {
        this.closeDayTimeout = closeDayTimeout;
    }
}
