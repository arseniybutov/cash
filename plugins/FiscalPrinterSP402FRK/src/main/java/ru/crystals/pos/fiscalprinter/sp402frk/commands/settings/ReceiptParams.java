package ru.crystals.pos.fiscalprinter.sp402frk.commands.settings;

import ru.crystals.pos.fiscalprinter.sp402frk.support.KKTDataType;
import ru.crystals.pos.fiscalprinter.sp402frk.transport.RequestElement;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class ReceiptParams {
    @XmlAttribute(name = "n")
    private String name = "Receipt";
    @XmlAttribute(name = "t")
    private String type = "7";

    /**
     * Округление
     * true – применяется округление на сумму чека
     * false – округление не применяется
     */
    @XmlElement(name = "pa")
    private RequestElement discountOnChange = new RequestElement("DiscountOnChange", KKTDataType.STRING, "false");

    public String getDiscountOnChange() {
        return discountOnChange.getValue();
    }

    public void setDiscountOnChange(String discountOnChange) {
        this.discountOnChange.setValue(discountOnChange);
    }
}
