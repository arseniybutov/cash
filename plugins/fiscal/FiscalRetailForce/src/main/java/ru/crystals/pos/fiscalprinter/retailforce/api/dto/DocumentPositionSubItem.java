package ru.crystals.pos.fiscalprinter.retailforce.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.math.BigDecimal;


public class DocumentPositionSubItem implements DocumentPosition {

    @JsonUnwrapped
    private DocumentPositionBase common;

    @JsonProperty("quantity")
    private BigDecimal quantity;

    @JsonProperty("quantityUnit")
    private QuantityUnit quantityUnit;

    @JsonProperty("itemId")
    private String itemId;

    @JsonProperty("itemCaption")
    private String itemCaption;

    @JsonProperty("baseNetValue")
    private BigDecimal baseNetValue;

    @JsonProperty("baseGrossValue")
    private BigDecimal baseGrossValue;

    @JsonProperty("baseTaxValue")
    private BigDecimal baseTaxValue;

    @JsonProperty("gtin")
    private String gtin;

    @JsonProperty("businessTransactionType")
    private BusinessTransactionType businessTransactionType;

    @JsonProperty("vatIdentification")
    private Integer vatIdentification;

    @JsonProperty("vatPercent")
    private BigDecimal vatPercent;

    @JsonProperty("netValue")
    private BigDecimal netValue;

    @JsonProperty("grossValue")
    private BigDecimal grossValue;

    @JsonProperty("taxValue")
    private BigDecimal taxValue;

    @JsonProperty("accountingIdentifier")
    private String accountingIdentifier;

    public DocumentPositionBase getCommon() {
        return common;
    }

    public void setCommon(final DocumentPositionBase common) {
        this.common = common;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(final BigDecimal quantity) {
        this.quantity = quantity;
    }

    public QuantityUnit getQuantityUnit() {
        return quantityUnit;
    }

    public void setQuantityUnit(final QuantityUnit quantityUnit) {
        this.quantityUnit = quantityUnit;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(final String itemId) {
        this.itemId = itemId;
    }

    public String getItemCaption() {
        return itemCaption;
    }

    public void setItemCaption(final String itemCaption) {
        this.itemCaption = itemCaption;
    }

    public BigDecimal getBaseNetValue() {
        return baseNetValue;
    }

    public void setBaseNetValue(final BigDecimal baseNetValue) {
        this.baseNetValue = baseNetValue;
    }

    public BigDecimal getBaseGrossValue() {
        return baseGrossValue;
    }

    public void setBaseGrossValue(final BigDecimal baseGrossValue) {
        this.baseGrossValue = baseGrossValue;
    }

    public BigDecimal getBaseTaxValue() {
        return baseTaxValue;
    }

    public void setBaseTaxValue(final BigDecimal baseTaxValue) {
        this.baseTaxValue = baseTaxValue;
    }

    public String getGtin() {
        return gtin;
    }

    public void setGtin(final String gtin) {
        this.gtin = gtin;
    }

    public BusinessTransactionType getBusinessTransactionType() {
        return businessTransactionType;
    }

    public void setBusinessTransactionType(final BusinessTransactionType businessTransactionType) {
        this.businessTransactionType = businessTransactionType;
    }

    public Integer getVatIdentification() {
        return vatIdentification;
    }

    public void setVatIdentification(final Integer vatIdentification) {
        this.vatIdentification = vatIdentification;
    }

    public BigDecimal getVatPercent() {
        return vatPercent;
    }

    public void setVatPercent(final BigDecimal vatPercent) {
        this.vatPercent = vatPercent;
    }

    public BigDecimal getNetValue() {
        return netValue;
    }

    public void setNetValue(final BigDecimal netValue) {
        this.netValue = netValue;
    }

    public BigDecimal getGrossValue() {
        return grossValue;
    }

    public void setGrossValue(final BigDecimal grossValue) {
        this.grossValue = grossValue;
    }

    public BigDecimal getTaxValue() {
        return taxValue;
    }

    public void setTaxValue(final BigDecimal taxValue) {
        this.taxValue = taxValue;
    }

    public String getAccountingIdentifier() {
        return accountingIdentifier;
    }

    public void setAccountingIdentifier(final String accountingIdentifier) {
        this.accountingIdentifier = accountingIdentifier;
    }
}

