package ru.crystals.pos.fiscalprinter.retailforce;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RetailForceConfig {

    /**
     * eg. http://localhost:7678
     */
    @JsonProperty("url")
    private String url;

    @JsonProperty("identification")
    private String identification;

    @JsonProperty("cloudApiKey")
    private String cloudApiKey;

    @JsonProperty("cloudApiSecret")
    private String cloudApiSecret;

    @JsonProperty("storeNumber")
    private String storeNumber;

    @JsonProperty("terminalNumber")
    private String terminalNumber;

    @JsonProperty("deviceId")
    private String deviceId;

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(final String identification) {
        this.identification = identification;
    }

    public String getCloudApiKey() {
        return cloudApiKey;
    }

    public void setCloudApiKey(final String cloudApiKey) {
        this.cloudApiKey = cloudApiKey;
    }

    public String getCloudApiSecret() {
        return cloudApiSecret;
    }

    public void setCloudApiSecret(final String cloudApiSecret) {
        this.cloudApiSecret = cloudApiSecret;
    }

    public String getStoreNumber() {
        return storeNumber;
    }

    public void setStoreNumber(final String storeNumber) {
        this.storeNumber = storeNumber;
    }

    public String getTerminalNumber() {
        return terminalNumber;
    }

    public void setTerminalNumber(final String terminalNumber) {
        this.terminalNumber = terminalNumber;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String toString() {
        return "RetailForceConfig{" +
                "url='" + url + '\'' +
                ", identification='" + identification + '\'' +
                ", storeNumber='" + storeNumber + '\'' +
                ", terminalNumber='" + terminalNumber + '\'' +
                ", deviceId='" + deviceId + '\'' +
                '}';
    }
}
