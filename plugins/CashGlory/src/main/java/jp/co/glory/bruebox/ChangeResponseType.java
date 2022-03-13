package jp.co.glory.bruebox;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for ChangeResponseType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ChangeResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}Id" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}SeqNo"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}User"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}Amount"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}ManualDeposit"/>
 *         &lt;element name="DepositCurrency" type="{http://www.glory.co.jp/bruebox.xsd}DepositCurrencyType" minOccurs="0"/>
 *         &lt;element name="Status" type="{http://www.glory.co.jp/bruebox.xsd}StatusType"/>
 *         &lt;element name="Cash" type="{http://www.glory.co.jp/bruebox.xsd}CashType" maxOccurs="2" minOccurs="2"/>
 *       &lt;/sequence>
 *       &lt;attribute ref="{http://www.glory.co.jp/bruebox.xsd}result"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChangeResponseType", propOrder = {
        "id",
        "seqNo",
        "user",
        "amount",
        "manualDeposit",
        "depositCurrency",
        "status",
        "cash"
})
public class ChangeResponseType {

    @XmlElement(name = "Id")
    protected String id;
    @XmlElement(name = "SeqNo", required = true)
    protected String seqNo;
    @XmlElement(name = "User", required = true)
    protected String user;
    @XmlElement(name = "Amount", required = true)
    protected String amount;
    @XmlElement(name = "ManualDeposit", required = true)
    protected String manualDeposit;
    @XmlElement(name = "DepositCurrency", namespace = "")
    protected DepositCurrencyType depositCurrency;
    @XmlElement(name = "Status", namespace = "", required = true)
    protected StatusType status;
    @XmlElement(name = "Cash", namespace = "", required = true)
    protected List<CashType> cash;
    @XmlAttribute(namespace = "http://www.glory.co.jp/bruebox.xsd")
    protected BigInteger result;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *         possible object is {@link String }
     * 
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *        allowed object is {@link String }
     * 
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the seqNo property.
     * 
     * @return
     *         possible object is {@link String }
     * 
     */
    public String getSeqNo() {
        return seqNo;
    }

    /**
     * Sets the value of the seqNo property.
     * 
     * @param value
     *        allowed object is {@link String }
     * 
     */
    public void setSeqNo(String value) {
        this.seqNo = value;
    }

    /**
     * Gets the value of the user property.
     * 
     * @return
     *         possible object is {@link String }
     * 
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets the value of the user property.
     * 
     * @param value
     *        allowed object is {@link String }
     * 
     */
    public void setUser(String value) {
        this.user = value;
    }

    /**
     * Gets the value of the amount property.
     * 
     * @return
     *         possible object is {@link String }
     * 
     */
    public String getAmount() {
        return amount;
    }

    /**
     * Sets the value of the amount property.
     * 
     * @param value
     *        allowed object is {@link String }
     * 
     */
    public void setAmount(String value) {
        this.amount = value;
    }

    /**
     * Gets the value of the manualDeposit property.
     * 
     * @return
     *         possible object is {@link String }
     * 
     */
    public String getManualDeposit() {
        return manualDeposit;
    }

    /**
     * Sets the value of the manualDeposit property.
     * 
     * @param value
     *        allowed object is {@link String }
     * 
     */
    public void setManualDeposit(String value) {
        this.manualDeposit = value;
    }

    /**
     * Gets the value of the depositCurrency property.
     * 
     * @return
     *         possible object is {@link DepositCurrencyType }
     * 
     */
    public DepositCurrencyType getDepositCurrency() {
        return depositCurrency;
    }

    /**
     * Sets the value of the depositCurrency property.
     * 
     * @param value
     *        allowed object is {@link DepositCurrencyType }
     * 
     */
    public void setDepositCurrency(DepositCurrencyType value) {
        this.depositCurrency = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *         possible object is {@link StatusType }
     * 
     */
    public StatusType getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *        allowed object is {@link StatusType }
     * 
     */
    public void setStatus(StatusType value) {
        this.status = value;
    }

    /**
     * Gets the value of the cash property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be present
     * inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the cash property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getCash().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link CashType }
     * 
     * 
     */
    public List<CashType> getCash() {
        if (cash == null) {
            cash = new ArrayList<CashType>();
        }
        return this.cash;
    }

    /**
     * Gets the value of the result property.
     * 
     * @return
     *         possible object is {@link BigInteger }
     * 
     */
    public BigInteger getResult() {
        return result;
    }

    /**
     * Sets the value of the result property.
     * 
     * @param value
     *        allowed object is {@link BigInteger }
     * 
     */
    public void setResult(BigInteger value) {
        this.result = value;
    }

    public void setCash(List<CashType> cash) {
        this.cash = cash;
    }

}
