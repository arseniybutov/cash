package ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class Info extends BaseResponse {

    @JsonProperty("ZReportMaxCount")
    private Long zReportMaxCount;

    @JsonProperty("ReceiptCount")
    private Long receiptCount;

    @JsonProperty("AvailablePersistentMemory")
    private Long availablePersistentMemory;

    @JsonProperty("TerminalID")
    private String terminalID;

    @JsonProperty("CurrentTime")
    private LocalDateTime currentTime;

    @JsonProperty("AvailableResetMemory")
    private Long availableResetMemory;

    @JsonProperty("CurrentReceiptSeq")
    private String currentReceiptSeq;

    @JsonProperty("ReceiptMaxCount")
    private Long receiptMaxCount;

    @JsonProperty("ZReportCount")
    private Long zReportCount;

    @JsonProperty("AvailableDeselectMemory")
    private Long availableDeselectMemory;

    public Long getZReportMaxCount() {
        return zReportMaxCount;
    }

    public void setZReportMaxCount(Long zReportMaxCount) {
        this.zReportMaxCount = zReportMaxCount;
    }

    public Long getReceiptCount() {
        return receiptCount;
    }

    public void setReceiptCount(Long receiptCount) {
        this.receiptCount = receiptCount;
    }

    public Long getAvailablePersistentMemory() {
        return availablePersistentMemory;
    }

    public void setAvailablePersistentMemory(Long availablePersistentMemory) {
        this.availablePersistentMemory = availablePersistentMemory;
    }

    public String getTerminalID() {
        return terminalID;
    }

    public void setTerminalID(String terminalID) {
        this.terminalID = terminalID;
    }

    public LocalDateTime getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(LocalDateTime currentTime) {
        this.currentTime = currentTime;
    }

    public Long getAvailableResetMemory() {
        return availableResetMemory;
    }

    public void setAvailableResetMemory(Long availableResetMemory) {
        this.availableResetMemory = availableResetMemory;
    }

    public String getCurrentReceiptSeq() {
        return currentReceiptSeq;
    }

    public void setCurrentReceiptSeq(String currentReceiptSeq) {
        this.currentReceiptSeq = currentReceiptSeq;
    }

    public Long getReceiptMaxCount() {
        return receiptMaxCount;
    }

    public void setReceiptMaxCount(Long receiptMaxCount) {
        this.receiptMaxCount = receiptMaxCount;
    }

    public Long getZReportCount() {
        return zReportCount;
    }

    public void setZReportCount(Long zReportCount) {
        this.zReportCount = zReportCount;
    }

    public Long getAvailableDeselectMemory() {
        return availableDeselectMemory;
    }

    public void setAvailableDeselectMemory(Long availableDeselectMemory) {
        this.availableDeselectMemory = availableDeselectMemory;
    }

    @Override
    public String toString() {
        return "Info{" +
                "zReportMaxCount=" + zReportMaxCount +
                ", receiptCount=" + receiptCount +
                ", availablePersistentMemory=" + availablePersistentMemory +
                ", terminalID='" + terminalID + '\'' +
                ", currentTime=" + currentTime +
                ", availableResetMemory=" + availableResetMemory +
                ", currentReceiptSeq='" + currentReceiptSeq + '\'' +
                ", receiptMaxCount=" + receiptMaxCount +
                ", zReportCount=" + zReportCount +
                ", availableDeselectMemory=" + availableDeselectMemory +
                '}';
    }
}
