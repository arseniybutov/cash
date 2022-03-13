package ru.crystals.pos.ws.sales;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ru.crystals.pos.ws.handlers.SOAPWSHandler;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;

/**
 * Created by myaichnikov on 10.10.2014.
 */
@Ignore("Не работает")
public class SOAPWSHandlerTest {
    SOAPMessage message;
    SOAPMessageContext context;

    @Before
    public void prepare()
        throws IOException, SOAPException, JAXBException, ParserConfigurationException, SAXException, TransformerException, XPathExpressionException {
        InputStream stream = this.getClass().getResourceAsStream("/testSoap.xml");
        String xml = getFlatXMLString(stream);

        MessageFactory factory = MessageFactory.newInstance();
        message = factory.createMessage(new MimeHeaders(), new ByteArrayInputStream(xml.getBytes()));

        context = Mockito.mock(SOAPMessageContext.class);
        Mockito.when(context.getMessage()).thenReturn(message);
        Mockito.when(context.get(anyString())).thenReturn(true);

    }

    /**
     * Читаем файл и преобразуем его в строчку без "лишних" пробелов. Для дальнейшего удобства
     * @param stream
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws XPathExpressionException
     * @throws TransformerException
     */
    private String getFlatXMLString(InputStream stream)
        throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, TransformerException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(stream);

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPathExpression xpathExp = xpathFactory.newXPath().compile(
            "//text()[normalize-space(.) = '']");
        NodeList emptyTextNodes = (NodeList)
            xpathExp.evaluate(doc, XPathConstants.NODESET);

        for (int i = 0; i < emptyTextNodes.getLength(); i++) {
            Node emptyTextNode = emptyTextNodes.item(i);
            emptyTextNode.getParentNode().removeChild(emptyTextNode);
        }

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "no" );
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.getBuffer().toString().replaceAll("\n|\r", "");
    }

    @Test
    public void testChangeNS() throws SOAPException {
        SOAPWSHandler handler = new SOAPWSHandler("http://sales.ws.pos.crystals.ru/", "test");

        handler.handleMessage(context);

        assertEquals("exportPurchase", message.getSOAPBody().getFirstChild().getLocalName());
        assertEquals("test", message.getSOAPBody().getFirstChild().getNamespaceURI());

        assertEquals("purchases", message.getSOAPBody().getFirstChild().getFirstChild().getLocalName());
        assertEquals("test", message.getSOAPBody().getFirstChild().getFirstChild().getNamespaceURI());
    }
}
