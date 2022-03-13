
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
 *                 Класс описывающий сущность вида оплаты (урезанная версия для отправки в ERP)
 *             
 * 
 * <p>Java class for paymentType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="paymentType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="plugin-property" type="{}plugin-propertyType" maxOccurs="unbounded" minOccurs="0" form="qualified"/>
 *       &lt;/sequence>
 *       &lt;attribute name="typeClass" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="amount" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="description" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "paymentType", propOrder = {
    "pluginProperty"
})
public class PaymentType {

    @XmlElement(name = "plugin-property")
    protected List<PluginPropertyType> pluginProperty;
    @XmlAttribute(name = "typeClass", required = true)
    protected String typeClass;
    @XmlAttribute(name = "amount", required = true)
    protected double amount;
    @XmlAttribute(name = "description", required = true)
    protected String description;

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
     * Gets the value of the typeClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTypeClass() {
        return typeClass;
    }

    /**
     * Sets the value of the typeClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTypeClass(String value) {
        this.typeClass = value;
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
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

}
