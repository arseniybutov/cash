package ru.crystals.pos.fiscalprinter.nonfiscalmode.withoutprinter;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SimplePrinterConfig {

    @JsonProperty("needToPrint")
    private boolean needToPrint;

    @JsonProperty("maxCharRow")
    private int maxCharRow = 44;

    public boolean isNeedToPrint() {
        return needToPrint;
    }

    public void setNeedToPrint(boolean needToPrint) {
        this.needToPrint = needToPrint;
    }

    public int getMaxCharRow() {
        return maxCharRow;
    }

    public void setMaxCharRow(int maxCharRow) {
        this.maxCharRow = maxCharRow;
    }
}
