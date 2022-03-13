package ru.crystals.pos.fiscalprinter.az.airconn.model;

import java.math.BigDecimal;

public class Item {
    private String itemName;
    private Integer itemCodeType;
    private String itemCode;
    private Integer itemQuantityType;
    private BigDecimal itemQuantity;
    private BigDecimal itemPrice;
    private BigDecimal itemSum;
    private Float itemVatPercent;

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getItemCodeType() {
        return itemCodeType;
    }

    public void setItemCodeType(Integer itemCodeType) {
        this.itemCodeType = itemCodeType;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public Integer getItemQuantityType() {
        return itemQuantityType;
    }

    public void setItemQuantityType(Integer itemQuantityType) {
        this.itemQuantityType = itemQuantityType;
    }

    public BigDecimal getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(BigDecimal itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    public BigDecimal getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(BigDecimal itemPrice) {
        this.itemPrice = itemPrice;
    }

    public BigDecimal getItemSum() {
        return itemSum;
    }

    public void setItemSum(BigDecimal itemSum) {
        this.itemSum = itemSum;
    }

    public Float getItemVatPercent() {
        return itemVatPercent;
    }

    public void setItemVatPercent(Float itemVatPercent) {
        this.itemVatPercent = itemVatPercent;
    }
}
