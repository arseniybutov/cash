package ru.crystals.pos.fiscalprinter.nfd.transport.responses.withdrawal;

import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.MoneyPlacementDocument;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.documententry.CommonDocumentEntry;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResponse;

import java.util.List;


public class WithdrawalResponse extends BaseResponse {
    public List<CommonDocumentEntry> getDocumentTextData() {
        return ((MoneyPlacementDocument) getReturn().getResultObject()).getData();
    }
}



