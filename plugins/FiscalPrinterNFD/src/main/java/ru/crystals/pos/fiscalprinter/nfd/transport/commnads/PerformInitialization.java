package ru.crystals.pos.fiscalprinter.nfd.transport.commnads;


import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResponse;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.performinitialization.PerformInitializationResponse;


/**
 * Выполнение инициализации NFD.
 */
public class PerformInitialization extends BaseRequest {

    private static final String METHOD_NAME = "performInitialization";

    /**
     * Токен инициализации.
     * Может быть получен в Рабочем Месте Клиента Системы
     * (РМКС) Программного Фискализатора 3.0.1
     */
    private static final String TOKEN_PARAM_NAME = "token";

    public PerformInitialization(String token) {
        putMethodParam(TOKEN_PARAM_NAME, token);
        putMethodParam(operatorPasswordParamName, operatorPasswordDefault);
    }

    @Override
    public String getMethodName() {
        return METHOD_NAME;
    }

    @Override
    public Class<? extends BaseResponse> getClassResponse() {
        return PerformInitializationResponse.class;
    }

    public String getToken() {
        return (String) getMethodParam(TOKEN_PARAM_NAME);
    }

    public void setToken(String token) {
        putMethodParam(TOKEN_PARAM_NAME, token);
    }
}
