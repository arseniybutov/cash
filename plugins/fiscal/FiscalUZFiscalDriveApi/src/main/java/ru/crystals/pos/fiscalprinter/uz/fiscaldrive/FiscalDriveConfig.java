package ru.crystals.pos.fiscalprinter.uz.fiscaldrive;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FiscalDriveConfig {

    /**
     * Серийный номер виртуальной кассы
     */
    @JsonProperty("serialNumber")
    private String serialNumber;

    @JsonProperty("url")
    private String url = "http://127.0.0.1:3448/rpc/api";

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
