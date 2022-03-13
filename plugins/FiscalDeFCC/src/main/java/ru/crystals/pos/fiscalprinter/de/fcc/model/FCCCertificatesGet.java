package ru.crystals.pos.fiscalprinter.de.fcc.model;

/**
 *
 * @author dalex
 */
public class FCCCertificatesGet implements Request<String, String> {

    private String token;

    @Override
    public String token() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String getUrlPath() {
        return "export/certificates";
    }

    @Override
    public String getRequestData() {
        return null;
    }

    @Override
    public Class<String> getResponseType() {
        return String.class;
    }
}
