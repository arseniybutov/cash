package ru.crystals.pos.fiscalprinter.retailforce.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.math.BigDecimal;
import java.util.List;

public class DocumentPositionTotal implements DocumentPosition {

    @JsonUnwrapped
    private DocumentPositionBase common;

    @JsonProperty("baseValue")
    private BigDecimal baseValue;

    @JsonProperty("value")
    private BigDecimal value;

    @JsonProperty("discounts")
    private List<Discount> discounts;

    public DocumentPositionBase getCommon() {
        return common;
    }

    public void setCommon(final DocumentPositionBase common) {
        this.common = common;
    }

    public BigDecimal getBaseValue() {
        return baseValue;
    }

    public void setBaseValue(final BigDecimal baseValue) {
        this.baseValue = baseValue;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(final BigDecimal value) {
        this.value = value;
    }

    public List<Discount> getDiscounts() {
        return discounts;
    }

    public void setDiscounts(final List<Discount> discounts) {
        this.discounts = discounts;
    }
}

