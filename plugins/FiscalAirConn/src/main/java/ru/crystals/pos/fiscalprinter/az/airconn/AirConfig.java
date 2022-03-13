package ru.crystals.pos.fiscalprinter.az.airconn;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AirConfig {

    @JsonProperty("tokenAddress")
    private String tokenAddress;

    @JsonProperty("pin")
    private String pin;

    @JsonProperty("userRole")
    private String userRole;


    @JsonProperty("cashRegisterFactoryNumber")
    private String cashRegisterFactoryNumber;

    @JsonProperty("auditUrl")
    private String auditUrl = "/audit";

    @JsonProperty("auditPort")
    private int auditPort = 6304;

    @JsonProperty("auditPath")
    private String auditPath = "fiscal";

    @JsonProperty("tokenReplacement")
    private String tokenReplacement;


    public String getTokenAddress() {
        return tokenAddress;
    }

    public void setTokenAddress(String tokenAddress) {
        this.tokenAddress = tokenAddress;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getCashRegisterFactoryNumber() {
        return cashRegisterFactoryNumber;
    }

    public void setCashRegisterFactoryNumber(String cashRegisterFactoryNumber) {
        this.cashRegisterFactoryNumber = cashRegisterFactoryNumber;
    }

    public String getAuditUrl() {
        return auditUrl;
    }

    public void setAuditUrl(String auditUrl) {
        this.auditUrl = auditUrl;
    }

    public int getAuditPort() {
        return auditPort;
    }

    public void setAuditPort(int auditPort) {
        this.auditPort = auditPort;
    }

    public String getAuditPath() {
        return auditPath;
    }

    public void setAuditPath(String auditPath) {
        this.auditPath = auditPath;
    }

    public String getTokenReplacement() {
        return tokenReplacement;
    }

    public void setTokenReplacement(String tokenReplacement) {
        this.tokenReplacement = tokenReplacement;
    }
}
