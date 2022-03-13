package ru.crystals.pos.fiscalprinter.nfd.transport.soaptransport;

import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.util.Objects;

public class NfdSoapRequest extends NfdSoapCommon {

    private String soapEndpointUrl;

    public NfdSoapRequest(SOAPMessage soapMessage, String soapEndpointUrl) {
        super(soapMessage);
        this.soapEndpointUrl = soapEndpointUrl;
    }

    public NfdSoapResponse call() throws SOAPException {
        SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
        SOAPConnection soapConnection = soapConnectionFactory.createConnection();

        logger.info("Call: \n {}", this);

        SOAPMessage soapResponse = soapConnection.call(soapMessage, soapEndpointUrl);
        if (Objects.isNull(soapResponse)) {
            throw new SOAPException("Error get response for : \n" + soapMessage.toString());
        }
        NfdSoapResponse nfdSoapResponse = new NfdSoapResponse(soapResponse);

        logger.info("Response: \n {}", nfdSoapResponse);
        soapConnection.close();
        return nfdSoapResponse;
    }
}
