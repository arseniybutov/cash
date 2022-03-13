package ru.crystals.pos.fiscalprinter.mstar;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MstarPluginConfig {
    @JsonProperty("port")
    private String port;

    @JsonProperty("baudRate")
    private String baudRate = "115200";

    @JsonProperty("useFlowControl")
    private boolean useFlowControl;

    @JsonProperty("printPosNum")
    private boolean printPosNum = true;

    @JsonProperty("printGoodsName")
    private boolean printGoodsName = true;

    @JsonProperty("printItem")
    private boolean printItem;

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

    public boolean isUseFlowControl() {
        return useFlowControl;
    }

    public void setUseFlowControl(boolean useFlowControl) {
        this.useFlowControl = useFlowControl;
    }

    public boolean isPrintPosNum() {
        return printPosNum;
    }

    public void setPrintPosNum(boolean printPosNum) {
        this.printPosNum = printPosNum;
    }

    public boolean isPrintGoodsName() {
        return printGoodsName;
    }

    public void setPrintGoodsName(boolean printGoodsName) {
        this.printGoodsName = printGoodsName;
    }

    public boolean isPrintItem() {
        return printItem;
    }

    public void setPrintItem(boolean printItem) {
        this.printItem = printItem;
    }
}
