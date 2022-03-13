package ru.crystals.pos.fiscalprinter.nfd.transport.commnads;

import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResponse;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.canceldocument.CancelDocumentResponse;

/**
 * Аннулирование открытого чека.
 */
public class CancelDocument extends BaseRequest {

    private static final String METHOD_NAME = "cancelDocument";

    public CancelDocument() {
        putMethodParam(operatorPasswordParamName, operatorPasswordDefault);
    }

    @Override
    public String getMethodName() {
        return METHOD_NAME;
    }

    @Override
    public Class<? extends BaseResponse> getClassResponse() {
        return CancelDocumentResponse.class;
    }

}
