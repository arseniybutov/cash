
package jp.co.glory.bruebox;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RCW8XRomVerType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RCW8XRomVerType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}AP"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}BV"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}Boot"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}IPL"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}FPGA"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RCW8XRomVerType", propOrder = {
    "ap",
    "bv",
    "boot",
    "ipl",
    "fpga"
})
public class RCW8XRomVerType {

    @XmlElement(name = "AP", required = true)
    protected String ap;
    @XmlElement(name = "BV", required = true)
    protected String bv;
    @XmlElement(name = "Boot", required = true)
    protected String boot;
    @XmlElement(name = "IPL", required = true)
    protected String ipl;
    @XmlElement(name = "FPGA", required = true)
    protected String fpga;

    /**
     * Gets the value of the ap property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAP() {
        return ap;
    }

    /**
     * Sets the value of the ap property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAP(String value) {
        this.ap = value;
    }

    /**
     * Gets the value of the bv property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBV() {
        return bv;
    }

    /**
     * Sets the value of the bv property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBV(String value) {
        this.bv = value;
    }

    /**
     * Gets the value of the boot property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBoot() {
        return boot;
    }

    /**
     * Sets the value of the boot property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBoot(String value) {
        this.boot = value;
    }

    /**
     * Gets the value of the ipl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIPL() {
        return ipl;
    }

    /**
     * Sets the value of the ipl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIPL(String value) {
        this.ipl = value;
    }

    /**
     * Gets the value of the fpga property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFPGA() {
        return fpga;
    }

    /**
     * Sets the value of the fpga property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFPGA(String value) {
        this.fpga = value;
    }

}
