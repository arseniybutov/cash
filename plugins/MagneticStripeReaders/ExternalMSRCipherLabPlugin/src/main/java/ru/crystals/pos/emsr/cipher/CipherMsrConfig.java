package ru.crystals.pos.emsr.cipher;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CipherMsrConfig {

    @JsonProperty("port")
    private String port;

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
