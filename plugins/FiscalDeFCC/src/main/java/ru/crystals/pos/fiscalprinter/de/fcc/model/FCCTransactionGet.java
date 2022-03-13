package ru.crystals.pos.fiscalprinter.de.fcc.model;

/**
 *
 * @author dalex
 */
public class FCCTransactionGet implements Request<String, String> {

    private String transactionId;
    private String token;
    private String clientId;

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public String token() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public String getUrlPath() {
        return "export/transactions/" + transactionId + "?clientId=" + clientId;
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
