package ru.crystals.pos.fiscalprinter.nfd.transport.commnads;


import ru.crystals.pos.fiscalprinter.nfd.techprocessdata.TaxGroupNumber;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResponse;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.SetNDSResponse;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Конфигурация ставки НДС
 */
public class SetNDS extends BaseRequest {

    private static final String METHOD_NAME = "editCatalogEntity";

    public SetNDS(TaxGroupNumber taxGroupNumber) {
        putMethodParam(operatorPasswordParamName, operatorPasswordDefault);
        Map<String, String> map = new LinkedHashMap<>();
        map.put("number", String.valueOf(taxGroupNumber.getValue()));
        map.put("percent", taxGroupNumber.getPercent());
        map.put("type", "VAT");
        map.put("taxationType", "RTS");
        putMethodParam("entity", map);
    }

    @Override
    public String getMethodName() {
        return METHOD_NAME;
    }

    @Override
    public Class<? extends BaseResponse> getClassResponse() {
        return SetNDSResponse.class;
    }
}
