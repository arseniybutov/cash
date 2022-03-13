package ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class ZReportInfo extends BaseResponse {

    @JsonProperty("TotalRefundCount")
    private Long totalRefundCount;
    @JsonProperty("TotalSaleVAT")
    private Long totalSaleVAT;
    @JsonProperty("TotalSaleCard")
    private Long totalSaleCard;
    @JsonProperty("Count")
    private Long count;
    @JsonProperty("TotalRefundCard")
    private Long totalRefundCard;
    @JsonProperty("FirstReceiptSeq")
    private Long firstReceiptSeq;
    @JsonProperty("TotalRefundCash")
    private Long totalRefundCash;
    @JsonProperty("TotalSaleCash")
    private Long totalSaleCash;
    @JsonProperty("OpenTime")
    private LocalDateTime openTime;
    @JsonProperty("Number")
    private Long number;
    @JsonProperty("CloseTime")
    private LocalDateTime closeTime;
    @JsonProperty("LastReceiptSeq")
    private String lastReceiptSeq;
    @JsonProperty("TotalRefundVAT")
    private Long totalRefundVAT;
    @JsonProperty("TerminalID")
    private String terminalID;
    @JsonProperty("TotalSaleCount")
    private Long totalSaleCount;

    public Long getTotalRefundCount() {
        return totalRefundCount;
    }

    public void setTotalRefundCount(Long totalRefundCount) {
        this.totalRefundCount = totalRefundCount;
    }

    public Long getTotalSaleVAT() {
        return totalSaleVAT;
    }

    public void setTotalSaleVAT(Long totalSaleVAT) {
        this.totalSaleVAT = totalSaleVAT;
    }

    public Long getTotalSaleCard() {
        return totalSaleCard;
    }

    public void setTotalSaleCard(Long totalSaleCard) {
        this.totalSaleCard = totalSaleCard;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Long getTotalRefundCard() {
        return totalRefundCard;
    }

    public void setTotalRefundCard(Long totalRefundCard) {
        this.totalRefundCard = totalRefundCard;
    }

    public Long getFirstReceiptSeq() {
        return firstReceiptSeq;
    }

    public void setFirstReceiptSeq(Long firstReceiptSeq) {
        this.firstReceiptSeq = firstReceiptSeq;
    }

    public Long getTotalRefundCash() {
        return totalRefundCash;
    }

    public void setTotalRefundCash(Long totalRefundCash) {
        this.totalRefundCash = totalRefundCash;
    }

    public Long getTotalSaleCash() {
        return totalSaleCash;
    }

    public void setTotalSaleCash(Long totalSaleCash) {
        this.totalSaleCash = totalSaleCash;
    }

    public LocalDateTime getOpenTime() {
        return openTime;
    }

    public void setOpenTime(LocalDateTime openTime) {
        this.openTime = openTime;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public LocalDateTime getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(LocalDateTime closeTime) {
        this.closeTime = closeTime;
    }

    public String getLastReceiptSeq() {
        return lastReceiptSeq;
    }

    public void setLastReceiptSeq(String lastReceiptSeq) {
        this.lastReceiptSeq = lastReceiptSeq;
    }

    public Long getTotalRefundVAT() {
        return totalRefundVAT;
    }

    public void setTotalRefundVAT(Long totalRefundVAT) {
        this.totalRefundVAT = totalRefundVAT;
    }

    public String getTerminalID() {
        return terminalID;
    }

    public void setTerminalID(String terminalID) {
        this.terminalID = terminalID;
    }

    public Long getTotalSaleCount() {
        return totalSaleCount;
    }

    public void setTotalSaleCount(Long totalSaleCount) {
        this.totalSaleCount = totalSaleCount;
    }

    @Override
    public String toString() {
        return "ZReportInfo{" +
                "totalRefundCount=" + totalRefundCount +
                ", totalSaleVAT=" + totalSaleVAT +
                ", totalSaleCard=" + totalSaleCard +
                ", count=" + count +
                ", totalRefundCard=" + totalRefundCard +
                ", firstReceiptSeq=" + firstReceiptSeq +
                ", totalRefundCash=" + totalRefundCash +
                ", totalSaleCash=" + totalSaleCash +
                ", openTime=" + openTime +
                ", number=" + number +
                ", closeTime=" + closeTime +
                ", lastReceiptSeq='" + lastReceiptSeq + '\'' +
                ", totalRefundVAT=" + totalRefundVAT +
                ", terminalID='" + terminalID + '\'' +
                ", totalSaleCount=" + totalSaleCount +
                '}';
    }
}
