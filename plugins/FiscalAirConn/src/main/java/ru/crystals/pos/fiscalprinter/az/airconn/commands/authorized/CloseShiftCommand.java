package ru.crystals.pos.fiscalprinter.az.airconn.commands.authorized;

import ru.crystals.pos.fiscalprinter.az.airconn.model.responses.ReportData;

public class CloseShiftCommand extends AuthorizedBaseCommand<ReportData> {

    public CloseShiftCommand(String accessToken) {
        getRequest().setOperationId("closeShift");
        setAccessToken(accessToken);
    }

    public Class<ReportData> getResponseDataClass() {
        return ReportData.class;
    }
}
