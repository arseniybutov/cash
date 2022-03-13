package ru.crystals.pos.bank.ucs.serviceoperations;

import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.ucs.ResBundleBankUcs;
import ru.crystals.pos.bank.ucs.messages.requests.GetReportRequest;
import ru.crystals.pos.bank.ucs.messages.requests.Request;

public class GetFullReportOperation extends UCSServiceOperation {
    @Override
    public Request createRequest() throws BankException {
        return new GetReportRequest(GetReportRequest.ReportType.FULL);
    }

    @Override
    public boolean needsInLastTransactionID() {
        return false;
    }

    @Override
    public boolean hasInitialResponse() {
        return false;
    }

    @Override
    public String getCommandTitle() {
        return ResBundleBankUcs.getString("FULL_REPORT_COMMAND_TITLE");
    }

    @Override
    public String getFormTitle() {
        return ResBundleBankUcs.getString("FULL_REPORT_FORM_TITLE");
    }

    @Override
    public String getSpinnerMessage() {
        return ResBundleBankUcs.getString("FULL_REPORT_SPINNER_MESSAGE");
    }
}
