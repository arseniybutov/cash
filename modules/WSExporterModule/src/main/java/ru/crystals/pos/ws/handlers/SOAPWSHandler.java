package ru.crystals.pos.ws.handlers;

import java.util.AbstractList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Хендлер, который используется для исправления namespace на клиентский
 *
 */
public class SOAPWSHandler implements SOAPHandler<SOAPMessageContext> {
    private static final Logger LOG = LoggerFactory.getLogger(SOAPWSHandler.class);
    private final String newNamespace;
    private final String oldNamespace;

    /**
     * Единственнй правильный конструктор
     * @param oldNamespace какой неймспейс ищем
     * @param newNamespace на что меняем
     */
    public SOAPWSHandler(String oldNamespace, String newNamespace) {
        this.newNamespace = newNamespace;
        this.oldNamespace = oldNamespace;
    }

    /**
     * Тут происходит вся "магия". Мы в SOAP-messag'e подменяем дефолтный неймспейс на клиентский. Тем самым,
     * клиентский веб вервис будет очень доволен и примет наш запрос
     * @param mc контекст
     * @return все ли хорошо
     */
    public boolean handleMessage(SOAPMessageContext mc) {
        boolean isRequest = (Boolean) mc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        try {
            if (isRequest) {
                SOAPMessage msg = mc.getMessage();
                SOAPBody body = msg.getSOAPBody();

                recursSetNamespace(body);

                if (msg.saveRequired()) {
                    msg.saveChanges();
                }
            }
        } catch (Exception e) {
            LOG.error("Failed to change namespace: ", e);
            return false;
        }

        return true;
    }

    /**
     * Рекурсивно идем по нодам и меняем неймспейс снизу наверх, т.к. обратно нельзя. Будет ексепшн
     * @param body
     * @throws SOAPException
     */
    private void recursSetNamespace(SOAPElement body) throws SOAPException {
        Iterator<?> iter = body.getChildElements();
        while (iter.hasNext()) {
            Object next = iter.next();
            if (next instanceof SOAPElement) {
                SOAPElement se = (SOAPElement) next;
                recursSetNamespace(se);
                String ns = se.getPrefix();
                if(ns!=null){
                se.removeNamespaceDeclaration(ns);

                QName exportPurchaseQName = new QName(newNamespace, se.getLocalName(), ns);
                se.setElementQName(exportPurchaseQName);  }
            }
        }
    }

    public Set<QName> getHeaders() {
        return Collections.emptySet();
    }

    public void close(MessageContext mc) {
    }

    public boolean handleFault(SOAPMessageContext mc) {
        return true;
    }

    public List<Node> asList(NodeList n) {
        return n.getLength() == 0 ? Collections.<Node>emptyList() : new NodeListWrapper(n);
    }

    static final class NodeListWrapper extends AbstractList<Node> implements RandomAccess {
        private final NodeList list;

        NodeListWrapper(NodeList l) {
            list = l;
        }

        public Node get(int index) {
            return list.item(index);
        }

        public int size() {
            return list.getLength();
        }
    }

}