package jp.co.glory.bruebox;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import ru.crystals.pos.cash_machine.Response;
import ru.crystals.pos.cash_machine.entities.AbstractInventoryResponse;
import ru.crystals.pos.cash_machine.entities.interfaces.CashUnitInterface;
import ru.crystals.pos.cash_machine.entities.interfaces.DenominationInterface;
import ru.crystals.pos.currency.CurrencyUtil;

/**
 * <p>
 * Java class for InventoryResponseType complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="InventoryResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}Id" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}SeqNo"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}User"/>
 *         &lt;element name="Cash" type="{http://www.glory.co.jp/bruebox.xsd}CashType" maxOccurs="2" minOccurs="0"/>
 *         &lt;element name="CashUnits" type="{http://www.glory.co.jp/bruebox.xsd}CashUnitsType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute ref="{http://www.glory.co.jp/bruebox.xsd}result"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "InventoryResponseType", propOrder = {"id", "seqNo", "user", "cash", "cashUnits"})
public class InventoryResponseType extends AbstractInventoryResponse {

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\tМонеты:\n");
        for (CashUnitInterface cashUnit : getCoinCashUnits()) {
            String value = cashUnit.getValueInf() % 100 == 0 ? cashUnit.getValueInf() / 100 + " р." : cashUnit.getValueInf() + " коп.";
            String count = String.valueOf(cashUnit.getCountInf()) + " шт.";
            String amount = CurrencyUtil.formatSum(cashUnit.getAmountInf()) + " р.";
            sb.append("\t\t" + value + " * " + count + " = " + amount + "\n");
        }
        sb.append("\tКупюры:\n");
        for (CashUnitInterface cashUnit : getBanknoteCashUnits()) {
            String value = cashUnit.getValueInf() % 100 == 0 ? cashUnit.getValueInf() / 100 + " р." : cashUnit.getValueInf() + " коп.";
            String count = String.valueOf(cashUnit.getCountInf()) + " шт.";
            String amount = CurrencyUtil.formatSum(cashUnit.getAmountInf()) + " р.";
            sb.append("\t\t" + value + " * " + count + " = " + amount + "\n");
        }
        sb.append("\tКассета:\n");
        for (DenominationInterface cashUnit : getCassete().getDenominationsInf()) {
            if (cashUnit.getPieceInf() > 0) {
                String value = cashUnit.getValueInf() % 100 == 0 ? cashUnit.getValueInf() / 100 + " р." : cashUnit.getValueInf() + " коп.";
                String count = String.valueOf(cashUnit.getPieceInf()) + " шт.";
                String amount = CurrencyUtil.formatSum(cashUnit.getAmountInf()) + " р.";
                sb.append("\t\t" + value + " * " + count + " = " + amount + "\n");
            }
        }
        sb.append("\tВсего: " + CurrencyUtil.formatSum(getCassete().getAmountInf()) + " р.\n");
        sb.append("\tМиксер:\n");
        for (DenominationInterface cashUnit : getMixer().getDenominationsInf()) {
            if (cashUnit.getPieceInf() > 0) {
                String value = cashUnit.getValueInf() % 100 == 0 ? cashUnit.getValueInf() / 100 + " р." : cashUnit.getValueInf() + " коп.";
                String count = String.valueOf(cashUnit.getPieceInf()) + " шт.";
                String amount = CurrencyUtil.formatSum(cashUnit.getAmountInf()) + " р.";
                sb.append("\t\t" + value + " * " + count + " = " + amount + "\n");
            }
        }
        sb.append("\tВсего: " + CurrencyUtil.formatSum(getMixer().getAmountInf()) + " р.\n");
        sb.append("----------------------------------------------------\n");
        sb.append("ИТОГО: " + CurrencyUtil.formatSum(getAmount()) + " р.\n");
        return sb.toString();
    }

    @XmlElement(name = "Id")
    protected String id;
    @XmlElement(name = "SeqNo", required = true)
    protected String seqNo;
    @XmlElement(name = "User", required = true)
    protected String user;
    @XmlElement(name = "Cash", namespace = "")
    protected List<CashType> cash;
    @XmlElement(name = "CashUnits", namespace = "")
    protected List<CashUnitsType> cashUnits;
    @XmlAttribute(namespace = "http://www.glory.co.jp/bruebox.xsd")
    protected BigInteger result;
    private Response response;

    /**
     * Gets the value of the id property.
     * 
     * @return possible object is {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *            allowed object is {@link String }
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the seqNo property.
     * 
     * @return possible object is {@link String }
     */
    public String getSeqNo() {
        return seqNo;
    }

    /**
     * Sets the value of the seqNo property.
     * 
     * @param value
     *            allowed object is {@link String }
     */
    public void setSeqNo(String value) {
        this.seqNo = value;
    }

    /**
     * Gets the value of the user property.
     * 
     * @return possible object is {@link String }
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets the value of the user property.
     * 
     * @param value
     *            allowed object is {@link String }
     */
    public void setUser(String value) {
        this.user = value;
    }

    /**
     * Gets the value of the cash property.
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
     * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the cash property.
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getCash().add(newItem);
     * </pre>
     * <p>
     * Objects of the following type(s) are allowed in the list {@link CashType }
     */
    @Override
    public List<CashType> getCash() {
        if (cash == null) {
            cash = new ArrayList<CashType>();
        }
        return this.cash;
    }

    public void setCash(List<CashType> cash) {
        this.cash = cash;
    }

    /**
     * Gets the value of the cashUnits property.
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
     * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the cashUnits property.
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getCashUnits().add(newItem);
     * </pre>
     * <p>
     * Objects of the following type(s) are allowed in the list {@link CashUnitsType }
     */
    @Override
    public List<CashUnitsType> getCashUnits() {
        if (cashUnits == null) {
            cashUnits = new ArrayList<CashUnitsType>();
        }
        return this.cashUnits;
    }

    public void setCashUnits(List<CashUnitsType> cashUnits) {
        this.cashUnits = cashUnits;
    }
    /**
     * Gets the value of the result property.
     * 
     * @return possible object is {@link BigInteger }
     */
    public BigInteger getResult() {
        return result;
    }

    /**
     * Sets the value of the result property.
     * 
     * @param value
     *            allowed object is {@link BigInteger }
     */
    public void setResult(BigInteger value) {
        this.result = value;
    }

    /* end of factory class */

    @Override
    public CashUnitInterface getNewCashUnit() {
        return new CashUnitType();
    }

    @Override
    public Response getResponse() {
        return response;
    }

    @Override
    public void setResponse(Response response) {
        this.response = response;
    }

}
