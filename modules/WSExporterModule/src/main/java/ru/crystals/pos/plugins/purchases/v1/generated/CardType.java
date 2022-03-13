
package ru.crystals.pos.plugins.purchases.v1.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 карта (примененная при покупке)
 *             
 * 
 * <p>Java class for cardType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="cardType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="type" type="{}card-typeType" />
 *       &lt;attribute name="card_type_guid" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="number" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cardType")
public class CardType {

    @XmlAttribute(name = "type")
    protected CardTypeType type;
    @XmlAttribute(name = "card_type_guid")
    protected Long cardTypeGuid;
    @XmlAttribute(name = "number", required = true)
    protected String number;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link CardTypeType }
     *     
     */
    public CardTypeType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link CardTypeType }
     *     
     */
    public void setType(CardTypeType value) {
        this.type = value;
    }

    /**
     * Gets the value of the cardTypeGuid property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getCardTypeGuid() {
        return cardTypeGuid;
    }

    /**
     * Sets the value of the cardTypeGuid property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCardTypeGuid(Long value) {
        this.cardTypeGuid = value;
    }

    /**
     * Gets the value of the number property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNumber() {
        return number;
    }

    /**
     * Sets the value of the number property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNumber(String value) {
        this.number = value;
    }

}
