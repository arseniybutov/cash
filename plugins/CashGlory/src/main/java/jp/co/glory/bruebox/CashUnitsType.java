package jp.co.glory.bruebox;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import ru.crystals.pos.cash_machine.entities.interfaces.CashUnitInterface;
import ru.crystals.pos.cash_machine.entities.interfaces.CashUnitsInterface;

/**
 * <p>
 * Java class for CashUnitsType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CashUnitsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CashUnit" type="{http://www.glory.co.jp/bruebox.xsd}CashUnitType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://www.glory.co.jp/bruebox.xsd}CashUnitsAttribGroup"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "CashUnitsType", propOrder = {
        "cashUnit"
})
public class CashUnitsType implements CashUnitsInterface {

    @XmlElement(name = "CashUnit", namespace = "")
    protected List<CashUnitType> cashUnit;
    @XmlAttribute(namespace = "http://www.glory.co.jp/bruebox.xsd")
    protected BigInteger devid;
    private ArrayList<CashUnitInterface> cashUnits;

    /**
     * Gets the value of the cashUnit property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be present
     * inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the cashUnit property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getCashUnit().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link CashUnitType }
     * 
     * 
     */
    public List<CashUnitType> getCashUnit() {
        if (cashUnit == null) {
            cashUnit = new ArrayList<CashUnitType>();
        }
        return this.cashUnit;
    }

    public void addCashUnit(CashUnitType cashUnit) {
        getCashUnit().add(cashUnit);
    }

    /**
     * Gets the value of the devid property.
     * 
     * @return
     *         possible object is {@link BigInteger }
     * 
     */
    public BigInteger getDevid() {
        return devid;
    }

    /**
     * Sets the value of the devid property.
     * 
     * @param value
     *        allowed object is {@link BigInteger }
     * 
     */
    public void setDevid(BigInteger value) {
        this.devid = value;
    }

    //end of factory class

    @Override
    public List<? extends CashUnitInterface> getCashUnits() {
        if (this.cashUnits == null) {
            ArrayList<CashUnitInterface> list = new ArrayList<CashUnitInterface>();
            for (CashUnitType c : cashUnit) {
                if (!c.isCassete()) {
                    list.add(c);
                }
            }
            cashUnits = list;
        }
        return cashUnits;
    }

    @Override
    public boolean isBanknotes() {
        return devid.intValue() == 1;
    }

    @Override
    public boolean isCoins() {
        return devid.intValue() == 2;
    }

    @Override
    public List<? extends CashUnitInterface> getAllCashUnits() {
        return cashUnit;
    }

    @Override
    public Integer getDevidInf() {
        return devid.intValue();
    }

}
