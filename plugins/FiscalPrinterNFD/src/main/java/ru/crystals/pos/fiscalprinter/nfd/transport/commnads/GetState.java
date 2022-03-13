package ru.crystals.pos.fiscalprinter.nfd.transport.commnads;


import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResponse;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.getstate.GetStateResponse;

/**
 * Получение информации о текущем состоянии.
 */
public class GetState extends BaseRequest {

    private static final String METHOD_NAME = "getState";

    @Override
    public String getMethodName() {
        return METHOD_NAME;
    }

    @Override
    public Class<? extends BaseResponse> getClassResponse() {
        return GetStateResponse.class;
    }
}
