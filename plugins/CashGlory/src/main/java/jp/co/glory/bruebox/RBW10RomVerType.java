
package jp.co.glory.bruebox;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RBW10RomVerType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RBW10RomVerType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}AP"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}BVControl"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}BVDownload"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}BVSetting"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}BVFormat"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}BVPLD2"/>
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
@XmlType(name = "RBW10RomVerType", propOrder = {
    "ap",
    "bvControl",
    "bvDownload",
    "bvSetting",
    "bvFormat",
    "bvpld2",
    "boot",
    "ipl",
    "fpga"
})
public class RBW10RomVerType {

    @XmlElement(name = "AP", required = true)
    protected String ap;
    @XmlElement(name = "BVControl", required = true)
    protected String bvControl;
    @XmlElement(name = "BVDownload", required = true)
    protected String bvDownload;
    @XmlElement(name = "BVSetting", required = true)
    protected String bvSetting;
    @XmlElement(name = "BVFormat", required = true)
    protected String bvFormat;
    @XmlElement(name = "BVPLD2", required = true)
    protected String bvpld2;
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
     * Gets the value of the bvControl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBVControl() {
        return bvControl;
    }

    /**
     * Sets the value of the bvControl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBVControl(String value) {
        this.bvControl = value;
    }

    /**
     * Gets the value of the bvDownload property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBVDownload() {
        return bvDownload;
    }

    /**
     * Sets the value of the bvDownload property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBVDownload(String value) {
        this.bvDownload = value;
    }

    /**
     * Gets the value of the bvSetting property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBVSetting() {
        return bvSetting;
    }

    /**
     * Sets the value of the bvSetting property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBVSetting(String value) {
        this.bvSetting = value;
    }

    /**
     * Gets the value of the bvFormat property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBVFormat() {
        return bvFormat;
    }

    /**
     * Sets the value of the bvFormat property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBVFormat(String value) {
        this.bvFormat = value;
    }

    /**
     * Gets the value of the bvpld2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBVPLD2() {
        return bvpld2;
    }

    /**
     * Sets the value of the bvpld2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBVPLD2(String value) {
        this.bvpld2 = value;
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
