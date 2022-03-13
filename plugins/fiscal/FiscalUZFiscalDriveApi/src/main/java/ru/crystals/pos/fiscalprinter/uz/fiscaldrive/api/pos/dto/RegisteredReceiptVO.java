package ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.dto;

import java.time.LocalDateTime;

public class RegisteredReceiptVO {

    private final long receiptSeq;
    private final LocalDateTime dateTime;
    private final String fiscalSign;
    private final String qrCodeURL;

    private RegisteredReceiptVO(long receiptSeq, LocalDateTime dateTime, String fiscalSign, String qrCodeURL) {
        this.receiptSeq = receiptSeq;
        this.dateTime = dateTime;
        this.fiscalSign = fiscalSign;
        this.qrCodeURL = qrCodeURL;
    }

    public static RegisteredReceiptBuilder builder() {
        return new RegisteredReceiptBuilder();
    }

    public long getReceiptSeq() {
        return receiptSeq;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getFiscalSign() {
        return fiscalSign;
    }

    public String getQrCodeURL() {
        return qrCodeURL;
    }

    public static class RegisteredReceiptBuilder {
        private long receiptSeq;
        private LocalDateTime dateTime;
        private String fiscalSign;
        private String qrCodeURL;

        RegisteredReceiptBuilder() {
        }

        public RegisteredReceiptBuilder receiptSeq(long receiptSeq) {
            this.receiptSeq = receiptSeq;
            return this;
        }

        public RegisteredReceiptBuilder dateTime(LocalDateTime dateTime) {
            this.dateTime = dateTime;
            return this;
        }

        public RegisteredReceiptBuilder fiscalSign(String fiscalSign) {
            this.fiscalSign = fiscalSign;
            return this;
        }

        public RegisteredReceiptBuilder qrCodeURL(String qrCodeURL) {
            this.qrCodeURL = qrCodeURL;
            return this;
        }

        public RegisteredReceiptVO build() {
            return new RegisteredReceiptVO(receiptSeq, dateTime, fiscalSign, qrCodeURL);
        }
    }
}
