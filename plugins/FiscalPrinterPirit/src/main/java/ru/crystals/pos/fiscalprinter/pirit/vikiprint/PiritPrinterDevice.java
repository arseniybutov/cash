package ru.crystals.pos.fiscalprinter.pirit.vikiprint;

//TODO надо вынести выше и реализоать работу с ним для всех пиритов
public class PiritPrinterDevice {
    private final boolean isFiscalized;
    private String port;
    private final String deviceName;
    private boolean isFiscal;

    public PiritPrinterDevice(String portName, String deviceName, boolean isFiscal, boolean isFiscalized) {
        this.port = portName;
        this.deviceName = deviceName;
        this.isFiscal = isFiscal;
        this.isFiscalized = isFiscalized;
    }

    public String getPort() {
        return port;
    }

    public boolean isFiscal() {
        return isFiscal;
    }

    public boolean isNonFiscal() {
        return !isFiscal;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public boolean isFiscalized() {
        return isFiscalized;
    }
}
