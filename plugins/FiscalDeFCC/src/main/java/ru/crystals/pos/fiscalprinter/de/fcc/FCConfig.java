package ru.crystals.pos.fiscalprinter.de.fcc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FCConfig {

    @JsonProperty("login")
    private String login;
    @JsonProperty("password")
    private String password;
    @JsonProperty("ersSecret")
    private String ersSecret;
    @JsonProperty("uniqueClientId")
    private String uniqueClientId;
    @JsonProperty("registrationToken")
    private String registrationToken;
    @JsonProperty("connectorAddress")
    private String connectorAddress;
    @JsonProperty("deviceSerial")
    private String deviceSerial;


    public String getDeviceSerial() {
        return deviceSerial;
    }

    public void setDeviceSerial(String deviceSerial) {
        this.deviceSerial = deviceSerial;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getErsSecret() {
        return ersSecret;
    }

    public void setErsSecret(String ersSecret) {
        this.ersSecret = ersSecret;
    }

    public String getUniqueClientId() {
        return uniqueClientId;
    }

    public void setUniqueClientId(String uniqueClientId) {
        this.uniqueClientId = uniqueClientId;
    }

    public String getRegistrationToken() {
        return registrationToken;
    }

    public void setRegistrationToken(String registrationToken) {
        this.registrationToken = registrationToken;
    }

    public String getConnectorAddress() {
        return connectorAddress;
    }

    public void setConnectorAddress(String connectorAddress) {
        this.connectorAddress = connectorAddress;
    }

}
