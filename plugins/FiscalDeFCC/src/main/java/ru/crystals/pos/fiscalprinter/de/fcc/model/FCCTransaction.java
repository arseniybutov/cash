package ru.crystals.pos.fiscalprinter.de.fcc.model;

import java.io.Serializable;

/**
 *
 * @author dalex
 */
public class FCCTransaction implements Serializable {

    private String clientId;
    private String transactionNumber;
    private String externalTransactionId;
    private String state;

    public FCCTransaction() {
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public String getExternalTransactionId() {
        return externalTransactionId;
    }

    public void setExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "FCCTransaction{" + "clientId=" + clientId + ", transactionNumber=" + transactionNumber + ", externalTransactionId=" + externalTransactionId + ", state=" + state + '}';
    }
}
