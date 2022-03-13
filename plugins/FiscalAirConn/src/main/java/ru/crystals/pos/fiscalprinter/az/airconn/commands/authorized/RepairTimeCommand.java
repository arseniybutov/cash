package ru.crystals.pos.fiscalprinter.az.airconn.commands.authorized;

public class RepairTimeCommand extends AuthorizedBaseCommand {

    public RepairTimeCommand(String accessToken) {
        getRequest().setOperationId("repairTime");
        setAccessToken(accessToken);
    }

    public Class<Object> getResponseDataClass() {
        return Object.class;
    }
}
