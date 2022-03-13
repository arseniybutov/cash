package ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.requests;

/**
 * Копия последнеге Z-отчета
 */
public class ReportZCopy extends CbsReport {
    @Override
    public String getTarget() {
        return "/api/report/z-last";
    }
}
