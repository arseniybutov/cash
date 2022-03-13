package ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class ReceiptInfo extends BaseResponse {

    @JsonProperty("Sale")
    private Boolean isSale;
    @JsonProperty("Number")
    private Long number;
    @JsonProperty("TransactionTime")
    private LocalDateTime transactionTime;
    @JsonProperty("TotalVAT")
    private Long totalVAT;
    @JsonProperty("TotalCard")
    private Long totalCard;
    @JsonProperty("TotalCash")
    private Long totalCash;
    @JsonProperty("TerminalID")
    private String terminalID;
    @JsonProperty("Count")
    private Long count;
    @JsonProperty("ReceiptSeq")
    private String receiptSeq;

    public Boolean getSale() {
        return isSale;
    }

    public void setSale(Boolean sale) {
        isSale = sale;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public LocalDateTime getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(LocalDateTime transactionTime) {
        this.transactionTime = transactionTime;
    }

    public Long getTotalVAT() {
        return totalVAT;
    }

    public void setTotalVAT(Long totalVAT) {
        this.totalVAT = totalVAT;
    }

    public Long getTotalCard() {
        return totalCard;
    }

    public void setTotalCard(Long totalCard) {
        this.totalCard = totalCard;
    }

    public Long getTotalCash() {
        return totalCash;
    }

    public void setTotalCash(Long totalCash) {
        this.totalCash = totalCash;
    }

    public String getTerminalID() {
        return terminalID;
    }

    public void setTerminalID(String terminalID) {
        this.terminalID = terminalID;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public String getReceiptSeq() {
        return receiptSeq;
    }

    public void setReceiptSeq(String receiptSeq) {
        this.receiptSeq = receiptSeq;
    }

    @Override
    public String toString() {
        return "ReceiptInfo{" +
                "isSale=" + isSale +
                ", number=" + number +
                ", transactionTime=" + transactionTime +
                ", totalVAT=" + totalVAT +
                ", totalCard=" + totalCard +
                ", totalCash=" + totalCash +
                ", terminalID='" + terminalID + '\'' +
                ", count=" + count +
                ", receiptSeq='" + receiptSeq + '\'' +
                '}';
    }
}
