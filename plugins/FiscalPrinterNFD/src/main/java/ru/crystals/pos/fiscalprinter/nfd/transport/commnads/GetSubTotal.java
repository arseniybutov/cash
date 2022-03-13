package ru.crystals.pos.fiscalprinter.nfd.transport.commnads;


import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResponse;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.getsubtotal.GetSubTotalResponse;

/**
 * Подытог чека.
 */
public class GetSubTotal extends BaseRequest {

    private static final String METHOD_NAME = "getSubTotal";

    @Override
    public String getMethodName() {
        return METHOD_NAME;
    }

    @Override
    public Class<? extends BaseResponse> getClassResponse() {
        return GetSubTotalResponse.class;
    }
}
