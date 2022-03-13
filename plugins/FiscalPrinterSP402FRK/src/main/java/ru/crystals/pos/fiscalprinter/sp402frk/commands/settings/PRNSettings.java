package ru.crystals.pos.fiscalprinter.sp402frk.commands.settings;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class PRNSettings {
    @XmlAttribute(name = "n")
    private String name = "PRN";
    @XmlAttribute(name = "t")
    private String type = "7";

    @XmlElement(name = "pa")
    private CDParams cdParams;
    @XmlElement(name = "pa")
    private PrinterParams printerParams;
    @XmlElement(name = "pa")
    private PrinterHeader printerHeader;

    public CDParams getCdParams() {
        return cdParams;
    }

    public void setCdParams(CDParams cdParams) {
        this.cdParams = cdParams;
    }

    public PrinterParams getPrinterParams() {
        return printerParams;
    }

    public void setPrinterParams(PrinterParams printerParams) {
        this.printerParams = printerParams;
    }

    public PrinterHeader getPrinterHeader() {
        return printerHeader;
    }

    public void setPrinterHeader(PrinterHeader printerHeader) {
        this.printerHeader = printerHeader;
    }
}
