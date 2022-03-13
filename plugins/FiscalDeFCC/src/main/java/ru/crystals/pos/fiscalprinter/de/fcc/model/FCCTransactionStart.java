package ru.crystals.pos.fiscalprinter.de.fcc.model;

import java.io.Serializable;

/**
 *
 * @author dalex
 */
public class FCCTransactionStart implements Serializable {

    private String clientId;
    private String processType;
    private String processData;
    private String externalTransactionId;

    public FCCTransactionStart() {
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getProcessType() {
        return processType;
    }

    public void setProcessType(String processType) {
        this.processType = processType;
    }

    public String getProcessData() {
        return processData;
    }

    public void setProcessData(String processData) {
        this.processData = processData;
    }

    public String getExternalTransactionId() {
        return externalTransactionId;
    }

    public void setExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
    }
}
