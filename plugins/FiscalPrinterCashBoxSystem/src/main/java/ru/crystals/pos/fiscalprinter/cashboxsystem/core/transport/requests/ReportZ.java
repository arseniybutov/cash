package ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.requests;

public class ReportZ extends CbsReport {
    @Override
    public String getTarget() {
        return "/api/report/z";
    }
}
