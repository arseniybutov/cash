package ru.crystals.pos.fiscalprinter.nfd.transport.responses.closedocument;

import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.TradeOperationDocument;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.documententry.CommonDocumentEntry;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResponse;

import java.util.List;

public class CloseDocumentResponse extends BaseResponse {

    public List<CommonDocumentEntry> getCloseDocumentTextData() {
        return ((TradeOperationDocument) getReturn().getResultObject()).getData();
    }

}



