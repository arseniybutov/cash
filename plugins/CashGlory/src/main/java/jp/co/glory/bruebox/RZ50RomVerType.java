
package jp.co.glory.bruebox;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RZ50RomVerType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RZ50RomVerType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}UP_APL" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}BV_CONTROL" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}BV_DL" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}BV_SET" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}BV_FORMAT" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}BV_PLD" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}LOW_APL" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}CST2" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}CST4" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}CST6" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}CST8" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}COLLECT" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RZ50RomVerType", propOrder = {
    "upapl",
    "bvcontrol",
    "bvdl",
    "bvset",
    "bvformat",
    "bvpld",
    "lowapl",
    "cst2",
    "cst4",
    "cst6",
    "cst8",
    "collect"
})
public class RZ50RomVerType {

    @XmlElement(name = "UP_APL")
    protected String upapl;
    @XmlElement(name = "BV_CONTROL")
    protected String bvcontrol;
    @XmlElement(name = "BV_DL")
    protected String bvdl;
    @XmlElement(name = "BV_SET")
    protected String bvset;
    @XmlElement(name = "BV_FORMAT")
    protected String bvformat;
    @XmlElement(name = "BV_PLD")
    protected String bvpld;
    @XmlElement(name = "LOW_APL")
    protected String lowapl;
    @XmlElement(name = "CST2")
    protected String cst2;
    @XmlElement(name = "CST4")
    protected String cst4;
    @XmlElement(name = "CST6")
    protected String cst6;
    @XmlElement(name = "CST8")
    protected String cst8;
    @XmlElement(name = "COLLECT")
    protected String collect;

    /**
     * Gets the value of the upapl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUPAPL() {
        return upapl;
    }

    /**
     * Sets the value of the upapl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUPAPL(String value) {
        this.upapl = value;
    }

    /**
     * Gets the value of the bvcontrol property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBVCONTROL() {
        return bvcontrol;
    }

    /**
     * Sets the value of the bvcontrol property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBVCONTROL(String value) {
        this.bvcontrol = value;
    }

    /**
     * Gets the value of the bvdl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBVDL() {
        return bvdl;
    }

    /**
     * Sets the value of the bvdl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBVDL(String value) {
        this.bvdl = value;
    }

    /**
     * Gets the value of the bvset property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBVSET() {
        return bvset;
    }

    /**
     * Sets the value of the bvset property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBVSET(String value) {
        this.bvset = value;
    }

    /**
     * Gets the value of the bvformat property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBVFORMAT() {
        return bvformat;
    }

    /**
     * Sets the value of the bvformat property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBVFORMAT(String value) {
        this.bvformat = value;
    }

    /**
     * Gets the value of the bvpld property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBVPLD() {
        return bvpld;
    }

    /**
     * Sets the value of the bvpld property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBVPLD(String value) {
        this.bvpld = value;
    }

    /**
     * Gets the value of the lowapl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLOWAPL() {
        return lowapl;
    }

    /**
     * Sets the value of the lowapl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLOWAPL(String value) {
        this.lowapl = value;
    }

    /**
     * Gets the value of the cst2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCST2() {
        return cst2;
    }

    /**
     * Sets the value of the cst2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCST2(String value) {
        this.cst2 = value;
    }

    /**
     * Gets the value of the cst4 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCST4() {
        return cst4;
    }

    /**
     * Sets the value of the cst4 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCST4(String value) {
        this.cst4 = value;
    }

    /**
     * Gets the value of the cst6 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCST6() {
        return cst6;
    }

    /**
     * Sets the value of the cst6 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCST6(String value) {
        this.cst6 = value;
    }

    /**
     * Gets the value of the cst8 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCST8() {
        return cst8;
    }

    /**
     * Sets the value of the cst8 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCST8(String value) {
        this.cst8 = value;
    }

    /**
     * Gets the value of the collect property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCOLLECT() {
        return collect;
    }

    /**
     * Sets the value of the collect property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCOLLECT(String value) {
        this.collect = value;
    }

}
