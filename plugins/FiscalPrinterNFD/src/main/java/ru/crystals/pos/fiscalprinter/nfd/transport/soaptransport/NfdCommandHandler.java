package ru.crystals.pos.fiscalprinter.nfd.transport.soaptransport;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.nfd.ResBundleFiscalPrinterNFD;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.BaseRequest;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResponse;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.ResponseCode;

import javax.xml.soap.SOAPException;
import java.io.IOException;

public class NfdCommandHandler {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private ObjectMapper objectMapper = new XmlMapper();
    private NfdSoapRequestFactory nfdSoapRequestFactory;

    public NfdCommandHandler(String soapAction, String myNamespace, String myNamespaceURI) {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        nfdSoapRequestFactory = new NfdSoapRequestFactory(soapAction, myNamespace, myNamespaceURI);
    }

    public BaseResponse invokeCommand(BaseRequest baseRequest) throws FiscalPrinterException {
        BaseResponse baseResponse;
        try {
            logger.info("Invoke: {}", baseRequest.getMethodName());
            NfdSoapRequest nfdSoapRequest = nfdSoapRequestFactory.createSOAPRequest(baseRequest);
            NfdSoapResponse nfdSoapResponse = nfdSoapRequest.call();
            baseResponse = objectMapper.readValue(nfdSoapResponse.getXmlBody(), baseRequest.getClassResponse());
            checkResponseError(baseResponse);
        } catch (SOAPException | FiscalPrinterException | IOException e) {
            logger.error("Error invoke: {}, {}", baseRequest.getMethodName(), e.getMessage());
            throw new FiscalPrinterException(e.getMessage(), CashErrorType.FISCAL_ERROR, e);
        }
        return baseResponse;
    }

    private BaseResponse checkResponseError(BaseResponse response) throws FiscalPrinterException {
        if (response != null && response.getReturn() != null) {
            logger.info("Invoke result: {}", response);
            if (response.getReturn().getCode() == ResponseCode.SUCCESS.getCode()) {
                return response;
            } else {
                throw new FiscalPrinterException(response.getReturn().getMessage() + " " + response.getReturn().getCode(), CashErrorType.FISCAL_ERROR);
            }
        }
        throw new FiscalPrinterException(ResBundleFiscalPrinterNFD.getString("ERROR_NO_DATA"), CashErrorType.FISCAL_ERROR);
    }


}
