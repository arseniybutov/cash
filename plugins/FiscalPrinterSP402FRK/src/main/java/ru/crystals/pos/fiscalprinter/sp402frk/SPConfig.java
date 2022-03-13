package ru.crystals.pos.fiscalprinter.sp402frk;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SPConfig {

    @JsonProperty("port")
    private String port = "COM1";

    @JsonProperty("baudRate")
    private int baudRate = 115200;

    @JsonProperty("useFlowControl")
    private boolean useFlowControl;

    /**
     * Печатать наименование товара
     */
    @JsonProperty("printGoodsName")
    private boolean printGoodsName = true;


    @JsonProperty("defaultGoodsName")
    private String defaultGoodsName = "-----";


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

    public boolean isPrintGoodsName() {
        return printGoodsName;
    }

    public void setPrintGoodsName(boolean printGoodsName) {
        this.printGoodsName = printGoodsName;
    }

    public String getDefaultGoodsName() {
        return defaultGoodsName;
    }

    public void setDefaultGoodsName(String defaultGoodsName) {
        this.defaultGoodsName = defaultGoodsName;
    }
}
