package ru.crystals.pos.fiscalprinter.de.fcc.model;

/**
 *
 * @author dalex
 */
public class FCCGetRegistredClientsRequest implements Request<String, FCCRegistredClientsResp> {

    @Override
    public String getUrlPath() {
        return "clients";
    }

    @Override
    public String getRequestData() {
        return null;
    }

    @Override
    public Class<FCCRegistredClientsResp> getResponseType() {
        return FCCRegistredClientsResp.class;
    }

    @Override
    public boolean isUseAuthorization() {
        return true;
    }
}
