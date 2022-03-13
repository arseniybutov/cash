package ru.crystals.pos.fiscalprinter.az.airconn.commands.authorized;

import ru.crystals.pos.fiscalprinter.az.airconn.model.responses.ReportData;

public class GetXReportCommand extends AuthorizedBaseCommand<ReportData> {

    public GetXReportCommand(String accessToken) {
        getRequest().setOperationId("getXReport");
        setAccessToken(accessToken);
    }

    public Class<ReportData> getResponseDataClass() {
        return ReportData.class;
    }
}
