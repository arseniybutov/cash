package ru.crystals.pos.fiscalprinter.az.airconn.commands.authorized;

public class OpenShiftCommand extends AuthorizedBaseCommand {

    public OpenShiftCommand(String accessToken) {
        getRequest().setOperationId("openShift");
        setAccessToken(accessToken);
    }

    public Class<Object> getResponseDataClass() {
        return Object.class;
    }
}
