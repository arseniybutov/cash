package ru.crystals.pos.fiscalprinter.nfd.transport.commnads;


import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.domains.CommonDomain;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.TradeOperationType;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResponse;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.opendocument.OpenDocumentResponse;

/**
 * Открытие чека.
 */
public class OpenDocument extends BaseRequest {

    private static final String METHOD_NAME = "openDocument";

    /**
     * Тип документа.
     */
    private static final String TRADE_OPERATION_TYPE_PARAM_NAME = "tradeOperationType";

    /**
     * Тип отрасли.
     */
    private static final String DOMAIN_PARAM_NAME = "domain";


    public OpenDocument(TradeOperationType tradeOperationType, CommonDomain domain) {
        putMethodParam(operatorPasswordParamName, operatorPasswordDefault);
        setTradeOperationType(tradeOperationType);
        setDomain(domain);
    }

    @Override
    public String getMethodName() {
        return METHOD_NAME;
    }

    @Override
    public Class<? extends BaseResponse> getClassResponse() {
        return OpenDocumentResponse.class;
    }

    public TradeOperationType getTradeOperationType() {
        return (TradeOperationType) getMethodParam(TRADE_OPERATION_TYPE_PARAM_NAME);
    }

    public void setTradeOperationType(TradeOperationType tradeOperationType) {
        putMethodParam(TRADE_OPERATION_TYPE_PARAM_NAME, tradeOperationType);
    }

    public CommonDomain getDomain() {
        return (CommonDomain) getMethodParam(DOMAIN_PARAM_NAME);
    }

    public void setDomain(CommonDomain domain) {
        putMethodParam(DOMAIN_PARAM_NAME, domain);
    }


}
