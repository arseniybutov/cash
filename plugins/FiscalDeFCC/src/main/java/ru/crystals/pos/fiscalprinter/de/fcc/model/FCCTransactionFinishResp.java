package ru.crystals.pos.fiscalprinter.de.fcc.model;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author dalex
 */
public class FCCTransactionFinishResp implements Serializable {

    private long signatureCounter;
    private String signatureValue;
    private Date logTime;

    public FCCTransactionFinishResp() {
    }

    public long getSignatureCounter() {
        return signatureCounter;
    }

    public void setSignatureCounter(long signatureCounter) {
        this.signatureCounter = signatureCounter;
    }

    public String getSignatureValue() {
        return signatureValue;
    }

    public void setSignatureValue(String signatureValue) {
        this.signatureValue = signatureValue;
    }

    public Date getLogTime() {
        return logTime;
    }

    public void setLogTime(Date logTime) {
        this.logTime = logTime;
    }
}
