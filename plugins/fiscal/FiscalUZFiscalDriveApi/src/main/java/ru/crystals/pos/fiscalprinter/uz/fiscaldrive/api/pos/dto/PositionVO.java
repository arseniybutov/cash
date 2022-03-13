package ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.dto;

public class PositionVO {
    private final String item;
    private final String name;
    private final long startSum;
    private final long quantity;
    private final long discount;
    private final long vat;

    private PositionVO(String item, String name, long startSum, long quantity, long discount, long vat) {
        this.item = item;
        this.name = name;
        this.startSum = startSum;
        this.quantity = quantity;
        this.discount = discount;
        this.vat = vat;
    }

    public static PositionVOBuilder builder() {
        return new PositionVOBuilder();
    }

    public String getItem() {
        return item;
    }

    public String getName() {
        return name;
    }

    public long getStartSum() {
        return startSum;
    }

    public long getQuantity() {
        return quantity;
    }

    public long getDiscount() {
        return discount;
    }

    public long getVat() {
        return vat;
    }

    public static class PositionVOBuilder {
        private String item;
        private String name;
        private long startSum;
        private long quantity;
        private long discount;
        private long vat;

        PositionVOBuilder() {
        }

        public PositionVOBuilder item(String item) {
            this.item = item;
            return this;
        }

        public PositionVOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public PositionVOBuilder startSum(long startSum) {
            this.startSum = startSum;
            return this;
        }

        public PositionVOBuilder quantity(long quantity) {
            this.quantity = quantity;
            return this;
        }

        public PositionVOBuilder discount(long discount) {
            this.discount = discount;
            return this;
        }

        public PositionVOBuilder vat(long vat) {
            this.vat = vat;
            return this;
        }

        public PositionVO build() {
            return new PositionVO(item, name, startSum, quantity, discount, vat);
        }
    }
}
