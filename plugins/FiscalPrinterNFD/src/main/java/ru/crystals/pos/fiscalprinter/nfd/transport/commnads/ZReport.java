package ru.crystals.pos.fiscalprinter.nfd.transport.commnads;


import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResponse;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.zreport.ZReportResponse;

/**
 * Получение Z-отчета (суточный отчет с гашением). При вызове метода закрывается рабочая смена.
 */
public class ZReport extends BaseRequest {

    private static final String METHOD_NAME = "zReport";

    public ZReport() {
        putMethodParam(operatorPasswordParamName, operatorPasswordDefault);
    }

    @Override
    public String getMethodName() {
        return METHOD_NAME;
    }

    @Override
    public Class<? extends BaseResponse> getClassResponse() {
        return ZReportResponse.class;
    }
}
