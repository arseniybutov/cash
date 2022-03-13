package ru.crystals.pos.fiscalprinter.sp402frk.commands.settings;

import ru.crystals.pos.fiscalprinter.sp402frk.transport.RequestElement;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class NetworkParams {
    @XmlAttribute(name = "n")
    private String name = "Net";
    @XmlAttribute(name = "t")
    private String type = "7";

    @XmlElement(name = "pa")
    private RequestElement netDns1;
    @XmlElement(name = "pa")
    private RequestElement netDns2;
    @XmlElement(name = "pa")
    private RequestElement netGate;
    @XmlElement(name = "pa")
    private RequestElement netIP;
    @XmlElement(name = "pa")
    private RequestElement netMask;
    /**
     * Тип сети
     * 0 – статический
     * 1 - динамический
     */
    @XmlElement(name = "pa")
    private RequestElement netType;


}
