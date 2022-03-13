package ru.crystals.pos.fiscalprinter.retailforce.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FiscalResponseAdditionalFields {

    @JsonProperty("QrCodeDataString")
    @JsonAlias("QrCode")
    private String qrCodeDataString;

    @JsonProperty("TransactionStartTime")
    private Long transactionStartTime;

    @JsonProperty("TransactionEndTime")
    private Long transactionEndTime;

    @JsonProperty("ProcessData")
    private String processData;

    @JsonProperty("ProcessType")
    private String processType;

    @JsonProperty("TseSerial")
    private String tseSerial;

    @JsonProperty("TseTimeFormat")
    private String tseTimeFormat;

    @JsonProperty("TseHashAlgorithm")
    private String tseHashAlgorithm;

    @JsonProperty("TsePublicKey")
    private String tsePublicKey;

    @JsonProperty("TseSignatureCounter")
    private Long tseSignatureCounter;


    public String getQrCodeDataString() {
        return qrCodeDataString;
    }

    public void setQrCodeDataString(String qrCodeDataString) {
        this.qrCodeDataString = qrCodeDataString;
    }

    public Long getTransactionStartTime() {
        return transactionStartTime;
    }

    public void setTransactionStartTime(Long transactionStartTime) {
        this.transactionStartTime = transactionStartTime;
    }

    public Long getTransactionEndTime() {
        return transactionEndTime;
    }

    public void setTransactionEndTime(Long transactionEndTime) {
        this.transactionEndTime = transactionEndTime;
    }

    public String getProcessData() {
        return processData;
    }

    public void setProcessData(String processData) {
        this.processData = processData;
    }

    public String getProcessType() {
        return processType;
    }

    public void setProcessType(String processType) {
        this.processType = processType;
    }

    public String getTseSerial() {
        return tseSerial;
    }

    public void setTseSerial(String tseSerial) {
        this.tseSerial = tseSerial;
    }

    public String getTseTimeFormat() {
        return tseTimeFormat;
    }

    public void setTseTimeFormat(String tseTimeFormat) {
        this.tseTimeFormat = tseTimeFormat;
    }

    public String getTseHashAlgorithm() {
        return tseHashAlgorithm;
    }

    public void setTseHashAlgorithm(String tseHashAlgorithm) {
        this.tseHashAlgorithm = tseHashAlgorithm;
    }

    public String getTsePublicKey() {
        return tsePublicKey;
    }

    public void setTsePublicKey(String tsePublicKey) {
        this.tsePublicKey = tsePublicKey;
    }

    public Long getTseSignatureCounter() {
        return tseSignatureCounter;
    }

    public void setTseSignatureCounter(Long tseSignatureCounter) {
        this.tseSignatureCounter = tseSignatureCounter;
    }
}
