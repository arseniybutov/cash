
package jp.co.glory.bruebox;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for StatusRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StatusRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}Id" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}SeqNo"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}SessionID" minOccurs="0"/>
 *         &lt;element name="Option" type="{http://www.glory.co.jp/bruebox.xsd}StatusOptionType"/>
 *         &lt;element name="RequireVerification" type="{http://www.glory.co.jp/bruebox.xsd}RequireVerificationType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StatusRequestType", propOrder = {
    "id",
    "seqNo",
    "sessionID",
    "option",
    "requireVerification"
})
public class StatusRequestType {

    @XmlElement(name = "Id")
    protected String id;
    @XmlElement(name = "SeqNo", required = true)
    protected String seqNo;
    @XmlElement(name = "SessionID")
    protected String sessionID;
    @XmlElement(name = "Option", namespace = "", required = true)
    protected StatusOptionType option;
    @XmlElement(name = "RequireVerification", namespace = "")
    protected RequireVerificationType requireVerification;

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
     *     {@link StatusOptionType }
     *     
     */
    public StatusOptionType getOption() {
        return option;
    }

    /**
     * Sets the value of the option property.
     * 
     * @param value
     *     allowed object is
     *     {@link StatusOptionType }
     *     
     */
    public void setOption(StatusOptionType value) {
        this.option = value;
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

}
