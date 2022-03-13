package ru.crystals.pos.fiscalprinter.sp402frk.commands;

import ru.crystals.pos.fiscalprinter.sp402frk.support.KKTDataType;
import ru.crystals.pos.fiscalprinter.sp402frk.transport.RequestElement;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Данные команды печати отчёта о текущем состоянии расчётов
 */
@XmlRootElement(name = "pa")
@XmlAccessorType(XmlAccessType.NONE)
public class PrintFNReport {
    @XmlAttribute(name = "n")
    private String name = "21";
    @XmlAttribute(name = "t")
    private String type = "7";

    @XmlElement(name = "pa")
    private RequestElement userLoginName = new RequestElement("1021", KKTDataType.STRING, "");

    public String getUserLoginName() {
        return userLoginName.getValue();
    }

    public void setUserLoginName(String userLoginName) {
        this.userLoginName.setValue(userLoginName);
    }
}
