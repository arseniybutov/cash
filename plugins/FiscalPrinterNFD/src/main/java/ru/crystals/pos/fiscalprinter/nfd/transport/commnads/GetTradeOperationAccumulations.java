package ru.crystals.pos.fiscalprinter.nfd.transport.commnads;


import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.TradeOperationAccumulationType;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResponse;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.gettradeoperationaccumulations.GetTradeOperationAccumulationsResponse;

import java.util.Set;

/**
 * Получение накоплений в чеке.
 */
public class GetTradeOperationAccumulations extends BaseRequest {

    private static final String METHOD_NAME = "getTradeOperationAccumulations";

    /**
     * Типы накоплений.
     */
    private static final String TYPES_PARAM_NAME = "types";

    public GetTradeOperationAccumulations(Set<TradeOperationAccumulationType> tradeOperationAccumulationTypes) {
        setTypes(tradeOperationAccumulationTypes);
    }

    public Set<TradeOperationAccumulationType> getTypes() {
        return (Set<TradeOperationAccumulationType>) getMethodParam(TYPES_PARAM_NAME);
    }

    public void setTypes(Set<TradeOperationAccumulationType> types) {
        putMethodParam(TYPES_PARAM_NAME, types);
    }

    @Override
    public String getMethodName() {
        return METHOD_NAME;
    }

    @Override
    public Class<? extends BaseResponse> getClassResponse() {
        return GetTradeOperationAccumulationsResponse.class;
    }
}
