package ru.crystals.pos.fiscalprinter.de.fcc.model;

/**
 *
 * @author dalex
 */
public class FCCOpenTransactionsGet implements Request<String, FCCOpenTransactionsResp> {

    private String clientId;
    private String token;

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public String token() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String getUrlPath() {
        return "transaction/" + clientId;
    }

    @Override
    public String getRequestData() {
        return null;
    }

    @Override
    public Class<FCCOpenTransactionsResp> getResponseType() {
        return FCCOpenTransactionsResp.class;
    }
}
