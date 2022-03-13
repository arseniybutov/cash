package ru.crystals.pos.fiscalprinter.nfd.transport.commnads;


import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResponse;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.xreport.XReportResponse;

/**
 * Получение информации о текущем состоянии.
 */
public class XReport extends BaseRequest {

    private static final String METHOD_NAME = "xReport";

    public XReport() {
        putMethodParam(operatorPasswordParamName, operatorPasswordDefault);
    }

    @Override
    public String getMethodName() {
        return METHOD_NAME;
    }

    @Override
    public Class<? extends BaseResponse> getClassResponse() {
        return XReportResponse.class;
    }
}
