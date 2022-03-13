
package ru.crystals.pos.plugins.purchases.v1.generated;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 Описание позиции в чеке
 *             
 * 
 * <p>Java class for positionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="positionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="plugin-property" type="{}plugin-propertyType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="order" use="required" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="departNumber" use="required" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="goodsCode" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="barCode" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="count" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="cost" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="nds" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="ndsClass" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="ndsSum" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="discountValue" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="costWithDiscount" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="amount" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="softCheckNumber" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "positionType", propOrder = {
    "pluginProperty"
})
public class PositionType {

    @XmlElement(name = "plugin-property")
    protected List<PluginPropertyType> pluginProperty;
    @XmlAttribute(name = "order", required = true)
    protected long order;
    @XmlAttribute(name = "departNumber", required = true)
    protected long departNumber;
    @XmlAttribute(name = "goodsCode", required = true)
    protected String goodsCode;
    @XmlAttribute(name = "barCode", required = true)
    protected String barCode;
    @XmlAttribute(name = "count", required = true)
    protected double count;
    @XmlAttribute(name = "cost", required = true)
    protected double cost;
    @XmlAttribute(name = "nds", required = true)
    protected double nds;
    @XmlAttribute(name = "ndsClass", required = true)
    protected String ndsClass;
    @XmlAttribute(name = "ndsSum", required = true)
    protected double ndsSum;
    @XmlAttribute(name = "discountValue", required = true)
    protected double discountValue;
    @XmlAttribute(name = "costWithDiscount", required = true)
    protected double costWithDiscount;
    @XmlAttribute(name = "amount", required = true)
    protected double amount;
    @XmlAttribute(name = "softCheckNumber")
    protected String softCheckNumber;

    /**
     * Gets the value of the pluginProperty property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pluginProperty property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPluginProperty().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PluginPropertyType }
     * 
     * 
     */
    public List<PluginPropertyType> getPluginProperty() {
        if (pluginProperty == null) {
            pluginProperty = new ArrayList<PluginPropertyType>();
        }
        return this.pluginProperty;
    }

    /**
     * Gets the value of the order property.
     * 
     */
    public long getOrder() {
        return order;
    }

    /**
     * Sets the value of the order property.
     * 
     */
    public void setOrder(long value) {
        this.order = value;
    }

    /**
     * Gets the value of the departNumber property.
     * 
     */
    public long getDepartNumber() {
        return departNumber;
    }

    /**
     * Sets the value of the departNumber property.
     * 
     */
    public void setDepartNumber(long value) {
        this.departNumber = value;
    }

    /**
     * Gets the value of the goodsCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGoodsCode() {
        return goodsCode;
    }

    /**
     * Sets the value of the goodsCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGoodsCode(String value) {
        this.goodsCode = value;
    }

    /**
     * Gets the value of the barCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBarCode() {
        return barCode;
    }

    /**
     * Sets the value of the barCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBarCode(String value) {
        this.barCode = value;
    }

    /**
     * Gets the value of the count property.
     * 
     */
    public double getCount() {
        return count;
    }

    /**
     * Sets the value of the count property.
     * 
     */
    public void setCount(double value) {
        this.count = value;
    }

    /**
     * Gets the value of the cost property.
     * 
     */
    public double getCost() {
        return cost;
    }

    /**
     * Sets the value of the cost property.
     * 
     */
    public void setCost(double value) {
        this.cost = value;
    }

    /**
     * Gets the value of the nds property.
     * 
     */
    public double getNds() {
        return nds;
    }

    /**
     * Sets the value of the nds property.
     * 
     */
    public void setNds(double value) {
        this.nds = value;
    }

    /**
     * Gets the value of the ndsClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNdsClass() {
        return ndsClass;
    }

    /**
     * Sets the value of the ndsClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNdsClass(String value) {
        this.ndsClass = value;
    }

    /**
     * Gets the value of the ndsSum property.
     * 
     */
    public double getNdsSum() {
        return ndsSum;
    }

    /**
     * Sets the value of the ndsSum property.
     * 
     */
    public void setNdsSum(double value) {
        this.ndsSum = value;
    }

    /**
     * Gets the value of the discountValue property.
     * 
     */
    public double getDiscountValue() {
        return discountValue;
    }

    /**
     * Sets the value of the discountValue property.
     * 
     */
    public void setDiscountValue(double value) {
        this.discountValue = value;
    }

    /**
     * Gets the value of the costWithDiscount property.
     * 
     */
    public double getCostWithDiscount() {
        return costWithDiscount;
    }

    /**
     * Sets the value of the costWithDiscount property.
     * 
     */
    public void setCostWithDiscount(double value) {
        this.costWithDiscount = value;
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
     * Gets the value of the softCheckNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSoftCheckNumber() {
        return softCheckNumber;
    }

    /**
     * Sets the value of the softCheckNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSoftCheckNumber(String value) {
        this.softCheckNumber = value;
    }

}
