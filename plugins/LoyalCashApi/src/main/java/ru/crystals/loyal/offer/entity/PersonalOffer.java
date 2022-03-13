
package ru.crystals.loyal.offer.entity;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;


public class PersonalOffer {
    private String text;
    private String id;
    private String description;
    private BigDecimal value;
    private BigDecimal quantityGoods;
    private BigDecimal priceOfQuantity;
    private BigDecimal minquantity;
    private Date effectDate;
    private Date expiryDate;
    private String subjectID;
    private String subjectName;
    private String subjectUrl;
    private BigInteger print;
    private Long internalID;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getQuantityGoods() {
        return quantityGoods;
    }

    public void setQuantityGoods(BigDecimal quantityGoods) {
        this.quantityGoods = quantityGoods;
    }

    public BigDecimal getPriceOfQuantity() {
        return priceOfQuantity;
    }

    public void setPriceOfQuantity(BigDecimal priceOfQuantity) {
        this.priceOfQuantity = priceOfQuantity;
    }

    public BigDecimal getMinquantity() {
        return minquantity;
    }

    public void setMinquantity(BigDecimal minquantity) {
        this.minquantity = minquantity;
    }

    public Date getEffectDate() {
        return effectDate;
    }

    public void setEffectDate(Date effectDate) {
        this.effectDate = effectDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getSubjectID() {
        return subjectID;
    }

    public void setSubjectID(String subjectID) {
        this.subjectID = subjectID;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectUrl() {
        return subjectUrl;
    }

    public void setSubjectUrl(String subjectUrl) {
        this.subjectUrl = subjectUrl;
    }

    public BigInteger getPrint() {
        return print;
    }

    public void setPrint(BigInteger print) {
        this.print = print;
    }

    public Long getInternalID() {
        return internalID;
    }

    public void setInternalID(Long internalID) {
        this.internalID = internalID;
    }

    @Override
    public String toString() {
        return "PersonalOffer{" +
                "text='" + text + '\'' +
                ", id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", value=" + value +
                ", quantityGoods=" + quantityGoods +
                ", priceOfQuantity=" + priceOfQuantity +
                ", minquantity=" + minquantity +
                ", effectDate=" + effectDate +
                ", expiryDate=" + expiryDate +
                ", subjectID='" + subjectID + '\'' +
                ", subjectName='" + subjectName + '\'' +
                ", subjectUrl='" + subjectUrl + '\'' +
                ", print=" + print +
                ", internalID=" + internalID +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PersonalOffer that = (PersonalOffer) o;

        if (text != null ? !text.equals(that.text) : that.text != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        if (quantityGoods != null ? !quantityGoods.equals(that.quantityGoods) : that.quantityGoods != null)
            return false;
        if (priceOfQuantity != null ? !priceOfQuantity.equals(that.priceOfQuantity) : that.priceOfQuantity != null)
            return false;
        if (minquantity != null ? !minquantity.equals(that.minquantity) : that.minquantity != null) return false;
        if (effectDate != null ? !effectDate.equals(that.effectDate) : that.effectDate != null) return false;
        if (expiryDate != null ? !expiryDate.equals(that.expiryDate) : that.expiryDate != null) return false;
        if (subjectID != null ? !subjectID.equals(that.subjectID) : that.subjectID != null) return false;
        if (subjectName != null ? !subjectName.equals(that.subjectName) : that.subjectName != null) return false;
        if (subjectUrl != null ? !subjectUrl.equals(that.subjectUrl) : that.subjectUrl != null) return false;
        if (print != null ? !print.equals(that.print) : that.print != null) return false;
        return internalID != null ? internalID.equals(that.internalID) : that.internalID == null;

    }

    @Override
    public int hashCode() {
        int result = text != null ? text.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (quantityGoods != null ? quantityGoods.hashCode() : 0);
        result = 31 * result + (priceOfQuantity != null ? priceOfQuantity.hashCode() : 0);
        result = 31 * result + (minquantity != null ? minquantity.hashCode() : 0);
        result = 31 * result + (effectDate != null ? effectDate.hashCode() : 0);
        result = 31 * result + (expiryDate != null ? expiryDate.hashCode() : 0);
        result = 31 * result + (subjectID != null ? subjectID.hashCode() : 0);
        result = 31 * result + (subjectName != null ? subjectName.hashCode() : 0);
        result = 31 * result + (subjectUrl != null ? subjectUrl.hashCode() : 0);
        result = 31 * result + (print != null ? print.hashCode() : 0);
        result = 31 * result + (internalID != null ? internalID.hashCode() : 0);
        return result;
    }
}
