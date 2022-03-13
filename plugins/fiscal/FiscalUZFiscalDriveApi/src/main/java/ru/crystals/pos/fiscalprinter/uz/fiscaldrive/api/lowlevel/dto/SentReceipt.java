package ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDateTime;

public class SentReceipt {

    @JsonProperty("QRCodeURL")
    private String qrCodeURL;
    @JsonProperty("TerminalID")
    private String terminalID;
    @JsonProperty("ReceiptSeq")
    private String receiptSeq;
    @JsonDeserialize(using = SentReceiptTimeDeserializer.class)
    @JsonSerialize(using = SentReceiptTimeSerializer.class)
    @JsonProperty("DateTime")
    private LocalDateTime dateTime;
    @JsonProperty("FiscalSign")
    private String fiscalSign;

    public String getQrCodeURL() {
        return qrCodeURL;
    }

    public void setQrCodeURL(String qrCodeURL) {
        this.qrCodeURL = qrCodeURL;
    }

    public String getTerminalID() {
        return terminalID;
    }

    public void setTerminalID(String terminalID) {
        this.terminalID = terminalID;
    }

    public String getReceiptSeq() {
        return receiptSeq;
    }

    public void setReceiptSeq(String receiptSeq) {
        this.receiptSeq = receiptSeq;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getFiscalSign() {
        return fiscalSign;
    }

    public void setFiscalSign(String fiscalSign) {
        this.fiscalSign = fiscalSign;
    }

    @Override
    public String toString() {
        return "SentReceipt{" +
                "qRCodeURL='" + qrCodeURL + '\'' +
                ", terminalID='" + terminalID + '\'' +
                ", receiptSeq='" + receiptSeq + '\'' +
                ", dateTime=" + dateTime +
                ", fiscalSign='" + fiscalSign + '\'' +
                '}';
    }
}
