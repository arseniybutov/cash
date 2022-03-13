package ru.crystals.pos.fiscalprinter.nfd.transport.responses.deposit;

import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.MoneyPlacementDocument;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.documententry.CommonDocumentEntry;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResponse;

import java.util.List;

public class DepositResponse extends BaseResponse {
    public List<CommonDocumentEntry> getDocumentTextData() {
        return ((MoneyPlacementDocument) getReturn().getResultObject()).getData();
    }
}



