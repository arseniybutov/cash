package ru.crystals.pos.fiscalprinter.cashboxsystem.core.transport.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.docreportprint.DocReportPrint;

/**
 * Ответ на запрос X и Z отчета.
 */
public class ReportResponse extends BaseResponse {
    /**
     * Документ для печати с реквизитами и z-отчетом
     */
    @JsonProperty("data")
    private DocReportPrint docReportPrint;

    public DocReportPrint getDocReportPrint() {
        return docReportPrint;
    }

    public void setDocReportPrint(DocReportPrint docReportPrint) {
        this.docReportPrint = docReportPrint;
    }
}
