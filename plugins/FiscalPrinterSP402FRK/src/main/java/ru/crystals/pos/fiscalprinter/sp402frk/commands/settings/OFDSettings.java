package ru.crystals.pos.fiscalprinter.sp402frk.commands.settings;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class OFDSettings {
    @XmlAttribute(name = "n")
    private String name = "OFD";
    @XmlAttribute(name = "t")
    private String type = "7";
}
