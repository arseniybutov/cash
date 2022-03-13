package ru.crystals.pos.fiscalprinter.retailforce.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.math.BigDecimal;

public class DocumentPositionBooking implements DocumentPosition {

    @JsonUnwrapped
    private DocumentPositionBase common;

    @JsonProperty("caption")
    private String caption;

    @JsonProperty("identifier")
    private String identifier;

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

    public DocumentPositionBooking() {
    }

    DocumentPositionBooking(DocumentPositionBase common,
                            String caption,
                            String identifier,
                            BusinessTransactionType businessTransactionType,
                            Integer vatIdentification,
                            BigDecimal vatPercent,
                            BigDecimal netValue,
                            BigDecimal grossValue,
                            BigDecimal taxValue,
                            String accountingIdentifier) {
        this.common = common;
        this.caption = caption;
        this.identifier = identifier;
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

    public String getCaption() {
        return caption;
    }

    public void setCaption(final String caption) {
        this.caption = caption;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(final String identifier) {
        this.identifier = identifier;
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

    public static DocumentPositionBookingBuilder builder() {
        return new DocumentPositionBookingBuilder();
    }

    public static class DocumentPositionBookingBuilder {
        private DocumentPositionBase common;
        private String caption;
        private String identifier;
        private BusinessTransactionType businessTransactionType;
        private Integer vatIdentification;
        private BigDecimal vatPercent;
        private BigDecimal netValue;
        private BigDecimal grossValue;
        private BigDecimal taxValue;
        private String accountingIdentifier;

        DocumentPositionBookingBuilder() {
        }

        public DocumentPositionBookingBuilder common(DocumentPositionBase common) {
            this.common = common;
            return this;
        }

        public DocumentPositionBookingBuilder caption(String caption) {
            this.caption = caption;
            return this;
        }

        public DocumentPositionBookingBuilder identifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public DocumentPositionBookingBuilder businessTransactionType(BusinessTransactionType businessTransactionType) {
            this.businessTransactionType = businessTransactionType;
            return this;
        }

        public DocumentPositionBookingBuilder vatIdentification(Integer vatIdentification) {
            this.vatIdentification = vatIdentification;
            return this;
        }

        public DocumentPositionBookingBuilder vatPercent(BigDecimal vatPercent) {
            this.vatPercent = vatPercent;
            return this;
        }

        public DocumentPositionBookingBuilder netValue(BigDecimal netValue) {
            this.netValue = netValue;
            return this;
        }

        public DocumentPositionBookingBuilder grossValue(BigDecimal grossValue) {
            this.grossValue = grossValue;
            return this;
        }

        public DocumentPositionBookingBuilder taxValue(BigDecimal taxValue) {
            this.taxValue = taxValue;
            return this;
        }

        public DocumentPositionBookingBuilder accountingIdentifier(String accountingIdentifier) {
            this.accountingIdentifier = accountingIdentifier;
            return this;
        }

        public DocumentPositionBooking build() {
            return new DocumentPositionBooking(common,
                    caption,
                    identifier,
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

