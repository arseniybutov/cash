package ru.crystals.pos.fiscalprinter.de.fcc.model;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author dalex
 */
public class FCCTransactionStartResp implements Serializable {

    private Date logTime;
    private int signatureCounter;
    private String signatureValue;
    private String transactionNumber;
    private String serialNumber;

    public FCCTransactionStartResp() {
    }

    public Date getLogTime() {
        return logTime;
    }

    public void setLogTime(Date logTime) {
        this.logTime = logTime;
    }

    public int getSignatureCounter() {
        return signatureCounter;
    }

    public void setSignatureCounter(int signatureCounter) {
        this.signatureCounter = signatureCounter;
    }

    public String getSignatureValue() {
        return signatureValue;
    }

    public void setSignatureValue(String signatureValue) {
        this.signatureValue = signatureValue;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
}
