package ru.crystals.pos.fiscalprinter.sp402frk.commands.settings;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class MacroSettings {
    @XmlAttribute(name = "n")
    private String name = "MACRO";
    @XmlAttribute(name = "t")
    private String type = "7";

    @XmlElement(name = "pa")
    private FDDesign fdDesign;
    @XmlElement(name = "pa")
    private LogParams logging;
    @XmlElement(name = "pa")
    private ReceiptParams receiptParams;

    public FDDesign getFdDesign() {
        return fdDesign;
    }

    public void setFdDesign(FDDesign fdDesign) {
        this.fdDesign = fdDesign;
    }

    public LogParams getLogging() {
        return logging;
    }

    public void setLogging(LogParams logging) {
        this.logging = logging;
    }

    public ReceiptParams getReceiptParams() {
        return receiptParams;
    }

    public void setReceiptParams(ReceiptParams receiptParams) {
        this.receiptParams = receiptParams;
    }
}
