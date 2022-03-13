package ru.crystals.pos.fiscalprinter.sp402frk.commands;

import ru.crystals.pos.fiscalprinter.sp402frk.support.KKTDataType;
import ru.crystals.pos.fiscalprinter.sp402frk.transport.RequestElement;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Данные команды печати сервисного документа.
 */
@XmlRootElement(name = "pa")
@XmlAccessorType(XmlAccessType.NONE)
public class ArmNonFiscalDoc {
    @XmlAttribute(name = "n")
    private String name = "200003";
    @XmlAttribute(name = "t")
    private String type = "7";

    @XmlElement(name = "pa")
    private RequestElement nonFiscalText = new RequestElement("NonFiscalText", KKTDataType.STRING, "");

    public void addLine(String line) {
        nonFiscalText.setValue(nonFiscalText.getValue() + line + "\n");
    }

    public String getNonFiscalText() {
        return nonFiscalText.getValue();
    }

    public void setNonFiscalText(String nonFiscalText) {
        this.nonFiscalText.setValue(nonFiscalText);
    }
}