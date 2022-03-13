
package ru.crystals.pos.plugins.purchases.v1.generated;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * 
 *                 Чек
 *             
 * 
 * <p>Java class for purchaseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="purchaseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="positions" type="{}positionsType" minOccurs="0" form="qualified"/>
 *         &lt;element name="payments" type="{}paymentsType" minOccurs="0" form="qualified"/>
 *         &lt;element name="discounts" type="{}discountsType" minOccurs="0" form="qualified"/>
 *         &lt;element name="discountCards" type="{}discountCardsType" minOccurs="0" form="qualified"/>
 *         &lt;element name="card" type="{}cardType" maxOccurs="unbounded" minOccurs="0" form="qualified"/>
 *         &lt;element name="original-purchase" type="{}purchaseType" minOccurs="0" form="qualified"/>
 *         &lt;element name="bonus-discount" type="{}bonusDiscountType" minOccurs="0" form="qualified"/>
 *       &lt;/sequence>
 *       &lt;attribute name="tabNumber" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="userName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="operationType" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="operDay" use="required" type="{http://www.w3.org/2001/XMLSchema}date" />
 *       &lt;attribute name="shop" use="required" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="cash" use="required" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="shift" use="required" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="number" use="required" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="saletime" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="amount" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="discountAmount" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="denyPrintToDocuments" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="clientType" type="{}client_Type" />
 *       &lt;attribute name="client_guid" type="{http://www.w3.org/2001/XMLSchema}long" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "purchaseType", propOrder = {
    "positions",
    "payments",
    "discounts",
    "discountCards",
    "card",
    "originalPurchase",
    "bonusDiscount"
})
public class PurchaseType {

    protected PositionsType positions;
    protected PaymentsType payments;
    protected DiscountsType discounts;
    protected DiscountCardsType discountCards;
    protected List<CardType> card;
    @XmlElement(name = "original-purchase")
    protected PurchaseType originalPurchase;
    @XmlElement(name = "bonus-discount")
    protected BonusDiscountType bonusDiscount;
    @XmlAttribute(name = "tabNumber")
    protected String tabNumber;
    @XmlAttribute(name = "userName")
    protected String userName;
    @XmlAttribute(name = "operationType")
    protected Boolean operationType;
    @XmlAttribute(name = "operDay", required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar operDay;
    @XmlAttribute(name = "shop", required = true)
    protected long shop;
    @XmlAttribute(name = "cash", required = true)
    protected long cash;
    @XmlAttribute(name = "shift", required = true)
    protected long shift;
    @XmlAttribute(name = "number", required = true)
    protected long number;
    @XmlAttribute(name = "saletime")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar saletime;
    @XmlAttribute(name = "amount")
    protected Double amount;
    @XmlAttribute(name = "discountAmount")
    protected Double discountAmount;
    @XmlAttribute(name = "denyPrintToDocuments")
    protected Boolean denyPrintToDocuments;
    @XmlAttribute(name = "clientType")
    protected ClientType clientType;
    @XmlAttribute(name = "client_guid")
    protected Long clientGuid;

    /**
     * Gets the value of the positions property.
     * 
     * @return
     *     possible object is
     *     {@link PositionsType }
     *     
     */
    public PositionsType getPositions() {
        return positions;
    }

    /**
     * Sets the value of the positions property.
     * 
     * @param value
     *     allowed object is
     *     {@link PositionsType }
     *     
     */
    public void setPositions(PositionsType value) {
        this.positions = value;
    }

    /**
     * Gets the value of the payments property.
     * 
     * @return
     *     possible object is
     *     {@link PaymentsType }
     *     
     */
    public PaymentsType getPayments() {
        return payments;
    }

    /**
     * Sets the value of the payments property.
     * 
     * @param value
     *     allowed object is
     *     {@link PaymentsType }
     *     
     */
    public void setPayments(PaymentsType value) {
        this.payments = value;
    }

    /**
     * Gets the value of the discounts property.
     * 
     * @return
     *     possible object is
     *     {@link DiscountsType }
     *     
     */
    public DiscountsType getDiscounts() {
        return discounts;
    }

    /**
     * Sets the value of the discounts property.
     * 
     * @param value
     *     allowed object is
     *     {@link DiscountsType }
     *     
     */
    public void setDiscounts(DiscountsType value) {
        this.discounts = value;
    }

    /**
     * Gets the value of the discountCards property.
     * 
     * @return
     *     possible object is
     *     {@link DiscountCardsType }
     *     
     */
    public DiscountCardsType getDiscountCards() {
        return discountCards;
    }

    /**
     * Sets the value of the discountCards property.
     * 
     * @param value
     *     allowed object is
     *     {@link DiscountCardsType }
     *     
     */
    public void setDiscountCards(DiscountCardsType value) {
        this.discountCards = value;
    }

    /**
     * Gets the value of the card property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the card property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCard().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CardType }
     * 
     * 
     */
    public List<CardType> getCard() {
        if (card == null) {
            card = new ArrayList<CardType>();
        }
        return this.card;
    }

    /**
     * Gets the value of the originalPurchase property.
     * 
     * @return
     *     possible object is
     *     {@link PurchaseType }
     *     
     */
    public PurchaseType getOriginalPurchase() {
        return originalPurchase;
    }

    /**
     * Sets the value of the originalPurchase property.
     * 
     * @param value
     *     allowed object is
     *     {@link PurchaseType }
     *     
     */
    public void setOriginalPurchase(PurchaseType value) {
        this.originalPurchase = value;
    }

    /**
     * Gets the value of the bonusDiscount property.
     * 
     * @return
     *     possible object is
     *     {@link BonusDiscountType }
     *     
     */
    public BonusDiscountType getBonusDiscount() {
        return bonusDiscount;
    }

    /**
     * Sets the value of the bonusDiscount property.
     * 
     * @param value
     *     allowed object is
     *     {@link BonusDiscountType }
     *     
     */
    public void setBonusDiscount(BonusDiscountType value) {
        this.bonusDiscount = value;
    }

    /**
     * Gets the value of the tabNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTabNumber() {
        return tabNumber;
    }

    /**
     * Sets the value of the tabNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTabNumber(String value) {
        this.tabNumber = value;
    }

    /**
     * Gets the value of the userName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the value of the userName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserName(String value) {
        this.userName = value;
    }

    /**
     * Gets the value of the operationType property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isOperationType() {
        if (operationType == null) {
            return true;
        } else {
            return operationType;
        }
    }

    /**
     * Sets the value of the operationType property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOperationType(Boolean value) {
        this.operationType = value;
    }

    /**
     * Gets the value of the operDay property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getOperDay() {
        return operDay;
    }

    /**
     * Sets the value of the operDay property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setOperDay(XMLGregorianCalendar value) {
        this.operDay = value;
    }

    /**
     * Gets the value of the shop property.
     * 
     */
    public long getShop() {
        return shop;
    }

    /**
     * Sets the value of the shop property.
     * 
     */
    public void setShop(long value) {
        this.shop = value;
    }

    /**
     * Gets the value of the cash property.
     * 
     */
    public long getCash() {
        return cash;
    }

    /**
     * Sets the value of the cash property.
     * 
     */
    public void setCash(long value) {
        this.cash = value;
    }

    /**
     * Gets the value of the shift property.
     * 
     */
    public long getShift() {
        return shift;
    }

    /**
     * Sets the value of the shift property.
     * 
     */
    public void setShift(long value) {
        this.shift = value;
    }

    /**
     * Gets the value of the number property.
     * 
     */
    public long getNumber() {
        return number;
    }

    /**
     * Sets the value of the number property.
     * 
     */
    public void setNumber(long value) {
        this.number = value;
    }

    /**
     * Gets the value of the saletime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getSaletime() {
        return saletime;
    }

    /**
     * Sets the value of the saletime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setSaletime(XMLGregorianCalendar value) {
        this.saletime = value;
    }

    /**
     * Gets the value of the amount property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getAmount() {
        return amount;
    }

    /**
     * Sets the value of the amount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setAmount(Double value) {
        this.amount = value;
    }

    /**
     * Gets the value of the discountAmount property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getDiscountAmount() {
        return discountAmount;
    }

    /**
     * Sets the value of the discountAmount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setDiscountAmount(Double value) {
        this.discountAmount = value;
    }

    /**
     * Gets the value of the denyPrintToDocuments property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDenyPrintToDocuments() {
        return denyPrintToDocuments;
    }

    /**
     * Sets the value of the denyPrintToDocuments property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDenyPrintToDocuments(Boolean value) {
        this.denyPrintToDocuments = value;
    }

    /**
     * Gets the value of the clientType property.
     * 
     * @return
     *     possible object is
     *     {@link ClientType }
     *     
     */
    public ClientType getClientType() {
        return clientType;
    }

    /**
     * Sets the value of the clientType property.
     * 
     * @param value
     *     allowed object is
     *     {@link ClientType }
     *     
     */
    public void setClientType(ClientType value) {
        this.clientType = value;
    }

    /**
     * Gets the value of the clientGuid property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getClientGuid() {
        return clientGuid;
    }

    /**
     * Sets the value of the clientGuid property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setClientGuid(Long value) {
        this.clientGuid = value;
    }

}
