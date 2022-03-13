package ru.crystals.pos.fiscalprinter.nfd.transport.commnads;


import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.ShiftAccumulationType;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResponse;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.getshiftaccumulations.GetShiftAccumulationsResponse;

import java.util.Set;

/**
 * Получение накоплений в смене.
 */
public class GetShiftAccumulations extends BaseRequest {

    private static final String METHOD_NAME = "getShiftAccumulations";

    /**
     * Типы накоплений.
     */
    private static final String TYPES_PARAM_NAME = "types";

    public GetShiftAccumulations(Set<ShiftAccumulationType> shiftAccumulationTypes) {
        setTypes(shiftAccumulationTypes);
    }

    public Set<ShiftAccumulationType> getTypes() {
        return (Set<ShiftAccumulationType>) getMethodParam(TYPES_PARAM_NAME);
    }

    public void setTypes(Set<ShiftAccumulationType> types) {
        putMethodParam(TYPES_PARAM_NAME, types);
    }

    @Override
    public String getMethodName() {
        return METHOD_NAME;
    }

    @Override
    public Class<? extends BaseResponse> getClassResponse() {
        return GetShiftAccumulationsResponse.class;
    }
}
