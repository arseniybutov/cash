package ru.crystals.pos.fiscalprinter.sp402frk.commands.settings;

import ru.crystals.pos.fiscalprinter.sp402frk.transport.RequestElement;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class ProxyParams {
    @XmlAttribute(name = "n")
    private String name = "Proxy";
    @XmlAttribute(name = "t")
    private String type = "7";

    @XmlElement(name = "pa")
    private RequestElement hostName;
    @XmlElement(name = "pa")
    private RequestElement password;
    @XmlElement(name = "pa")
    private RequestElement port;
    @XmlElement(name = "pa")
    private RequestElement proxyType;
    @XmlElement(name = "pa")
    private RequestElement user;

}
