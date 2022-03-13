package ru.crystals.pos.fiscalprinter.sp402frk.commands;

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
 * Данные команды изъятия/внесения.
 */
@XmlRootElement(name = "pa")
@XmlAccessorType(XmlAccessType.NONE)
public class ArmMoneyOperation {
    public static final int CASH_IN = 1;
    public static final int CASH_OUT = 2;

    @XmlAttribute(name = "n")
    private String name = "200001";
    @XmlAttribute(name = "t")
    private String type = "7";

    @XmlElement(name = "pa")
    private RequestElement userLoginName = new RequestElement("1021", KKTDataType.STRING, "");
    @XmlElement(name = "pa")
    private RequestElement cashSumm = new RequestElement("CashSumm", KKTDataType.CURRENCY, "0.00");
    @XmlElement(name = "pa")
    private RequestElement footer = new RequestElement("Footer", KKTDataType.STRING, "");
    @XmlElement(name = "pa")
    private RequestElement headerText = new RequestElement("HeaderText", KKTDataType.STRING, "");
    /**
     * Тип операции: 1 – внесение, 2 – изъятие
     */
    @XmlElement(name = "pa")
    private RequestElement moneyOperationType = new RequestElement("MoneyOperationType", KKTDataType.UINT, "1");

    public String getUserLoginName() {
        return userLoginName.getValue();
    }

    public void setUserLoginName(String userLoginName) {
        this.userLoginName.setValue(userLoginName);
    }

    public BigDecimal getCashSumm() {
        return cashSumm.getFloatValue();
    }

    public void setCashSumm(BigDecimal cashSumm) {
        this.cashSumm.setFloatValue(UtilsSP.roundBigDecimal(cashSumm, KKTDataType.CURRENCY));
    }

    public void addHeaderText(String text) {
        this.headerText.setValue(headerText.getValue() + text);
    }

    public String getHeaderText() {
        return headerText.getValue();
    }

    public void setHeaderText(String headerText) {
        this.headerText.setValue(headerText);
    }

    public String getFooter() {
        return footer.getValue();
    }

    public void setFooter(String footer) {
        this.footer.setValue(footer);
    }

    public int getMoneyOperationType() {
        return moneyOperationType.getIntValue();
    }

    public void setMoneyOperationType(int moneyOperationType) {
        this.moneyOperationType.setIntValue(moneyOperationType);
    }
}
