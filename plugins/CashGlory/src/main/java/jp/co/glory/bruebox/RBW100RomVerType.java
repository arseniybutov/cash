
package jp.co.glory.bruebox;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RBW100RomVerType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RBW100RomVerType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}BV_GENERAL1" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}BV_GENERAL2" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}IPL" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}BV_DL" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}BV_AP" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}BV_PARAM1" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}BV_PARAM2" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}BV_FORMAT" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}PLD1" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}FPGA1" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}PLD2" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}FPGA2" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}UN_GENERAL" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}UN_FUNC" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}UN_SERIAL" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}UN_CC" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}UN_CMB" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}MAIN_AP" minOccurs="0"/>
 *         &lt;element ref="{http://www.glory.co.jp/bruebox.xsd}COLLECT_SERIAL" minOccurs="0"/>
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
@XmlType(name = "RBW100RomVerType", propOrder = {
    "bvgeneral1",
    "bvgeneral2",
    "ipl",
    "bvdl",
    "bvap",
    "bvparam1",
    "bvparam2",
    "bvformat",
    "pld1",
    "fpga1",
    "pld2",
    "fpga2",
    "ungeneral",
    "unfunc",
    "unserial",
    "uncc",
    "uncmb",
    "mainap",
    "collectserial",
    "specinfo"
})
public class RBW100RomVerType {

    @XmlElement(name = "BV_GENERAL1")
    protected String bvgeneral1;
    @XmlElement(name = "BV_GENERAL2")
    protected String bvgeneral2;
    @XmlElement(name = "IPL")
    protected String ipl;
    @XmlElement(name = "BV_DL")
    protected String bvdl;
    @XmlElement(name = "BV_AP")
    protected String bvap;
    @XmlElement(name = "BV_PARAM1")
    protected String bvparam1;
    @XmlElement(name = "BV_PARAM2")
    protected String bvparam2;
    @XmlElement(name = "BV_FORMAT")
    protected String bvformat;
    @XmlElement(name = "PLD1")
    protected String pld1;
    @XmlElement(name = "FPGA1")
    protected String fpga1;
    @XmlElement(name = "PLD2")
    protected String pld2;
    @XmlElement(name = "FPGA2")
    protected String fpga2;
    @XmlElement(name = "UN_GENERAL")
    protected String ungeneral;
    @XmlElement(name = "UN_FUNC")
    protected String unfunc;
    @XmlElement(name = "UN_SERIAL")
    protected String unserial;
    @XmlElement(name = "UN_CC")
    protected String uncc;
    @XmlElement(name = "UN_CMB")
    protected String uncmb;
    @XmlElement(name = "MAIN_AP")
    protected String mainap;
    @XmlElement(name = "COLLECT_SERIAL")
    protected String collectserial;
    @XmlElement(name = "SPEC_INFO")
    protected String specinfo;

    /**
     * Gets the value of the bvgeneral1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBVGENERAL1() {
        return bvgeneral1;
    }

    /**
     * Sets the value of the bvgeneral1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBVGENERAL1(String value) {
        this.bvgeneral1 = value;
    }

    /**
     * Gets the value of the bvgeneral2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBVGENERAL2() {
        return bvgeneral2;
    }

    /**
     * Sets the value of the bvgeneral2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBVGENERAL2(String value) {
        this.bvgeneral2 = value;
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
     * Gets the value of the bvap property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBVAP() {
        return bvap;
    }

    /**
     * Sets the value of the bvap property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBVAP(String value) {
        this.bvap = value;
    }

    /**
     * Gets the value of the bvparam1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBVPARAM1() {
        return bvparam1;
    }

    /**
     * Sets the value of the bvparam1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBVPARAM1(String value) {
        this.bvparam1 = value;
    }

    /**
     * Gets the value of the bvparam2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBVPARAM2() {
        return bvparam2;
    }

    /**
     * Sets the value of the bvparam2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBVPARAM2(String value) {
        this.bvparam2 = value;
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
     * Gets the value of the pld1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPLD1() {
        return pld1;
    }

    /**
     * Sets the value of the pld1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPLD1(String value) {
        this.pld1 = value;
    }

    /**
     * Gets the value of the fpga1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFPGA1() {
        return fpga1;
    }

    /**
     * Sets the value of the fpga1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFPGA1(String value) {
        this.fpga1 = value;
    }

    /**
     * Gets the value of the pld2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPLD2() {
        return pld2;
    }

    /**
     * Sets the value of the pld2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPLD2(String value) {
        this.pld2 = value;
    }

    /**
     * Gets the value of the fpga2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFPGA2() {
        return fpga2;
    }

    /**
     * Sets the value of the fpga2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFPGA2(String value) {
        this.fpga2 = value;
    }

    /**
     * Gets the value of the ungeneral property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUNGENERAL() {
        return ungeneral;
    }

    /**
     * Sets the value of the ungeneral property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUNGENERAL(String value) {
        this.ungeneral = value;
    }

    /**
     * Gets the value of the unfunc property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUNFUNC() {
        return unfunc;
    }

    /**
     * Sets the value of the unfunc property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUNFUNC(String value) {
        this.unfunc = value;
    }

    /**
     * Gets the value of the unserial property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUNSERIAL() {
        return unserial;
    }

    /**
     * Sets the value of the unserial property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUNSERIAL(String value) {
        this.unserial = value;
    }

    /**
     * Gets the value of the uncc property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUNCC() {
        return uncc;
    }

    /**
     * Sets the value of the uncc property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUNCC(String value) {
        this.uncc = value;
    }

    /**
     * Gets the value of the uncmb property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUNCMB() {
        return uncmb;
    }

    /**
     * Sets the value of the uncmb property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUNCMB(String value) {
        this.uncmb = value;
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
     * Gets the value of the collectserial property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCOLLECTSERIAL() {
        return collectserial;
    }

    /**
     * Sets the value of the collectserial property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCOLLECTSERIAL(String value) {
        this.collectserial = value;
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
