package ru.crystals.pos.fiscalprinter.nfd.transport.responses.getstate;

import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.State;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResponse;

public class GetStateResponse extends BaseResponse {

    public State getStateResultObject() throws FiscalPrinterException {
        if (getReturn() != null && getReturn().getResultObject() != null) {
            return (State) getReturn().getResultObject();
        }
        throw new FiscalPrinterException("Error value of resultObject");
    }
}
