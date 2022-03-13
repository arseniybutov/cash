
package jp.co.glory.bruebox;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RegisterEventRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RegisterEventRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}Id" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}SeqNo"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}SessionID" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}Url"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}Port" minOccurs="0"/>
 *         &lt;element name="DestinationType" type="{http://www.glory.co.jp/bruebox.xsd}StatusOptionType" minOccurs="0"/>
 *         &lt;element name="RequireEventList" type="{http://www.glory.co.jp/bruebox.xsd}RequireEventListType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RegisterEventRequestType", propOrder = {
    "id",
    "seqNo",
    "sessionID",
    "url",
    "port",
    "destinationType",
    "requireEventList"
})
public class RegisterEventRequestType {

    @XmlElement(name = "Id")
    protected String id;
    @XmlElement(name = "SeqNo", required = true)
    protected String seqNo;
    @XmlElement(name = "SessionID")
    protected String sessionID;
    @XmlElement(name = "Url", required = true)
    protected String url;
    @XmlElement(name = "Port")
    protected BigInteger port;
    @XmlElement(name = "DestinationType", namespace = "")
    protected StatusOptionType destinationType;
    @XmlElement(name = "RequireEventList", namespace = "")
    protected RequireEventListType requireEventList;

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
     * Gets the value of the url property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the value of the url property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUrl(String value) {
        this.url = value;
    }

    /**
     * Gets the value of the port property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getPort() {
        return port;
    }

    /**
     * Sets the value of the port property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setPort(BigInteger value) {
        this.port = value;
    }

    /**
     * Gets the value of the destinationType property.
     * 
     * @return
     *     possible object is
     *     {@link StatusOptionType }
     *     
     */
    public StatusOptionType getDestinationType() {
        return destinationType;
    }

    /**
     * Sets the value of the destinationType property.
     * 
     * @param value
     *     allowed object is
     *     {@link StatusOptionType }
     *     
     */
    public void setDestinationType(StatusOptionType value) {
        this.destinationType = value;
    }

    /**
     * Gets the value of the requireEventList property.
     * 
     * @return
     *     possible object is
     *     {@link RequireEventListType }
     *     
     */
    public RequireEventListType getRequireEventList() {
        return requireEventList;
    }

    /**
     * Sets the value of the requireEventList property.
     * 
     * @param value
     *     allowed object is
     *     {@link RequireEventListType }
     *     
     */
    public void setRequireEventList(RequireEventListType value) {
        this.requireEventList = value;
    }

}
