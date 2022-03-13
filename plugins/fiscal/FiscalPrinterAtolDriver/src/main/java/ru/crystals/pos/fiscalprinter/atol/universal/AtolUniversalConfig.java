package ru.crystals.pos.fiscalprinter.atol.universal;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AtolUniversalConfig {

    @JsonProperty("port")
    private String port;

    @JsonProperty("baudRate")
    private String baudRate = "115200";

    @JsonProperty("invertDrawerState")
    private boolean invertDrawerState;
    /**
     * Масштабирование QR-кода при печати: каждая точка оригинального QR-кода будет увеличена в это количество раз
     */
    @JsonProperty("qrCodeScaleFactor")
    private int qrCodeScaleFactor = 8;

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(String baudRate) {
        this.baudRate = baudRate;
    }

    public boolean isInvertDrawerState() {
        return invertDrawerState;
    }

    public void setInvertDrawerState(boolean invertDrawerState) {
        this.invertDrawerState = invertDrawerState;
    }

    public int getQrCodeScaleFactor() {
        return qrCodeScaleFactor;
    }

    public void setQrCodeScaleFactor(int qrCodeScaleFactor) {
        this.qrCodeScaleFactor = qrCodeScaleFactor;
    }

    @Override
    public String toString() {
        return "AtolUniversalConfig{" +
                "port='" + port + '\'' +
                ", baudRate='" + baudRate + '\'' +
                ", invertDrawerState=" + invertDrawerState +
                ", qrCodeScaleFactor=" + qrCodeScaleFactor +
                '}';
    }
}
