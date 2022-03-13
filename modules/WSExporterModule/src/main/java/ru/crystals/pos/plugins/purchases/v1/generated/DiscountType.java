
package ru.crystals.pos.plugins.purchases.v1.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for discountType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="discountType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="positionId" use="required" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="goodCode" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="amount" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="isDiscountPurchase" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="AdvertActGUID" use="required" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="AdvertActExternalCode" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="advertType" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "discountType")
public class DiscountType {

    @XmlAttribute(name = "positionId", required = true)
    protected long positionId;
    @XmlAttribute(name = "goodCode")
    protected String goodCode;
    @XmlAttribute(name = "amount", required = true)
    protected double amount;
    @XmlAttribute(name = "isDiscountPurchase", required = true)
    protected boolean isDiscountPurchase;
    @XmlAttribute(name = "AdvertActGUID", required = true)
    protected long advertActGUID;
    @XmlAttribute(name = "AdvertActExternalCode")
    protected String advertActExternalCode;
    @XmlAttribute(name = "advertType", required = true)
    protected String advertType;

    /**
     * Gets the value of the positionId property.
     * 
     */
    public long getPositionId() {
        return positionId;
    }

    /**
     * Sets the value of the positionId property.
     * 
     */
    public void setPositionId(long value) {
        this.positionId = value;
    }

    /**
     * Gets the value of the goodCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGoodCode() {
        return goodCode;
    }

    /**
     * Sets the value of the goodCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGoodCode(String value) {
        this.goodCode = value;
    }

    /**
     * Gets the value of the amount property.
     * 
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Sets the value of the amount property.
     * 
     */
    public void setAmount(double value) {
        this.amount = value;
    }

    /**
     * Gets the value of the isDiscountPurchase property.
     * 
     */
    public boolean isIsDiscountPurchase() {
        return isDiscountPurchase;
    }

    /**
     * Sets the value of the isDiscountPurchase property.
     * 
     */
    public void setIsDiscountPurchase(boolean value) {
        this.isDiscountPurchase = value;
    }

    /**
     * Gets the value of the advertActGUID property.
     * 
     */
    public long getAdvertActGUID() {
        return advertActGUID;
    }

    /**
     * Sets the value of the advertActGUID property.
     * 
     */
    public void setAdvertActGUID(long value) {
        this.advertActGUID = value;
    }

    /**
     * Gets the value of the advertActExternalCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAdvertActExternalCode() {
        return advertActExternalCode;
    }

    /**
     * Sets the value of the advertActExternalCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAdvertActExternalCode(String value) {
        this.advertActExternalCode = value;
    }

    /**
     * Gets the value of the advertType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAdvertType() {
        return advertType;
    }

    /**
     * Sets the value of the advertType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAdvertType(String value) {
        this.advertType = value;
    }

}
