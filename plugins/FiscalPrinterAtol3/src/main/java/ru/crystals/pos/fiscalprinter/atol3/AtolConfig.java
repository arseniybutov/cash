package ru.crystals.pos.fiscalprinter.atol3;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AtolConfig {
    @JsonProperty("port")
    private String port;

    @JsonProperty("baudRate")
    private int baudRate = 115200;

    @JsonProperty("useFlowControl")
    private boolean useFlowControl;

    /**
     * Cостояние денежного ящика (для некоторых моделей денежных ящиков может быть инвертировано)
     */
    @JsonProperty("invertDrawerState")
    private boolean invertDrawerState;

    /**
     * Масштабирование QR-кода при печати: каждая точка оригинального QR-кода будет увеличена в это количество раз
     */
    @JsonProperty("qrCodeScaleFactor")
    private int qrCodeScaleFactor = 8;

    @JsonProperty("serviceExec")
    private String serviceExec = "/home/tc/storage/3rd-party/atol/EthOverUsb.sh";

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }

    public boolean isUseFlowControl() {
        return useFlowControl;
    }

    public void setUseFlowControl(boolean useFlowControl) {
        this.useFlowControl = useFlowControl;
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

    public String getServiceExec() {
        return serviceExec;
    }

    public void setServiceExec(String serviceExec) {
        this.serviceExec = serviceExec;
    }
}
