package ru.crystals.pos.fiscalprinter.sp402frk.commands.settings;

import ru.crystals.pos.fiscalprinter.sp402frk.support.KKTDataType;
import ru.crystals.pos.fiscalprinter.sp402frk.transport.RequestElement;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class PrinterParams {
    @XmlAttribute(name = "n")
    private String name = "Printer";
    @XmlAttribute(name = "t")
    private String type = "7";

    /**
     * Плотная печать (false/true)
     */
    @XmlElement(name = "pa")
    private RequestElement compactPrint = new RequestElement("CompactPrint", KKTDataType.STRING, "false");
    /**
     * Узкая лента (false/true)
     */
    @XmlElement(name = "pa")
    private RequestElement narrowWidth = new RequestElement("NarrowWidth", KKTDataType.STRING, "false");
    /**
     * Ширина бумаги
     */
    @XmlElement(name = "pa")
    private RequestElement printWidthChars = new RequestElement("PrintWidthChars", KKTDataType.STRING, "40");

    public String getCompactPrint() {
        return compactPrint.getValue();
    }

    public void setCompactPrint(String compactPrint) {
        this.compactPrint.setValue(compactPrint);
    }

    public String getNarrowWidth() {
        return narrowWidth.getValue();
    }

    public void setNarrowWidth(String narrowWidth) {
        this.narrowWidth.setValue(narrowWidth);
    }

    public String getPrintWidthChars() {
        return printWidthChars.getValue();
    }

    public void setPrintWidthChars(String printWidthChars) {
        this.printWidthChars.setValue(printWidthChars);
    }
}
