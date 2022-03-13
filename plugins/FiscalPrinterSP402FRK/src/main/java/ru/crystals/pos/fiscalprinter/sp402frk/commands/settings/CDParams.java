package ru.crystals.pos.fiscalprinter.sp402frk.commands.settings;

import ru.crystals.pos.fiscalprinter.sp402frk.support.KKTDataType;
import ru.crystals.pos.fiscalprinter.sp402frk.transport.RequestElement;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class CDParams {
    @XmlAttribute(name = "n")
    private String name = "CDSettings";
    @XmlAttribute(name = "t")
    private String type = "7";

    /**
     * Тип дисплея покупателя
     * 0 - BA63
     * 1 - LD-202
     * 2 – EPSON USB
     */
    @XmlElement(name = "CDType")
    private RequestElement cdType = new RequestElement("CompactPrint", KKTDataType.STRING, "0");

    /**
     * Использование дисплея покупателя
     * 0 - "нет"
     * 1 - "да"
     */
    @XmlElement(name = "UseCD")
    private RequestElement useCD = new RequestElement("CompactPrint", KKTDataType.STRING, "0");
}
