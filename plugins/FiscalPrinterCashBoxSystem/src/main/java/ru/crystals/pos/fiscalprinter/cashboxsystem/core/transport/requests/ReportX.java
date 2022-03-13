package ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.requests;

/**
 * Запрос на печать X-отчета. Данные запроса идентичны Z-отчету, поэтому наследуется его реализация.
 */
public class ReportX extends CbsReport {
    @Override
    public String getTarget() {
        return "/api/report/x";
    }
}
