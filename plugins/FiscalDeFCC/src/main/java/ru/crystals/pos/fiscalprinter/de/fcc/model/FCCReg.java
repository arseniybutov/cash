package ru.crystals.pos.fiscalprinter.de.fcc.model;

import java.io.Serializable;

/**
 *
 * @author dalex
 */
public class FCCReg implements Serializable {

    private String registrationToken;
    private String uniqueClientId;
    private String briefDescription;
    private String typeOfSystem;

    public FCCReg() {
    }

    public String getRegistrationToken() {
        return registrationToken;
    }

    public void setRegistrationToken(String registrationToken) {
        this.registrationToken = registrationToken;
    }

    public String getUniqueClientId() {
        return uniqueClientId;
    }

    public void setUniqueClientId(String uniqueClientId) {
        this.uniqueClientId = uniqueClientId;
    }

    public String getBriefDescription() {
        return briefDescription;
    }

    public void setBriefDescription(String briefDescription) {
        this.briefDescription = briefDescription;
    }

    public String getTypeOfSystem() {
        return typeOfSystem;
    }

    public void setTypeOfSystem(String typeOfSystem) {
        this.typeOfSystem = typeOfSystem;
    }
}
