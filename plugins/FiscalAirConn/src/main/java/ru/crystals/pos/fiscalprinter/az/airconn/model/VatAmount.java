package ru.crystals.pos.fiscalprinter.az.airconn.model;

import java.math.BigDecimal;

public class VatAmount {
    private Float vatPercent;
    private BigDecimal vatSum;

    public Float getVatPercent() {
        return vatPercent;
    }

    public void setVatPercent(Float vatPercent) {
        this.vatPercent = vatPercent;
    }

    public BigDecimal getVatSum() {
        return vatSum;
    }

    public void setVatSum(BigDecimal vatSum) {
        this.vatSum = vatSum;
    }
}
