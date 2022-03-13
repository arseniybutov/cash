package ru.crystals.pos.fiscalprinter.az.airconn.model.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Регистрационные данные AirConn
 */
public class InfoData {
    @JsonProperty("company_tax_number")
    private String companyTaxNumber;
    @JsonProperty("company_name")
    private String companyName;
    @JsonProperty("object_tax_number")
    private String objectTaxNumber;
    @JsonProperty("object_name")
    private String objectName;
    @JsonProperty("object_address")
    private String objectAddress;
    @JsonProperty("cashbox_tax_number")
    private String cashboxTaxNumber;
    @JsonProperty("cashbox_factory_number")
    private String cashboxFactoryNumber;
    @JsonProperty("firmware_version")
    private String firmwareVersion;
    @JsonProperty("cashregister_factory_number")
    private String cashregisterFactoryNumber;
    @JsonProperty("cashregister_model")
    private String cashregisterModel;
    @JsonProperty("qr_code_url")
    private String qrCodeUrl;
    @JsonProperty("not_before")
    private String notBefore;
    @JsonProperty("not_after")
    private String notAfter;
    @JsonProperty("state")
    private String state;
    @JsonProperty("last_online_time")
    private String lastOnlineTime;
    @JsonProperty("oldest_document_time")
    private String oldestDocumentTime;

    public String getCompanyTaxNumber() {
        return companyTaxNumber;
    }

    public void setCompanyTaxNumber(String companyTaxNumber) {
        this.companyTaxNumber = companyTaxNumber;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getObjectTaxNumber() {
        return objectTaxNumber;
    }

    public void setObjectTaxNumber(String objectTaxNumber) {
        this.objectTaxNumber = objectTaxNumber;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getObjectAddress() {
        return objectAddress;
    }

    public void setObjectAddress(String objectAddres) {
        this.objectAddress = objectAddres;
    }

    public String getCashboxTaxNumber() {
        return cashboxTaxNumber;
    }

    public void setCashboxTaxNumber(String cashboxTaxNumber) {
        this.cashboxTaxNumber = cashboxTaxNumber;
    }

    public String getCashboxFactoryNumber() {
        return cashboxFactoryNumber;
    }

    public void setCashboxFactoryNumber(String cashboxFactoryNumber) {
        this.cashboxFactoryNumber = cashboxFactoryNumber;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getCashregisterFactoryNumber() {
        return cashregisterFactoryNumber;
    }

    public void setCashregisterFactoryNumber(String cashregisterFactoryNumber) {
        this.cashregisterFactoryNumber = cashregisterFactoryNumber;
    }

    public String getCashregisterModel() {
        return cashregisterModel;
    }

    public void setCashregisterModel(String cashregisterModel) {
        this.cashregisterModel = cashregisterModel;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public String getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(String notBefore) {
        this.notBefore = notBefore;
    }

    public String getNotAfter() {
        return notAfter;
    }

    public void setNotAfter(String notAfter) {
        this.notAfter = notAfter;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getLastOnlineTime() {
        return lastOnlineTime;
    }

    public void setLastOnlineTime(String lastOnlineTime) {
        this.lastOnlineTime = lastOnlineTime;
    }

    public String getOldestDocumentTime() {
        return oldestDocumentTime;
    }

    public void setOldestDocumentTime(String oldestDocumentTime) {
        this.oldestDocumentTime = oldestDocumentTime;
    }
}
