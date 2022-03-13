
package jp.co.glory.bruebox;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RequireVerifyInfosType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RequireVerifyInfosType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RequireVerifyDenominationInfos" type="{http://www.glory.co.jp/bruebox.xsd}RequireVerifyDenominationInfosType" minOccurs="0"/>
 *         &lt;element name="RequireVerifyCollectionContainerInfos" type="{http://www.glory.co.jp/bruebox.xsd}RequireVerifyCollectionContainerInfosType" minOccurs="0"/>
 *         &lt;element name="RequireVerifyMixStackerInfos" type="{http://www.glory.co.jp/bruebox.xsd}RequireVerifyMixStackerInfosType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RequireVerifyInfosType", propOrder = {
    "requireVerifyDenominationInfos",
    "requireVerifyCollectionContainerInfos",
    "requireVerifyMixStackerInfos"
})
public class RequireVerifyInfosType {

    @XmlElement(name = "RequireVerifyDenominationInfos", namespace = "")
    protected RequireVerifyDenominationInfosType requireVerifyDenominationInfos;
    @XmlElement(name = "RequireVerifyCollectionContainerInfos", namespace = "")
    protected RequireVerifyCollectionContainerInfosType requireVerifyCollectionContainerInfos;
    @XmlElement(name = "RequireVerifyMixStackerInfos", namespace = "")
    protected RequireVerifyMixStackerInfosType requireVerifyMixStackerInfos;

    /**
     * Gets the value of the requireVerifyDenominationInfos property.
     * 
     * @return
     *     possible object is
     *     {@link RequireVerifyDenominationInfosType }
     *     
     */
    public RequireVerifyDenominationInfosType getRequireVerifyDenominationInfos() {
        return requireVerifyDenominationInfos;
    }

    /**
     * Sets the value of the requireVerifyDenominationInfos property.
     * 
     * @param value
     *     allowed object is
     *     {@link RequireVerifyDenominationInfosType }
     *     
     */
    public void setRequireVerifyDenominationInfos(RequireVerifyDenominationInfosType value) {
        this.requireVerifyDenominationInfos = value;
    }

    /**
     * Gets the value of the requireVerifyCollectionContainerInfos property.
     * 
     * @return
     *     possible object is
     *     {@link RequireVerifyCollectionContainerInfosType }
     *     
     */
    public RequireVerifyCollectionContainerInfosType getRequireVerifyCollectionContainerInfos() {
        return requireVerifyCollectionContainerInfos;
    }

    /**
     * Sets the value of the requireVerifyCollectionContainerInfos property.
     * 
     * @param value
     *     allowed object is
     *     {@link RequireVerifyCollectionContainerInfosType }
     *     
     */
    public void setRequireVerifyCollectionContainerInfos(RequireVerifyCollectionContainerInfosType value) {
        this.requireVerifyCollectionContainerInfos = value;
    }

    /**
     * Gets the value of the requireVerifyMixStackerInfos property.
     * 
     * @return
     *     possible object is
     *     {@link RequireVerifyMixStackerInfosType }
     *     
     */
    public RequireVerifyMixStackerInfosType getRequireVerifyMixStackerInfos() {
        return requireVerifyMixStackerInfos;
    }

    /**
     * Sets the value of the requireVerifyMixStackerInfos property.
     * 
     * @param value
     *     allowed object is
     *     {@link RequireVerifyMixStackerInfosType }
     *     
     */
    public void setRequireVerifyMixStackerInfos(RequireVerifyMixStackerInfosType value) {
        this.requireVerifyMixStackerInfos = value;
    }

}
