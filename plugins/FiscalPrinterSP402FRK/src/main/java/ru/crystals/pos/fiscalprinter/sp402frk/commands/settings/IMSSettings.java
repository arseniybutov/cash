package ru.crystals.pos.fiscalprinter.sp402frk.commands.settings;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class IMSSettings {
    @XmlAttribute(name = "n")
    private String name = "IMS";
    @XmlAttribute(name = "t")
    private String type = "7";
}
