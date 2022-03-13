package ru.crystals.pos.fiscalprinter.sp402frk.commands.settings;

import ru.crystals.pos.fiscalprinter.sp402frk.support.KKTDataType;
import ru.crystals.pos.fiscalprinter.sp402frk.transport.RequestElement;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class PrinterHeader {
    @XmlAttribute(name = "n")
    private String name = "PrinterHeader";
    @XmlAttribute(name = "t")
    private String type = "7";

    /**
     * Печать реквизитов пользователя в клише
     * true – включена печать реквизитов пользователя в клише
     * false – выключена
     */
    @XmlElement(name = "pa")
    private RequestElement printUserAddrInCliche;

    /**
     * Максимальное количество строк клише – 4
     * Максимальное количество символов в строке:
     * СП402-Ф – 40;
     * Строки длиной больше, чем указанное количество символов, будут обрезаны.
     */
    @XmlElement(name = "pa")
    private RequestElement cliche = new RequestElement("Cliche", KKTDataType.STRING, "");

    public void addClicheLine(String line) {
        cliche.setValue(cliche.getValue() + line);
    }

    public String getCliche() {
        return cliche.getValue();
    }

    public void setCliche(String cliche) {
        this.cliche.setValue(cliche);
    }
}
