package ru.crystals.pos.fiscalprinter.az.airconn.commands;

import ru.crystals.pos.fiscalprinter.az.airconn.model.requests.AccessToken;
import ru.crystals.pos.fiscalprinter.az.airconn.model.requests.LoginCredentials;

public class ToLoginCommand extends BaseCommand<LoginCredentials, AccessToken> {

    public ToLoginCommand(LoginCredentials loginParameters) {
        getRequest().setOperationId("toLogin");
        setParameters(loginParameters);
    }

    public Class<AccessToken> getResponseDataClass() {
        return AccessToken.class;
    }
}
