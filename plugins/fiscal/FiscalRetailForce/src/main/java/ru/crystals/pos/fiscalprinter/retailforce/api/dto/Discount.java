package ru.crystals.pos.fiscalprinter.retailforce.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class Discount {
    @JsonProperty("discountValue")
    private BigDecimal discountValue;

    @JsonProperty("caption")
    private String caption;

    @JsonProperty("discountOrder")
    private int discountOrder;

    @JsonProperty("type")
    private DiscountType type;

    @JsonProperty("typeValue")
    private BigDecimal typeValue;

    public Discount() {
    }

    Discount(BigDecimal discountValue, String caption, int discountOrder, DiscountType type, BigDecimal typeValue) {
        this.discountValue = discountValue;
        this.caption = caption;
        this.discountOrder = discountOrder;
        this.type = type;
        this.typeValue = typeValue;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(final BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(final String caption) {
        this.caption = caption;
    }

    public int getDiscountOrder() {
        return discountOrder;
    }

    public void setDiscountOrder(final int discountOrder) {
        this.discountOrder = discountOrder;
    }

    public DiscountType getType() {
        return type;
    }

    public void setType(final DiscountType type) {
        this.type = type;
    }

    public BigDecimal getTypeValue() {
        return typeValue;
    }

    public void setTypeValue(final BigDecimal typeValue) {
        this.typeValue = typeValue;
    }

    public static DiscountBuilder builder() {
        return new DiscountBuilder();
    }

    public static class DiscountBuilder {
        private BigDecimal discountValue;
        private String caption;
        private int discountOrder;
        private DiscountType type;
        private BigDecimal typeValue;

        DiscountBuilder() {
        }

        public DiscountBuilder discountValue(BigDecimal discountValue) {
            this.discountValue = discountValue;
            return this;
        }

        public DiscountBuilder caption(String caption) {
            this.caption = caption;
            return this;
        }

        public DiscountBuilder discountOrder(int discountOrder) {
            this.discountOrder = discountOrder;
            return this;
        }

        public DiscountBuilder type(DiscountType type) {
            this.type = type;
            return this;
        }

        public DiscountBuilder typeValue(BigDecimal typeValue) {
            this.typeValue = typeValue;
            return this;
        }

        public Discount build() {
            return new Discount(discountValue, caption, discountOrder, type, typeValue);
        }
    }
}

