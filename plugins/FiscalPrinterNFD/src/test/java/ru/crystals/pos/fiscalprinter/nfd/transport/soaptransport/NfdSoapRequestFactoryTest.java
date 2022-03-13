package ru.crystals.pos.fiscalprinter.nfd.transport.soaptransport;

import org.junit.Ignore;
import org.junit.Test;
import ru.crystals.pos.fiscalprinter.nfd.techprocessdata.TaxGroupNumber;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.SetNDS;

import javax.xml.soap.SOAPException;

@Ignore
public class NfdSoapRequestFactoryTest {

    @Test
    public void createSOAPRequest() throws SOAPException {
        final NfdSoapRequestFactory f = new NfdSoapRequestFactory("http://172.29.17.207:7070", "emul", "http://emulator.nfd.neofiscal.neoservice.com/");
        final NfdSoapRequest soapRequest = f.createSOAPRequest(new SetNDS(TaxGroupNumber.NDS_8_GROUP));
        System.out.println(soapRequest.getXmlBody());
    }
}