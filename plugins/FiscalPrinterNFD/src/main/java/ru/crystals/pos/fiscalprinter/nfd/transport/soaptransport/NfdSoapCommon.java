package ru.crystals.pos.fiscalprinter.nfd.transport.soaptransport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.soap.SOAPMessage;

import static ru.crystals.pos.fiscalprinter.nfd.transport.soaptransport.SoapPrettyPrintFormatter.getPrettyStringData;
import static ru.crystals.pos.fiscalprinter.nfd.transport.soaptransport.SoapPrettyPrintFormatter.getStringXmlBody;
import static ru.crystals.pos.fiscalprinter.nfd.transport.soaptransport.SoapPrettyPrintFormatter.getStringXmlEnvelope;

public abstract class NfdSoapCommon {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    protected SOAPMessage soapMessage;
    private String prettyPrintData;
    private String fullXmlEnvelope;
    private String xmlBody;

    public NfdSoapCommon(SOAPMessage soapMessage) {
        this.soapMessage = soapMessage;
        this.prettyPrintData = getPrettyStringData(soapMessage);
        this.fullXmlEnvelope = getStringXmlEnvelope(soapMessage);
    }

    public SOAPMessage getSoapMessage() {
        return soapMessage;
    }

    public String getFullXmlEnvelope() {
        return fullXmlEnvelope;
    }

    public String getXmlBody() {
        if (xmlBody == null) {
            try {
                xmlBody = getStringXmlBody(soapMessage.getSOAPBody());
            } catch (Exception e) {
                logger.error("Error get SoapBody {}", e);
            }
        }
        return xmlBody;
    }

    @Override
    public String toString() {
        return prettyPrintData;
    }
}
