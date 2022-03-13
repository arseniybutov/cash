package ru.crystals.pos.fiscalprinter.de.fcc.model;

/**
 *
 * @author dalex
 */
public class FCCTransactionFinishRequest implements Request<FCCTransactionFinish, FCCTransactionFinishResp> {

    private FCCTransactionFinish data = new FCCTransactionFinish();
    private String transactionId;

    private String token;

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

    @Override
    public String getUrlPath() {
        return "transaction/" + transactionId;
    }

    @Override
    public FCCTransactionFinish getRequestData() {
        return data;
    }

    @Override
    public Class<FCCTransactionFinishResp> getResponseType() {
        return FCCTransactionFinishResp.class;
    }
}
