package ru.crystals.pos.fiscalprinter.shtrihminifrk.simurg;

public class SimurgConfig {


    private String fiscalPort;
    private int fiscalBaudRate;
    private int fiscalMaxCharRow = 21;

    private String fiscalPositionName = "";
    private String fiscalPositionNameNoNDS = "";

    private boolean mockCounters;

    public String getFiscalPort() {
        return fiscalPort;
    }

    public void setFiscalPort(String fiscalPort) {
        this.fiscalPort = fiscalPort;
    }

    public int getFiscalBaudRate() {
        return fiscalBaudRate;
    }

    public void setFiscalBaudRate(int fiscalBaudRate) {
        this.fiscalBaudRate = fiscalBaudRate;
    }

    public int getFiscalMaxCharRow() {
        return fiscalMaxCharRow;
    }

    public void setFiscalMaxCharRow(int fiscalMaxCharRow) {
        this.fiscalMaxCharRow = fiscalMaxCharRow;
    }

    public String getFiscalPositionName() {
        return fiscalPositionName;
    }

    public void setFiscalPositionName(String fiscalPositionName) {
        this.fiscalPositionName = fiscalPositionName;
    }

    public String getFiscalPositionNameNoNDS() {
        return fiscalPositionNameNoNDS;
    }

    public void setFiscalPositionNameNoNDS(String fiscalPositionNameNoNDS) {
        this.fiscalPositionNameNoNDS = fiscalPositionNameNoNDS;
    }

    public void setMockCounters(boolean mockCounters) {
        this.mockCounters = mockCounters;
    }

    public boolean isMockCounters() {
        return mockCounters;
    }
}
