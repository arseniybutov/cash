package ru.crystals.pos.fiscalprinter.de.fcc.model;

/**
 *
 * @author dalex
 */
public class FCCInitRequest implements Request<FCCInit, FCCInitResp> {

    private FCCInit data = new FCCInit();

    @Override
    public String getUrlPath() {
        return "initialize";
    }

    @Override
    public FCCInit getRequestData() {
        return data;
    }

    @Override
    public Class<FCCInitResp> getResponseType() {
        return FCCInitResp.class;
    }

    @Override
    public boolean isUseAuthorization() {
        return true;
    }
}
