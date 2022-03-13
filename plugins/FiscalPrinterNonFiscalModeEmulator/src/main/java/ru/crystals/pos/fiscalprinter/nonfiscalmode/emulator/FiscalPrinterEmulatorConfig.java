package ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FiscalPrinterEmulatorConfig {
    @JsonProperty("manualExceptionPort")
    private int manualExceptionPort = 8890;

    @JsonProperty("printPreviewPort")
    private int printPreviewPort = 8889;

    @JsonProperty("zOnClosedShift")
    private boolean zOnClosedShift;

    @JsonProperty("recreatePrinterFileOnShiftOpen")
    private boolean recreatePrinterFileOnShiftOpen;

    @JsonProperty("showPrinterFrame")
    private boolean showPrinterFrame;

    @JsonProperty("createPrinterFrame")
    private boolean createPrinterFrame;

    @JsonProperty("printPreviewHost")
    private String printPreviewHost = "127.0.0.1";

    @JsonProperty("ofdMode")
    private boolean ofdMode;

    @JsonProperty("maxCharRow")
    private int maxCharRow = 46;

    @JsonProperty("printLineInterval")
    private int printLineInterval = 1;

    @JsonProperty("closeCheckInterval")
    private int closeCheckInterval = 1;

    @JsonProperty("controlTapeMaxSize")
    private int controlTapeMaxSize = 5000;

    @JsonProperty("canReturnFullLastDocInfo")
    private boolean canReturnFullLastDocInfo = true;

    @JsonProperty("canMakeArbitraryRefund")
    private boolean canMakeArbitraryRefund = true;

    public int getManualExceptionPort() {
        return manualExceptionPort;
    }

    public void setManualExceptionPort(int manualExceptionPort) {
        this.manualExceptionPort = manualExceptionPort;
    }

    public int getPrintPreviewPort() {
        return printPreviewPort;
    }

    public void setPrintPreviewPort(int printPreviewPort) {
        this.printPreviewPort = printPreviewPort;
    }

    public boolean iszOnClosedShift() {
        return zOnClosedShift;
    }

    public void setzOnClosedShift(boolean zOnClosedShift) {
        this.zOnClosedShift = zOnClosedShift;
    }

    public boolean isRecreatePrinterFileOnShiftOpen() {
        return recreatePrinterFileOnShiftOpen;
    }

    public void setRecreatePrinterFileOnShiftOpen(boolean recreatePrinterFileOnShiftOpen) {
        this.recreatePrinterFileOnShiftOpen = recreatePrinterFileOnShiftOpen;
    }

    public boolean isShowPrinterFrame() {
        return showPrinterFrame;
    }

    public void setShowPrinterFrame(boolean showPrinterFrame) {
        this.showPrinterFrame = showPrinterFrame;
    }

    public boolean isCreatePrinterFrame() {
        return createPrinterFrame;
    }

    public void setCreatePrinterFrame(boolean createPrinterFrame) {
        this.createPrinterFrame = createPrinterFrame;
    }

    public String getPrintPreviewHost() {
        return printPreviewHost;
    }

    public void setPrintPreviewHost(String printPreviewHost) {
        this.printPreviewHost = printPreviewHost;
    }

    public boolean isOfdMode() {
        return ofdMode;
    }

    public void setOfdMode(boolean ofdMode) {
        this.ofdMode = ofdMode;
    }

    public int getMaxCharRow() {
        return maxCharRow;
    }

    public void setMaxCharRow(int maxCharRow) {
        this.maxCharRow = maxCharRow;
    }

    public int getPrintLineInterval() {
        return printLineInterval;
    }

    public void setPrintLineInterval(int printLineInterval) {
        this.printLineInterval = printLineInterval;
    }

    public int getCloseCheckInterval() {
        return closeCheckInterval;
    }

    public void setCloseCheckInterval(int closeCheckInterval) {
        this.closeCheckInterval = closeCheckInterval;
    }

    public int getControlTapeMaxSize() {
        return controlTapeMaxSize;
    }

    public void setControlTapeMaxSize(int controlTapeMaxSize) {
        this.controlTapeMaxSize = controlTapeMaxSize;
    }

    public boolean isCanReturnFullLastDocInfo() {
        return canReturnFullLastDocInfo;
    }

    public void setCanReturnFullLastDocInfo(boolean canReturnFullLastDocInfo) {
        this.canReturnFullLastDocInfo = canReturnFullLastDocInfo;
    }

    public boolean isCanMakeArbitraryRefund() {
        return canMakeArbitraryRefund;
    }

    public void setCanMakeArbitraryRefund(boolean canMakeArbitraryRefund) {
        this.canMakeArbitraryRefund = canMakeArbitraryRefund;
    }

    @Override
    public String toString() {
        return "FiscalPrinterEmulatorConfig{" +
                "manualExceptionPort=" + manualExceptionPort +
                ", printPreviewPort=" + printPreviewPort +
                ", zOnClosedShift=" + zOnClosedShift +
                ", recreatePrinterFileOnShiftOpen=" + recreatePrinterFileOnShiftOpen +
                ", showPrinterFrame=" + showPrinterFrame +
                ", createPrinterFrame=" + createPrinterFrame +
                ", printPreviewHost='" + printPreviewHost + '\'' +
                ", ofdMode=" + ofdMode +
                ", maxCharRow=" + maxCharRow +
                ", printLineInterval=" + printLineInterval +
                ", closeCheckInterval=" + closeCheckInterval +
                ", controlTapeMaxSize=" + controlTapeMaxSize +
                ", canReturnFullLastDocInfo=" + canReturnFullLastDocInfo +
                ", canMakeArbitraryRefund=" + canMakeArbitraryRefund +
                '}';
    }
}
