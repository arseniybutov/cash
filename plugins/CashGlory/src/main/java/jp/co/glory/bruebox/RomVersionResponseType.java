
package jp.co.glory.bruebox;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RomVersionResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RomVersionResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}Id" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}SeqNo"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}User"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}CUTE"/>
 *         &lt;element name="RBW10" type="{http://www.glory.co.jp/bruebox.xsd}RBW10RomVerType" minOccurs="0"/>
 *         &lt;element name="RCW8X" type="{http://www.glory.co.jp/bruebox.xsd}RCW8XRomVerType" minOccurs="0"/>
 *         &lt;element name="RZ50" type="{http://www.glory.co.jp/bruebox.xsd}RZ50RomVerType" minOccurs="0"/>
 *         &lt;element name="RZ100" type="{http://www.glory.co.jp/bruebox.xsd}RZ100RomVerType" minOccurs="0"/>
 *         &lt;element name="RBW100" type="{http://www.glory.co.jp/bruebox.xsd}RBW100RomVerType" minOccurs="0"/>
 *         &lt;element name="RCW100" type="{http://www.glory.co.jp/bruebox.xsd}RCW100RomVerType" minOccurs="0"/>
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
@XmlType(name = "RomVersionResponseType", propOrder = {
    "id",
    "seqNo",
    "user",
    "cute",
    "rbw10",
    "rcw8X",
    "rz50",
    "rz100",
    "rbw100",
    "rcw100"
})
public class RomVersionResponseType {

    @XmlElement(name = "Id")
    protected String id;
    @XmlElement(name = "SeqNo", required = true)
    protected String seqNo;
    @XmlElement(name = "User", required = true)
    protected String user;
    @XmlElement(name = "CUTE", required = true)
    protected String cute;
    @XmlElement(name = "RBW10", namespace = "")
    protected RBW10RomVerType rbw10;
    @XmlElement(name = "RCW8X", namespace = "")
    protected RCW8XRomVerType rcw8X;
    @XmlElement(name = "RZ50", namespace = "")
    protected RZ50RomVerType rz50;
    @XmlElement(name = "RZ100", namespace = "")
    protected RZ100RomVerType rz100;
    @XmlElement(name = "RBW100", namespace = "")
    protected RBW100RomVerType rbw100;
    @XmlElement(name = "RCW100", namespace = "")
    protected RCW100RomVerType rcw100;
    @XmlAttribute(namespace = "http://www.glory.co.jp/bruebox.xsd")
    protected BigInteger result;

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
     * Gets the value of the user property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets the value of the user property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUser(String value) {
        this.user = value;
    }

    /**
     * Gets the value of the cute property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCUTE() {
        return cute;
    }

    /**
     * Sets the value of the cute property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCUTE(String value) {
        this.cute = value;
    }

    /**
     * Gets the value of the rbw10 property.
     * 
     * @return
     *     possible object is
     *     {@link RBW10RomVerType }
     *     
     */
    public RBW10RomVerType getRBW10() {
        return rbw10;
    }

    /**
     * Sets the value of the rbw10 property.
     * 
     * @param value
     *     allowed object is
     *     {@link RBW10RomVerType }
     *     
     */
    public void setRBW10(RBW10RomVerType value) {
        this.rbw10 = value;
    }

    /**
     * Gets the value of the rcw8X property.
     * 
     * @return
     *     possible object is
     *     {@link RCW8XRomVerType }
     *     
     */
    public RCW8XRomVerType getRCW8X() {
        return rcw8X;
    }

    /**
     * Sets the value of the rcw8X property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCW8XRomVerType }
     *     
     */
    public void setRCW8X(RCW8XRomVerType value) {
        this.rcw8X = value;
    }

    /**
     * Gets the value of the rz50 property.
     * 
     * @return
     *     possible object is
     *     {@link RZ50RomVerType }
     *     
     */
    public RZ50RomVerType getRZ50() {
        return rz50;
    }

    /**
     * Sets the value of the rz50 property.
     * 
     * @param value
     *     allowed object is
     *     {@link RZ50RomVerType }
     *     
     */
    public void setRZ50(RZ50RomVerType value) {
        this.rz50 = value;
    }

    /**
     * Gets the value of the rz100 property.
     * 
     * @return
     *     possible object is
     *     {@link RZ100RomVerType }
     *     
     */
    public RZ100RomVerType getRZ100() {
        return rz100;
    }

    /**
     * Sets the value of the rz100 property.
     * 
     * @param value
     *     allowed object is
     *     {@link RZ100RomVerType }
     *     
     */
    public void setRZ100(RZ100RomVerType value) {
        this.rz100 = value;
    }

    /**
     * Gets the value of the rbw100 property.
     * 
     * @return
     *     possible object is
     *     {@link RBW100RomVerType }
     *     
     */
    public RBW100RomVerType getRBW100() {
        return rbw100;
    }

    /**
     * Sets the value of the rbw100 property.
     * 
     * @param value
     *     allowed object is
     *     {@link RBW100RomVerType }
     *     
     */
    public void setRBW100(RBW100RomVerType value) {
        this.rbw100 = value;
    }

    /**
     * Gets the value of the rcw100 property.
     * 
     * @return
     *     possible object is
     *     {@link RCW100RomVerType }
     *     
     */
    public RCW100RomVerType getRCW100() {
        return rcw100;
    }

    /**
     * Sets the value of the rcw100 property.
     * 
     * @param value
     *     allowed object is
     *     {@link RCW100RomVerType }
     *     
     */
    public void setRCW100(RCW100RomVerType value) {
        this.rcw100 = value;
    }

    /**
     * Gets the value of the result property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getResult() {
        return result;
    }

    /**
     * Sets the value of the result property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setResult(BigInteger value) {
        this.result = value;
    }

}
