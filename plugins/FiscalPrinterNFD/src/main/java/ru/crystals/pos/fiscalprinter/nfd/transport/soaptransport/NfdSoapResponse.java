package ru.crystals.pos.fiscalprinter.nfd.transport.soaptransport;

import javax.xml.soap.SOAPMessage;

public class NfdSoapResponse extends NfdSoapCommon {

    public NfdSoapResponse(SOAPMessage soapMessage) {
        super(soapMessage);
    }
}
