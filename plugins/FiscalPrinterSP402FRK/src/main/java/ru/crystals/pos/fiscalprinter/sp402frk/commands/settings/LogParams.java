package ru.crystals.pos.fiscalprinter.sp402frk.commands.settings;

import ru.crystals.pos.fiscalprinter.sp402frk.support.KKTDataType;
import ru.crystals.pos.fiscalprinter.sp402frk.transport.RequestElement;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class LogParams {
    @XmlAttribute(name = "n")
    private String name = "Logging";
    @XmlAttribute(name = "t")
    private String type = "7";

    /**
     * 0 - Максимально подробно (debug)
     * 1 - По умолчанию (info)
     */
    @XmlElement(name = "pa")
    private RequestElement logLevel = new RequestElement("UseItemFreeString", KKTDataType.STRING, "1");

    public String getLogLevel() {
        return logLevel.getValue();
    }

    public void setLogLevel(String logLevel) {
        this.logLevel.setValue(logLevel);
    }
}
