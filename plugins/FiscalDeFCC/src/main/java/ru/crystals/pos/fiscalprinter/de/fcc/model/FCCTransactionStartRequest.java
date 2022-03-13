package ru.crystals.pos.fiscalprinter.de.fcc.model;

/**
 *
 * @author dalex
 */
public class FCCTransactionStartRequest implements Request<FCCTransactionStart, FCCTransactionStartResp> {

    private FCCTransactionStart data = new FCCTransactionStart();
    private String token;

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String token() {
        return token;
    }

    @Override
    public String getUrlPath() {
        return "transaction";
    }

    @Override
    public FCCTransactionStart getRequestData() {
        return data;
    }

    @Override
    public Class<FCCTransactionStartResp> getResponseType() {
        return FCCTransactionStartResp.class;
    }
}
