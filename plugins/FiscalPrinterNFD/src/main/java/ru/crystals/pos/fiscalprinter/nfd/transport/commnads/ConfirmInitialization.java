package ru.crystals.pos.fiscalprinter.nfd.transport.commnads;


import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResponse;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.confirminitialization.ConfirmInitializationResponse;

/**
 * Подтверждение инициализации NFD.
 */
public class ConfirmInitialization extends BaseRequest {

    private static final String METHOD_NAME = "confirmInitialization";

    public ConfirmInitialization(String operatorPassword) {
        putMethodParam(operatorPasswordParamName, operatorPassword);
    }

    public ConfirmInitialization() {
        putMethodParam(operatorPasswordParamName, operatorPasswordDefault);
    }

    @Override
    public String getMethodName() {
        return METHOD_NAME;
    }

    @Override
    public Class<? extends BaseResponse> getClassResponse() {
        return ConfirmInitializationResponse.class;
    }

}
