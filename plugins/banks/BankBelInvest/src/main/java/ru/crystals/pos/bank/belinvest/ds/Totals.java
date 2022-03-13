package ru.crystals.pos.bank.belinvest.ds;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Created by Tatarinov Eduard on 17.11.16.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Totals {
    @XmlElement(name = "SaleAmt")
    private Long saleAmt;
    @XmlElement(name = "SaleCount")
    private Long saleCount;
    @XmlElement(name = "ReversalAmt")
    private Long reversalAmt;
    @XmlElement(name = "ReversalCount")
    private Long reversalCount;
    @XmlElement(name = "ReturnAmt")
    private Long returnAmt;
    @XmlElement(name = "ReturnCount")
    private Long returnCount;
    @XmlElement(name = "TotalAmt")
    private Long totalAmt;
    @XmlElement(name = "TotalCount")
    private Long totalCount;

    public Long getSaleAmt() {
        return saleAmt;
    }

    public void setSaleAmt(Long saleAmt) {
        this.saleAmt = saleAmt;
    }

    public Long getSaleCount() {
        return saleCount;
    }

    public void setSaleCount(Long saleCount) {
        this.saleCount = saleCount;
    }

    public Long getReversalAmt() {
        return reversalAmt;
    }

    public void setReversalAmt(Long reversalAmt) {
        this.reversalAmt = reversalAmt;
    }

    public Long getReversalCount() {
        return reversalCount;
    }

    public void setReversalCount(Long reversalCount) {
        this.reversalCount = reversalCount;
    }

    public Long getReturnAmt() {
        return returnAmt;
    }

    public void setReturnAmt(Long returnAmt) {
        this.returnAmt = returnAmt;
    }

    public Long getReturnCount() {
        return returnCount;
    }

    public void setReturnCount(Long returnCount) {
        this.returnCount = returnCount;
    }

    public Long getTotalAmt() {
        return totalAmt;
    }

    public void setTotalAmt(Long totalAmt) {
        this.totalAmt = totalAmt;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }
}
