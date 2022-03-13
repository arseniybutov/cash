package ru.crystals.pos.fiscalprinter.sp402frk.commands;

import ru.crystals.pos.fiscalprinter.sp402frk.support.KKTDataType;
import ru.crystals.pos.fiscalprinter.sp402frk.transport.RequestElement;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Данные команды открытия смены
 */
@XmlRootElement(name = "pa")
@XmlAccessorType(XmlAccessType.NONE)
public class ShiftOpen {
    @XmlAttribute(name = "n")
    private String name = "2";
    @XmlAttribute(name = "t")
    private String type = "7";

    /**
     * Имя кассира
     */
    @XmlElement(name = "pa")
    private RequestElement userLoginName = new RequestElement("1021", KKTDataType.STRING, "");
    /**
     * ИНН кассира
     */
    @XmlElement(name = "pa")
    private RequestElement userINN = new RequestElement("1203", KKTDataType.STRING, "");

    public String getUserINN() {
        return (String) userINN.getValue();
    }

    public void setUserINN(String userINN) {
        this.userINN.setValue(userINN);
    }

    public String getUserLoginName() {
        return userLoginName.getValue();
    }

    public void setUserLoginName(String userLoginName) {
        this.userLoginName.setValue(userLoginName);
    }

}
