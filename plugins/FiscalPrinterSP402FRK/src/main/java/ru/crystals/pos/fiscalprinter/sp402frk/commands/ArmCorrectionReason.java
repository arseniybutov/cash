package ru.crystals.pos.fiscalprinter.sp402frk.commands;

import ru.crystals.pos.fiscalprinter.sp402frk.support.KKTDataType;
import ru.crystals.pos.fiscalprinter.sp402frk.transport.RequestElement;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Елемент команды печати чека коррекции содержащей реквизиты причины коррекции.
 */
@XmlRootElement(name = "pa")
@XmlAccessorType(XmlAccessType.NONE)
public class ArmCorrectionReason {
    @XmlAttribute(name = "n")
    private String name = "1174";
    @XmlAttribute(name = "t")
    private String type = "7";

    @XmlElement(name = "pa")
    private RequestElement reasonStr = new RequestElement("1177", KKTDataType.STRING, "");
    @XmlElement(name = "pa")
    private RequestElement docDate = new RequestElement("1178", KKTDataType.DATE_TIME, "");
    @XmlElement(name = "pa")
    private RequestElement docNumber = new RequestElement("1179", KKTDataType.STRING, "");

    public String getReasonStr() {
        return reasonStr.getValue();
    }

    public void setReasonStr(String reasonStr) {
        this.reasonStr.setValue(reasonStr);
    }

    public String getDocDate() {
        return docDate.getValue();
    }

    public void setDocDate(String docDate) {
        this.docDate.setValue(docDate);
    }

    public String getDocNumber() {
        return docNumber.getValue();
    }

    public void setDocNumber(String docNumber) {
        this.docNumber.setValue(docNumber);
    }
}
