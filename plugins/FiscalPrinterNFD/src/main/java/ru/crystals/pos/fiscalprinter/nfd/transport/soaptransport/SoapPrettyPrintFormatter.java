package ru.crystals.pos.fiscalprinter.nfd.transport.soaptransport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Objects;

public class SoapPrettyPrintFormatter {

    private static final Logger logger = LoggerFactory.getLogger(NfdSoapRequest.class);

    public static String getPrettyStringData(SOAPMessage soapMessage) {
        String prettyPrintData = "";
        if (Objects.nonNull(soapMessage)) {
            try {
                TransformerFactory tff = TransformerFactory.newInstance();
                Transformer tf = tff.newTransformer();
                tf.setOutputProperty(OutputKeys.INDENT, "yes");
                tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                Source sc = soapMessage.getSOAPPart().getContent();
                ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
                StreamResult result = new StreamResult(streamOut);
                tf.transform(sc, result);
                prettyPrintData = streamOut.toString();
            } catch (TransformerException | SOAPException e) {
                logger.error("Error {}", e);
                prettyPrintData = getStringXmlEnvelope(soapMessage);
            }
        }
        return prettyPrintData;
    }

    public static String getStringXmlEnvelope(SOAPMessage soapMessage) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            soapMessage.writeTo(baos);
        } catch (SOAPException | IOException e) {
            logger.error("Error {}", e);
        }
        return baos.toString();
    }

    public static String getStringXmlBody(SOAPBody message) throws Exception {
        Document doc = message.extractContentAsDocument();
        StringWriter sw = new StringWriter();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.transform(new DOMSource(doc), new StreamResult(sw));
        return sw.toString();
    }
}
