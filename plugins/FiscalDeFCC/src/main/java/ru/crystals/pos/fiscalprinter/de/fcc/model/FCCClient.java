package ru.crystals.pos.fiscalprinter.de.fcc.model;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author dalex
 */
public class FCCClient implements Serializable {

    private String clientId;
    private String clientState;
    private Date lastStateChange;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientState() {
        return clientState;
    }

    public void setClientState(String clientState) {
        this.clientState = clientState;
    }

    public Date getLastStateChange() {
        return lastStateChange;
    }

    public void setLastStateChange(Date lastStateChange) {
        this.lastStateChange = lastStateChange;
    }
}
