package ru.crystals.pos.fiscalprinter.de.fcc.model;

/**
 *
 * @author dalex
 */
public class FCCTokenRequest implements Request<String, FCCTokenResp> {

    private String uniqueClientId;
    private String ersSecret;

    @Override
    public String getUrlPath() {
        return "oauth/token?grant_type=client_credentials";
    }

    public void setUniqueClientId(String uniqueClientId) {
        this.uniqueClientId = uniqueClientId;
    }

    public void setErsSecret(String ersSecret) {
        this.ersSecret = ersSecret;
    }

    @Override
    public String getRequestData() {
        return "";
    }

    @Override
    public Class<FCCTokenResp> getResponseType() {
        return FCCTokenResp.class;
    }

    @Override
    public String login() {
        return uniqueClientId;
    }

    @Override
    public String password() {
        return ersSecret;
    }

}
