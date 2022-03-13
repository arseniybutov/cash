package ru.crystals.pos.fiscalprinter.documentprinter.system;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DocumentPrinterSystemConfig {

    @JsonProperty("port")
    private String port;

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
