package ru.crystals.pos.fiscalprinter.de.fcc.model;

import java.io.Serializable;

/**
 *
 * @author dalex
 */
public class FCCInit implements Serializable {

    private String tssKey = "fcc-3dac9e4e-c5ba-41ce-974f-f9ae78f4a705";
    private String tssSecret = "c06b593c-fbc2-4f47-a613-33691cc4f269";
    private String remoteCspPassword = "<NOT_YET_NECESSARY>";
    private String ersSecret = null;
    public FCCInit() {
    }

    public String getTssKey() {
        return tssKey;
    }

    public void setTssKey(String tssKey) {
        this.tssKey = tssKey;
    }

    public String getTssSecret() {
        return tssSecret;
    }

    public void setTssSecret(String tssSecret) {
        this.tssSecret = tssSecret;
    }

    public String getRemoteCspPassword() {
        return remoteCspPassword;
    }

    public void setRemoteCspPassword(String remoteCspPassword) {
        this.remoteCspPassword = remoteCspPassword;
    }

    public String getErsSecret() {
        return ersSecret;
    }

    public void setErsSecret(String ersSecret) {
        this.ersSecret = ersSecret;
    }

}
