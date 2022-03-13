
package jp.co.glory.bruebox;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CollectRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CollectRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}Id" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}SeqNo"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}SessionID" minOccurs="0"/>
 *         &lt;element name="Option" type="{http://www.glory.co.jp/bruebox.xsd}CollectOptionType"/>
 *         &lt;element name="Mix" type="{http://www.glory.co.jp/bruebox.xsd}CollectOptionType" minOccurs="0"/>
 *         &lt;element name="IFCassette" type="{http://www.glory.co.jp/bruebox.xsd}CollectOptionType" minOccurs="0"/>
 *         &lt;element name="RequireVerification" type="{http://www.glory.co.jp/bruebox.xsd}RequireVerificationType" minOccurs="0"/>
 *         &lt;element name="Partial" type="{http://www.glory.co.jp/bruebox.xsd}CollectPartialType" minOccurs="0"/>
 *         &lt;element name="Cash" type="{http://www.glory.co.jp/bruebox.xsd}CashType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CollectRequestType", propOrder = {
    "id",
    "seqNo",
    "sessionID",
    "option",
    "mix",
    "ifCassette",
    "requireVerification",
    "partial",
    "cash"
})
public class CollectRequestType {

    @XmlElement(name = "Id")
    protected String id;
    @XmlElement(name = "SeqNo", required = true)
    protected String seqNo;
    @XmlElement(name = "SessionID")
    protected String sessionID;
    @XmlElement(name = "Option", namespace = "", required = true)
    protected CollectOptionType option;
    @XmlElement(name = "Mix", namespace = "")
    protected CollectOptionType mix;
    @XmlElement(name = "IFCassette", namespace = "")
    protected CollectOptionType ifCassette;
    @XmlElement(name = "RequireVerification", namespace = "")
    protected RequireVerificationType requireVerification;
    @XmlElement(name = "Partial", namespace = "")
    protected CollectPartialType partial;
    @XmlElement(name = "Cash", namespace = "", required = true)
    protected CashType cash;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the seqNo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSeqNo() {
        return seqNo;
    }

    /**
     * Sets the value of the seqNo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSeqNo(String value) {
        this.seqNo = value;
    }

    /**
     * Gets the value of the sessionID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSessionID() {
        return sessionID;
    }

    /**
     * Sets the value of the sessionID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSessionID(String value) {
        this.sessionID = value;
    }

    /**
     * Gets the value of the option property.
     * 
     * @return
     *     possible object is
     *     {@link CollectOptionType }
     *     
     */
    public CollectOptionType getOption() {
        return option;
    }

    /**
     * Sets the value of the option property.
     * 
     * @param value
     *     allowed object is
     *     {@link CollectOptionType }
     *     
     */
    public void setOption(CollectOptionType value) {
        this.option = value;
    }

    /**
     * Gets the value of the mix property.
     * 
     * @return
     *     possible object is
     *     {@link CollectOptionType }
     *     
     */
    public CollectOptionType getMix() {
        return mix;
    }

    /**
     * Sets the value of the mix property.
     * 
     * @param value
     *     allowed object is
     *     {@link CollectOptionType }
     *     
     */
    public void setMix(CollectOptionType value) {
        this.mix = value;
    }

    /**
     * Gets the value of the ifCassette property.
     * 
     * @return
     *     possible object is
     *     {@link CollectOptionType }
     *     
     */
    public CollectOptionType getIFCassette() {
        return ifCassette;
    }

    /**
     * Sets the value of the ifCassette property.
     * 
     * @param value
     *     allowed object is
     *     {@link CollectOptionType }
     *     
     */
    public void setIFCassette(CollectOptionType value) {
        this.ifCassette = value;
    }

    /**
     * Gets the value of the requireVerification property.
     * 
     * @return
     *     possible object is
     *     {@link RequireVerificationType }
     *     
     */
    public RequireVerificationType getRequireVerification() {
        return requireVerification;
    }

    /**
     * Sets the value of the requireVerification property.
     * 
     * @param value
     *     allowed object is
     *     {@link RequireVerificationType }
     *     
     */
    public void setRequireVerification(RequireVerificationType value) {
        this.requireVerification = value;
    }

    /**
     * Gets the value of the partial property.
     * 
     * @return
     *     possible object is
     *     {@link CollectPartialType }
     *     
     */
    public CollectPartialType getPartial() {
        return partial;
    }

    /**
     * Sets the value of the partial property.
     * 
     * @param value
     *     allowed object is
     *     {@link CollectPartialType }
     *     
     */
    public void setPartial(CollectPartialType value) {
        this.partial = value;
    }

    /**
     * Gets the value of the cash property.
     * 
     * @return
     *     possible object is
     *     {@link CashType }
     *     
     */
    public CashType getCash() {
        return cash;
    }

    /**
     * Sets the value of the cash property.
     * 
     * @param value
     *     allowed object is
     *     {@link CashType }
     *     
     */
    public void setCash(CashType value) {
        this.cash = value;
    }

}
