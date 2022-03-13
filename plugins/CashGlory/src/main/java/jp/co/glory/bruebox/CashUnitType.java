package jp.co.glory.bruebox;

import ru.crystals.pos.cash_machine.entities.AbstractCashUnit;
import ru.crystals.pos.cash_machine.entities.interfaces.DenominationInterface;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Java class for CashUnitType complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CashUnitType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Denomination" type="{http://www.glory.co.jp/bruebox.xsd}DenominationType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://www.glory.co.jp/bruebox.xsd}CashUnitAttribGroup"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "CashUnitType", propOrder = {"denomination"})
public class CashUnitType extends AbstractCashUnit {
    private static final long serialVersionUID = 1L;
    @XmlElement(name = "Denomination", namespace = "")
    protected List<DenominationType> denomination;
    @XmlAttribute(namespace = "http://www.glory.co.jp/bruebox.xsd")
    protected BigInteger unitno;
    @XmlAttribute(namespace = "http://www.glory.co.jp/bruebox.xsd")
    protected BigInteger st;
    @XmlAttribute(namespace = "http://www.glory.co.jp/bruebox.xsd")
    protected BigInteger nf;
    @XmlAttribute(namespace = "http://www.glory.co.jp/bruebox.xsd")
    protected BigInteger ne;
    @XmlAttribute(namespace = "http://www.glory.co.jp/bruebox.xsd")
    protected BigInteger max;

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

    public void addDenomination(DenominationType denomination) {
        getDenomination().add(denomination);
    }

    /**
     * Gets the value of the unitno property.
     * 
     * @return possible object is {@link BigInteger }
     */
    public BigInteger getUnitno() {
        return unitno;
    }

    /**
     * Sets the value of the unitno property.
     * 
     * @param value
     *            allowed object is {@link BigInteger }
     */
    public void setUnitno(BigInteger value) {
        this.unitno = value;
    }

    /**
     * Gets the value of the st property.
     * 
     * @return possible object is {@link BigInteger }
     */
    public BigInteger getSt() {
        return st;
    }

    /**
     * Sets the value of the st property.
     * 
     * @param value
     *            allowed object is {@link BigInteger }
     */
    public void setSt(BigInteger value) {
        this.st = value;
    }

    /**
     * Gets the value of the nf property.
     * 
     * @return possible object is {@link BigInteger }
     */
    public BigInteger getNf() {
        return nf;
    }

    /**
     * Sets the value of the nf property.
     * 
     * @param value
     *            allowed object is {@link BigInteger }
     */
    public void setNf(BigInteger value) {
        this.nf = value;
    }

    /**
     * Gets the value of the ne property.
     * 
     * @return possible object is {@link BigInteger }
     */
    public BigInteger getNe() {
        return ne;
    }

    /**
     * Sets the value of the ne property.
     * 
     * @param value
     *            allowed object is {@link BigInteger }
     */
    public void setNe(BigInteger value) {
        this.ne = value;
    }

    /**
     * Gets the value of the max property.
     * 
     * @return possible object is {@link BigInteger }
     */
    public BigInteger getMax() {
        return max;
    }

    /**
     * Sets the value of the max property.
     * 
     * @param value
     *            allowed object is {@link BigInteger }
     */
    public void setMax(BigInteger value) {
        this.max = value;
    }

    /* end of factory class */

    @Override
    public int getNearFullInf() {
        return nf.intValue();
    }

    @Override
    public int getNearEmptyInf() {
        return ne.intValue();
    }

    @Override
    public int getMaxInf() {
        return max.intValue();
    }

    @Override
    public List< ? extends DenominationInterface> getDenominationsInf() {
        return denomination;
    }

    @Override
    public String getCurrencyInf() {
        if (getDenominationsInf().size() == 1) {
            return getDenominationsInf().get(0).getCurrencyInf();
        }
        return null;
    }

    @Override
    public Long getValueInf() {
        if (getDenominationsInf() != null && getDenominationsInf().size() == 1) {
            return getDenominationsInf().get(0).getValueInf();
        }
        return null;
    }

    @Override
    public boolean isCassete() {
        int unitno = this.unitno.intValue();
        if (isBanknote()) {
            return unitno == 4057 || unitno == 4058 || unitno == 4059 || unitno == 4060;
        } else {
            return unitno == 4084 || unitno == 4165;
        }
    }

    @Override
    public void setMaxInf(int maxInf) {
        this.max = BigInteger.valueOf(maxInf);
    }

    @Override
    public void setNearEmptyInf(int nearEmptyInf) {
        this.ne = BigInteger.valueOf(nearEmptyInf);
    }

    @Override
    public void setNearFullInf(int nearFullInf) {
        this.nf = BigInteger.valueOf(nearFullInf);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setDenominationsInf(List< ? extends DenominationInterface> denominationsInf) {
        this.denomination = (List<DenominationType>) denominationsInf;
    }

    @Override
    public Integer getUnitnoInf() {
        return unitno.intValue();
    }

    @Override
    public void setUnitnoInf(Integer unitnoInf) {
        unitno = BigInteger.valueOf(unitnoInf);
    }

}
