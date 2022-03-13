package ru.crystals.pos.fiscalprinter.nfd.transport.responses.xreport;

import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.ReportDocument;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.documententry.CommonDocumentEntry;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResponse;

import java.util.List;

public class XReportResponse extends BaseResponse {

    public List<CommonDocumentEntry> getDocumentTextData() {
        return ((ReportDocument) getReturn().getResultObject()).getData();
    }
}



