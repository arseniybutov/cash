package ru.crystals.pos.fiscalprinter.sp402frk.commands.settings;

import ru.crystals.pos.fiscalprinter.sp402frk.transport.RequestElement;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class COMPortParams {
    @XmlAttribute(name = "n")
    private String name = "comport";
    @XmlAttribute(name = "t")
    private String type = "7";

    /**
     * Управление потоком:
     * 0 – None
     * 2 – Software
     */
    @XmlElement(name = "pa")
    private RequestElement flowcontrol;
    /**
     * RS-232 PPP
     * n – PPP выключено (raw)
     * y – PPP включено (TCP-IP)
     */
    @XmlElement(name = "pa")
    private RequestElement useppp;
}
