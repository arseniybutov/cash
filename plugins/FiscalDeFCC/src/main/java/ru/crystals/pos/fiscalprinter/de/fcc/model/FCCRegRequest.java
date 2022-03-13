package ru.crystals.pos.fiscalprinter.de.fcc.model;

/**
 *
 * @author dalex
 */
public class FCCRegRequest implements Request<FCCReg, String> {

    private FCCReg data = new FCCReg();

    @Override
    public String getUrlPath() {
        return "registration";
    }

    @Override
    public FCCReg getRequestData() {
        return data;
    }

    @Override
    public Class<String> getResponseType() {
        return String.class;
    }

    @Override
    public boolean isUseAuthorization() {
        return true;
    }
}
