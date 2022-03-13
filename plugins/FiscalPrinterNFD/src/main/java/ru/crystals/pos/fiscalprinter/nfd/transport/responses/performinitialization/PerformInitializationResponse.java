package ru.crystals.pos.fiscalprinter.nfd.transport.responses.performinitialization;

import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.Initialization;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResponse;

public class PerformInitializationResponse extends BaseResponse {

    public Initialization getInitializationResultObject() throws FiscalPrinterException {
        if (getReturn() != null && getReturn().getResultObject() != null) {
            return (Initialization) getReturn().getResultObject();
        }
        throw new FiscalPrinterException("Error value of resultObject");
    }
}



