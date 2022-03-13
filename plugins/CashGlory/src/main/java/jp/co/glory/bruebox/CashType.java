package jp.co.glory.bruebox;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import ru.crystals.pos.cash_machine.entities.AbstractCash;
import ru.crystals.pos.cash_machine.entities.interfaces.DenominationInterface;

/**
 * <p>
 * Java class for CashType complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CashType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Denomination" type="{http://www.glory.co.jp/bruebox.xsd}DenominationType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://www.glory.co.jp/bruebox.xsd}CashAttribGroup"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "CashType", propOrder = {"denomination"})
public class CashType extends AbstractCash {

    @XmlElement(name = "Denomination", namespace = "")
    protected List<DenominationType> denomination;
    @XmlAttribute(namespace = "http://www.glory.co.jp/bruebox.xsd", required = true)
    protected BigInteger type;

    /**
     * Gets the value of the denomination property.
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
     * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the denomination property.
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getDenomination().add(newItem);
     * </pre>
     * <p>
     * Objects of the following type(s) are allowed in the list {@link DenominationType }
     */
    public List<DenominationType> getDenomination() {
        if (denomination == null) {
            denomination = new ArrayList<DenominationType>();
        }
        return this.denomination;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return possible object is {@link BigInteger }
     */
    public BigInteger getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *            allowed object is {@link BigInteger }
     */
    public void setType(BigInteger value) {
        this.type = value;
    }

    //end of factory class

    @Override
    public List< ? extends DenominationInterface> getDenomintaions() {
        return getDenomination();
    }

    @Override
    public int getTypeInf() {
        return type.intValue();
    }

    @Override
    public void addDenomination(DenominationInterface denomintion) {
        getDenomination().add((DenominationType) denomintion);
    }

    public void setDenomination(List<DenominationType> denom) {
        this.denomination = denom;
    }

    @Override
    public boolean isDispensable() {
        return (getTypeInf() == 4);
    }

}
