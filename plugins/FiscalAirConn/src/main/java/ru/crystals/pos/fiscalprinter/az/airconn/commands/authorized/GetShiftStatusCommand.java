package ru.crystals.pos.fiscalprinter.az.airconn.commands.authorized;

import ru.crystals.pos.fiscalprinter.az.airconn.model.responses.ShiftStatus;

public class GetShiftStatusCommand extends AuthorizedBaseCommand<ShiftStatus> {

    public GetShiftStatusCommand(String accessToken) {
        getRequest().setOperationId("getShiftStatus");
        setAccessToken(accessToken);
    }

    public Class<ShiftStatus> getResponseDataClass() {
        return ShiftStatus.class;
    }
}
