package ru.crystals.pos.fiscalprinter.sp402frk.commands;

import ru.crystals.pos.fiscalprinter.sp402frk.support.KKTDataType;
import ru.crystals.pos.fiscalprinter.sp402frk.transport.RequestElement;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Данные команды установки времени
 */
@XmlRootElement(name = "pa")
@XmlAccessorType(XmlAccessType.NONE)
public class SetDateTime {
    @XmlAttribute(name = "n")
    private String name = "200001";
    @XmlAttribute(name = "t")
    private String type = "7";

    @XmlElement(name = "pa")
    private RequestElement dateTime = new RequestElement("DateTime", KKTDataType.DATE_TIME, "");

    public String getDateTime() {
        return dateTime.getValue();
    }

    public void setDateTime(String dateTime) {
        this.dateTime.setValue(dateTime);
    }
}
