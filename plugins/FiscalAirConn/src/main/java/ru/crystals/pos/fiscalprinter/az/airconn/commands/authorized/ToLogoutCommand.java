package ru.crystals.pos.fiscalprinter.az.airconn.commands.authorized;

public class ToLogoutCommand extends AuthorizedBaseCommand {

    public ToLogoutCommand(String accessToken) {
        getRequest().setOperationId("toLogout");
        setAccessToken(accessToken);
    }

    public Class<Object> getResponseDataClass() {
        return Object.class;
    }
}
