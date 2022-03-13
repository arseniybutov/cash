package ru.crystals.pos.fiscalprinter.nfd.transport.responses.getshiftaccumulations;

import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.ShiftAccumulation;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResponse;

public class GetShiftAccumulationsResponse extends BaseResponse {
    public ShiftAccumulation getShiftAccumulationData() {
        return (ShiftAccumulation) getReturn().getResultObject();
    }
}



