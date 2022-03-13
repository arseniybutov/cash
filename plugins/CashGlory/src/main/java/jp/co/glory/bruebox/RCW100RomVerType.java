
package jp.co.glory.bruebox;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RCW100RomVerType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RCW100RomVerType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}CV_AP" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}CV_COUNTRY" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}MAIN_AP" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}SPEC_INFO" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RCW100RomVerType", propOrder = {
    "cvap",
    "cvcountry",
    "mainap",
    "specinfo"
})
public class RCW100RomVerType {

    @XmlElement(name = "CV_AP")
    protected String cvap;
    @XmlElement(name = "CV_COUNTRY")
    protected String cvcountry;
    @XmlElement(name = "MAIN_AP")
    protected String mainap;
    @XmlElement(name = "SPEC_INFO")
    protected String specinfo;

    /**
     * Gets the value of the cvap property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCVAP() {
        return cvap;
    }

    /**
     * Sets the value of the cvap property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCVAP(String value) {
        this.cvap = value;
    }

    /**
     * Gets the value of the cvcountry property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCVCOUNTRY() {
        return cvcountry;
    }

    /**
     * Sets the value of the cvcountry property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCVCOUNTRY(String value) {
        this.cvcountry = value;
    }

    /**
     * Gets the value of the mainap property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMAINAP() {
        return mainap;
    }

    /**
     * Sets the value of the mainap property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMAINAP(String value) {
        this.mainap = value;
    }

    /**
     * Gets the value of the specinfo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSPECINFO() {
        return specinfo;
    }

    /**
     * Sets the value of the specinfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSPECINFO(String value) {
        this.specinfo = value;
    }

}
