package ru.crystals.pos.fiscalprinter.retailforce.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.math.BigDecimal;
import java.util.List;

public class DocumentPositionItem implements DocumentPosition {

    @JsonUnwrapped
    private DocumentPositionBase common;

    @JsonProperty("itemCaption")
    private String itemCaption;

    @JsonProperty("itemShortCaption")
    private String itemShortCaption;

    @JsonProperty("discounts")
    private List<Discount> discounts;

    @JsonProperty("useSubItemVatCalculation")
    private boolean useSubItemVatCalculation;

    @JsonProperty("subItems")
    private List<DocumentPositionSubItem> subItems;

    @JsonProperty("quantity")
    private BigDecimal quantity;

    @JsonProperty("quantityUnit")
    private QuantityUnit quantityUnit;

    @JsonProperty("itemId")
    private String itemId;

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
    private int vatIdentification;

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

    public DocumentPositionItem() {
    }

    DocumentPositionItem(DocumentPositionBase common,
                         String itemCaption,
                         String itemShortCaption,
                         List<Discount> discounts,
                         boolean useSubItemVatCalculation,
                         List<DocumentPositionSubItem> subItems,
                         BigDecimal quantity,
                         QuantityUnit quantityUnit,
                         String itemId,
                         BigDecimal baseNetValue,
                         BigDecimal baseGrossValue,
                         BigDecimal baseTaxValue,
                         String gtin,
                         BusinessTransactionType businessTransactionType,
                         int vatIdentification,
                         BigDecimal vatPercent,
                         BigDecimal netValue,
                         BigDecimal grossValue,
                         BigDecimal taxValue,
                         String accountingIdentifier) {
        this.common = common;
        this.itemCaption = itemCaption;
        this.itemShortCaption = itemShortCaption;
        this.discounts = discounts;
        this.useSubItemVatCalculation = useSubItemVatCalculation;
        this.subItems = subItems;
        this.quantity = quantity;
        this.quantityUnit = quantityUnit;
        this.itemId = itemId;
        this.baseNetValue = baseNetValue;
        this.baseGrossValue = baseGrossValue;
        this.baseTaxValue = baseTaxValue;
        this.gtin = gtin;
        this.businessTransactionType = businessTransactionType;
        this.vatIdentification = vatIdentification;
        this.vatPercent = vatPercent;
        this.netValue = netValue;
        this.grossValue = grossValue;
        this.taxValue = taxValue;
        this.accountingIdentifier = accountingIdentifier;
    }

    public DocumentPositionBase getCommon() {
        return common;
    }

    public void setCommon(final DocumentPositionBase common) {
        this.common = common;
    }

    public String getItemCaption() {
        return itemCaption;
    }

    public void setItemCaption(final String itemCaption) {
        this.itemCaption = itemCaption;
    }

    public String getItemShortCaption() {
        return itemShortCaption;
    }

    public void setItemShortCaption(final String itemShortCaption) {
        this.itemShortCaption = itemShortCaption;
    }

    public List<Discount> getDiscounts() {
        return discounts;
    }

    public void setDiscounts(final List<Discount> discounts) {
        this.discounts = discounts;
    }

    public boolean isUseSubItemVatCalculation() {
        return useSubItemVatCalculation;
    }

    public void setUseSubItemVatCalculation(final boolean useSubItemVatCalculation) {
        this.useSubItemVatCalculation = useSubItemVatCalculation;
    }

    public List<DocumentPositionSubItem> getSubItems() {
        return subItems;
    }

    public void setSubItems(final List<DocumentPositionSubItem> subItems) {
        this.subItems = subItems;
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

    public int getVatIdentification() {
        return vatIdentification;
    }

    public void setVatIdentification(final int vatIdentification) {
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

    public static DocumentPositionItemBuilder builder() {
        return new DocumentPositionItemBuilder();
    }

    public static class DocumentPositionItemBuilder {
        private DocumentPositionBase common;
        private String itemCaption;
        private String itemShortCaption;
        private List<Discount> discounts;
        private boolean useSubItemVatCalculation;
        private List<DocumentPositionSubItem> subItems;
        private BigDecimal quantity;
        private QuantityUnit quantityUnit;
        private String itemId;
        private BigDecimal baseNetValue;
        private BigDecimal baseGrossValue;
        private BigDecimal baseTaxValue;
        private String gtin;
        private BusinessTransactionType businessTransactionType;
        private int vatIdentification;
        private BigDecimal vatPercent;
        private BigDecimal netValue;
        private BigDecimal grossValue;
        private BigDecimal taxValue;
        private String accountingIdentifier;

        DocumentPositionItemBuilder() {
        }

        public DocumentPositionItemBuilder common(DocumentPositionBase common) {
            this.common = common;
            return this;
        }

        public DocumentPositionItemBuilder itemCaption(String itemCaption) {
            this.itemCaption = itemCaption;
            return this;
        }

        public DocumentPositionItemBuilder itemShortCaption(String itemShortCaption) {
            this.itemShortCaption = itemShortCaption;
            return this;
        }

        public DocumentPositionItemBuilder discounts(List<Discount> discounts) {
            this.discounts = discounts;
            return this;
        }

        public DocumentPositionItemBuilder useSubItemVatCalculation(boolean useSubItemVatCalculation) {
            this.useSubItemVatCalculation = useSubItemVatCalculation;
            return this;
        }

        public DocumentPositionItemBuilder subItems(List<DocumentPositionSubItem> subItems) {
            this.subItems = subItems;
            return this;
        }

        public DocumentPositionItemBuilder quantity(BigDecimal quantity) {
            this.quantity = quantity;
            return this;
        }

        public DocumentPositionItemBuilder quantityUnit(QuantityUnit quantityUnit) {
            this.quantityUnit = quantityUnit;
            return this;
        }

        public DocumentPositionItemBuilder itemId(String itemId) {
            this.itemId = itemId;
            return this;
        }

        public DocumentPositionItemBuilder baseNetValue(BigDecimal baseNetValue) {
            this.baseNetValue = baseNetValue;
            return this;
        }

        public DocumentPositionItemBuilder baseGrossValue(BigDecimal baseGrossValue) {
            this.baseGrossValue = baseGrossValue;
            return this;
        }

        public DocumentPositionItemBuilder baseTaxValue(BigDecimal baseTaxValue) {
            this.baseTaxValue = baseTaxValue;
            return this;
        }

        public DocumentPositionItemBuilder gtin(String gtin) {
            this.gtin = gtin;
            return this;
        }

        public DocumentPositionItemBuilder businessTransactionType(BusinessTransactionType businessTransactionType) {
            this.businessTransactionType = businessTransactionType;
            return this;
        }

        public DocumentPositionItemBuilder vatIdentification(int vatIdentification) {
            this.vatIdentification = vatIdentification;
            return this;
        }

        public DocumentPositionItemBuilder vatPercent(BigDecimal vatPercent) {
            this.vatPercent = vatPercent;
            return this;
        }

        public DocumentPositionItemBuilder netValue(BigDecimal netValue) {
            this.netValue = netValue;
            return this;
        }

        public DocumentPositionItemBuilder grossValue(BigDecimal grossValue) {
            this.grossValue = grossValue;
            return this;
        }

        public DocumentPositionItemBuilder taxValue(BigDecimal taxValue) {
            this.taxValue = taxValue;
            return this;
        }

        public DocumentPositionItemBuilder accountingIdentifier(String accountingIdentifier) {
            this.accountingIdentifier = accountingIdentifier;
            return this;
        }

        public DocumentPositionItem build() {
            return new DocumentPositionItem(common,
                    itemCaption,
                    itemShortCaption,
                    discounts,
                    useSubItemVatCalculation,
                    subItems,
                    quantity,
                    quantityUnit,
                    itemId,
                    baseNetValue,
                    baseGrossValue,
                    baseTaxValue,
                    gtin,
                    businessTransactionType,
                    vatIdentification,
                    vatPercent,
                    netValue,
                    grossValue,
                    taxValue,
                    accountingIdentifier);
        }
    }
}

