package ru.crystals.pos.fiscalprinter.nfd.transport.soaptransport;


import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.BaseRequest;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.Modifier;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.PaymentNFD;
import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.domains.CommonDomain;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class NfdSoapRequestFactory {

    private String soapAction;
    private String myNamespace;
    private String myNamespaceURI;

    public NfdSoapRequestFactory(String soapAction, String myNamespace, String myNamespaceURI) {
        this.soapAction = soapAction;
        this.myNamespace = myNamespace;
        this.myNamespaceURI = myNamespaceURI;
    }

    public NfdSoapRequest createSOAPRequest(BaseRequest nfdSoapAction) throws SOAPException {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();

        SOAPPart soapPart = soapMessage.getSOAPPart();

        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration(myNamespace, myNamespaceURI);

        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement(nfdSoapAction.getMethodName(), myNamespace);
        for (Map.Entry<String, Object> entry : nfdSoapAction.getMethodParams().entrySet()) {
            if ("entity".equals(entry.getKey()) && "editCatalogEntity".equals(nfdSoapAction.getMethodName())) {
                setNds(soapBodyElem, (Map<String, String>) entry.getValue());
            } else if (entry.getValue() instanceof CommonDomain) {
                setCommonDomain(soapBodyElem, entry);
            } else if ("payments".equals(entry.getKey())) {
                setPayments(soapBodyElem, entry);
            } else if ("modifier".equals(entry.getKey())) {
                setModifier(soapBodyElem, entry);
            } else if ("taxGroupNumbers".equals(entry.getKey())) {
                setTaxGroupNumbers(soapBodyElem, entry);
            } else if ("types".equals(entry.getKey())) {
                setAccumulations(soapBodyElem, entry);
            } else {
                soapBodyElem.addChildElement(entry.getKey()).addTextNode(entry.getValue().toString());
            }
        }

        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", soapAction);

        soapMessage.saveChanges();

        NfdSoapRequest nfdSoapRequest = new NfdSoapRequest(soapMessage, soapAction);
        return nfdSoapRequest;
    }

    private void setCommonDomain(SOAPElement soapBodyElem, Map.Entry<String, Object> entry) throws SOAPException {
        CommonDomain commonDomain = (CommonDomain) entry.getValue();
        QName name = new QName("http://www.w3.org/2001/XMLSchema-instance", "type", "xsi");
        SOAPElement currentNode = soapBodyElem.addChildElement(entry.getKey());
        currentNode.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        currentNode.addAttribute(name, commonDomain.getType());
        for (Map.Entry<String, Object> entryParam : commonDomain.getParams().entrySet()) {
            currentNode.addChildElement(entryParam.getKey()).addTextNode(entryParam.getValue().toString());
        }
    }

    private void setNds(SOAPElement soapBodyElem, Map<String, String> fields) throws SOAPException {
        QName type = new QName("http://www.w3.org/2001/XMLSchema-instance", "type", "xsi");
        SOAPElement currentNode = soapBodyElem.addChildElement("entity");
        currentNode.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        currentNode.addAttribute(type, "emul:TaxGroup");
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            currentNode.addChildElement(entry.getKey()).addTextNode(entry.getValue());
        }
    }

    private void setPayments(SOAPElement soapBodyElem, Map.Entry<String, Object> entry) throws SOAPException {
        Set<PaymentNFD> payments = (Set<PaymentNFD>) entry.getValue();
        for (PaymentNFD payment : payments) {
            SOAPElement currentNode = soapBodyElem.addChildElement("payments");
            currentNode.addChildElement("type").addTextNode(payment.getPaymentType().toString());
            currentNode.addChildElement("sum").addTextNode(payment.getSum().toString());
        }
    }

    private void setModifier(SOAPElement soapBodyElem, Map.Entry<String, Object> entry) throws SOAPException {
        Modifier modifier = (Modifier) entry.getValue();
        SOAPElement currentNode = soapBodyElem.addChildElement("modifier");
        currentNode.addChildElement("type").addTextNode(modifier.getType().toString());
        currentNode.addChildElement("name").addTextNode(modifier.getName());
        currentNode.addChildElement("sum").addTextNode(modifier.getSum().toString());
        if (!modifier.getTaxGroupNumbers().isEmpty()) {
            currentNode.addChildElement("taxGroupNumbers").addTextNode(modifier.getTaxGroupNumbers().iterator().next().toString());
        }
    }

    private void setTaxGroupNumbers(SOAPElement soapBodyElem, Map.Entry<String, Object> entry) throws SOAPException {
        Set<Integer> taxGroupNumbers = (Set<Integer>) entry.getValue();
        if (!taxGroupNumbers.isEmpty()) {
            soapBodyElem.addChildElement("taxGroupNumbers").addTextNode(taxGroupNumbers.iterator().next().toString());
        }
    }

    private void setAccumulations(SOAPElement soapBodyElem, Map.Entry<String, Object> entry) throws SOAPException {
        Set<Object> accumulations = (Set<Object>) entry.getValue();
        Iterator<Object> iterator = accumulations.iterator();
        while (iterator.hasNext()) {
            soapBodyElem.addChildElement("types").addTextNode(iterator.next().toString());
        }
    }


}
