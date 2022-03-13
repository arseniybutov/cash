package ru.crystals.pos.fiscalprinter.retailforce.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FiscalClientStatus {
    @JsonProperty("fiscalIdentification")
    private String fiscalIdentification;

    @JsonProperty("vatNumber")
    private String vatNumber;

    @JsonProperty("fiscalCountry")
    private FiscalCountry fiscalCountry;

    @JsonProperty("fiscalSoftwareVersion")
    private String fiscalSoftwareVersion;

    @JsonProperty("online")
    private boolean online;

    @JsonProperty("cloudMessagesQueued")
    private int cloudMessagesQueued;

    @JsonProperty("state")
    private FiscalClientState state;

    @JsonProperty("storeNumber")
    private String storeNumber;

    @JsonProperty("terminalNumber")
    private String terminalNumber;

    @JsonProperty("alert")
    private boolean alert;

    public String getFiscalIdentification() {
        return fiscalIdentification;
    }

    public void setFiscalIdentification(final String fiscalIdentification) {
        this.fiscalIdentification = fiscalIdentification;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(final String vatNumber) {
        this.vatNumber = vatNumber;
    }

    public FiscalCountry getFiscalCountry() {
        return fiscalCountry;
    }

    public void setFiscalCountry(final FiscalCountry fiscalCountry) {
        this.fiscalCountry = fiscalCountry;
    }

    public String getFiscalSoftwareVersion() {
        return fiscalSoftwareVersion;
    }

    public void setFiscalSoftwareVersion(final String fiscalSoftwareVersion) {
        this.fiscalSoftwareVersion = fiscalSoftwareVersion;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(final boolean online) {
        this.online = online;
    }

    public int getCloudMessagesQueued() {
        return cloudMessagesQueued;
    }

    public void setCloudMessagesQueued(final int cloudMessagesQueued) {
        this.cloudMessagesQueued = cloudMessagesQueued;
    }

    public FiscalClientState getState() {
        return state;
    }

    public void setState(final FiscalClientState state) {
        this.state = state;
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

    public boolean isAlert() {
        return alert;
    }

    public void setAlert(final boolean alert) {
        this.alert = alert;
    }
}

