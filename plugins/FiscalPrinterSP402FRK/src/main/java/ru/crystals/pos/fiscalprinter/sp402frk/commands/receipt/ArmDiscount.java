package ru.crystals.pos.fiscalprinter.sp402frk.commands.receipt;

import ru.crystals.pos.fiscalprinter.sp402frk.support.KKTDataType;
import ru.crystals.pos.fiscalprinter.sp402frk.transport.RequestElement;
import ru.crystals.pos.fiscalprinter.sp402frk.utils.UtilsSP;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.math.BigDecimal;

/**
 * Елемент команды печати чека содержащий скидку, не используется т.к. скидки кассовые.
 */
@XmlRootElement(name = "pa")
@XmlAccessorType(XmlAccessType.NONE)
public class ArmDiscount {
    @XmlAttribute(name = "n")
    private String name = "ItemDiscounts";
    @XmlAttribute(name = "t")
    private String type = "7";

    @XmlElement(name = "pa")
    private RequestElement discName = new RequestElement("Name", KKTDataType.STRING, "");
    /**
     * Сумма скидки/наценки: скидка отрицательная, наценка – положительная
     */
    @XmlElement(name = "pa")
    private RequestElement value = new RequestElement("Value", KKTDataType.CURRENCY, "0.00");

    public String getDiscName() {
        return (String) discName.getValue();
    }

    public void setDiscName(String discName) {
        this.discName.setValue(discName);
    }

    public BigDecimal getValue() {
        return value.getFloatValue();
    }

    public void setValue(BigDecimal value) {
        this.value.setFloatValue(UtilsSP.roundBigDecimal(value, KKTDataType.CURRENCY));
    }
}
