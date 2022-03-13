package ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.dto;

import java.time.LocalDateTime;

public class ShiftVO {

    private final long number;
    private final LocalDateTime openTime;
    private final LocalDateTime closeTime;
    private final long firstReceiptSeq;
    private final long lastReceiptSeq;
    private final long totalSaleCount;
    private final long totalSaleCard;
    private final long totalSaleCash;
    private final long totalSaleVAT;
    private final long totalRefundCount;
    private final long totalRefundCard;
    private final long totalRefundCash;
    private final long totalRefundVAT;

    private ShiftVO(long number, LocalDateTime openTime, LocalDateTime closeTime, long firstReceiptSeq, long lastReceiptSeq,
                    long totalSaleCount, long totalSaleCard, long totalSaleCash, long totalSaleVAT,
                    long totalRefundCount, long totalRefundCard, long totalRefundCash, long totalRefundVAT) {
        this.number = number;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.firstReceiptSeq = firstReceiptSeq;
        this.lastReceiptSeq = lastReceiptSeq;
        this.totalSaleCount = totalSaleCount;
        this.totalSaleCard = totalSaleCard;
        this.totalSaleCash = totalSaleCash;
        this.totalSaleVAT = totalSaleVAT;
        this.totalRefundCount = totalRefundCount;
        this.totalRefundCard = totalRefundCard;
        this.totalRefundCash = totalRefundCash;
        this.totalRefundVAT = totalRefundVAT;
    }

    public static ShiftVOBuilder builder() {
        return new ShiftVOBuilder();
    }

    public long getNumber() {
        return number;
    }

    public LocalDateTime getOpenTime() {
        return openTime;
    }

    public LocalDateTime getCloseTime() {
        return closeTime;
    }

    public long getFirstReceiptSeq() {
        return firstReceiptSeq;
    }

    public long getLastReceiptSeq() {
        return lastReceiptSeq;
    }

    public long getTotalSaleCount() {
        return totalSaleCount;
    }

    public long getTotalSaleCard() {
        return totalSaleCard;
    }

    public long getTotalSaleCash() {
        return totalSaleCash;
    }

    public long getTotalSaleVAT() {
        return totalSaleVAT;
    }

    public long getTotalRefundCount() {
        return totalRefundCount;
    }

    public long getTotalRefundCard() {
        return totalRefundCard;
    }

    public long getTotalRefundCash() {
        return totalRefundCash;
    }

    public long getTotalRefundVAT() {
        return totalRefundVAT;
    }

    public static class ShiftVOBuilder {
        private long number;
        private LocalDateTime openTime;
        private LocalDateTime closeTime;
        private long firstReceiptSeq;
        private long lastReceiptSeq;
        private long totalSaleCount;
        private long totalSaleCard;
        private long totalSaleCash;
        private long totalSaleVAT;
        private long totalRefundCount;
        private long totalRefundCard;
        private long totalRefundCash;
        private long totalRefundVAT;

        ShiftVOBuilder() {
        }

        public ShiftVOBuilder number(long number) {
            this.number = number;
            return this;
        }

        public ShiftVOBuilder openTime(LocalDateTime openTime) {
            this.openTime = openTime;
            return this;
        }

        public ShiftVOBuilder closeTime(LocalDateTime closeTime) {
            this.closeTime = closeTime;
            return this;
        }

        public ShiftVOBuilder firstReceiptSeq(long firstReceiptSeq) {
            this.firstReceiptSeq = firstReceiptSeq;
            return this;
        }

        public ShiftVOBuilder lastReceiptSeq(long lastReceiptSeq) {
            this.lastReceiptSeq = lastReceiptSeq;
            return this;
        }

        public ShiftVOBuilder totalSaleCount(long totalSaleCount) {
            this.totalSaleCount = totalSaleCount;
            return this;
        }

        public ShiftVOBuilder totalSaleCard(long totalSaleCard) {
            this.totalSaleCard = totalSaleCard;
            return this;
        }

        public ShiftVOBuilder totalSaleCash(long totalSaleCash) {
            this.totalSaleCash = totalSaleCash;
            return this;
        }

        public ShiftVOBuilder totalSaleVAT(long totalSaleVAT) {
            this.totalSaleVAT = totalSaleVAT;
            return this;
        }

        public ShiftVOBuilder totalRefundCount(long totalRefundCount) {
            this.totalRefundCount = totalRefundCount;
            return this;
        }

        public ShiftVOBuilder totalRefundCard(long totalRefundCard) {
            this.totalRefundCard = totalRefundCard;
            return this;
        }

        public ShiftVOBuilder totalRefundCash(long totalRefundCash) {
            this.totalRefundCash = totalRefundCash;
            return this;
        }

        public ShiftVOBuilder totalRefundVAT(long totalRefundVAT) {
            this.totalRefundVAT = totalRefundVAT;
            return this;
        }

        public ShiftVO build() {
            return new ShiftVO(number, openTime, closeTime, firstReceiptSeq, lastReceiptSeq, totalSaleCount, totalSaleCard, totalSaleCash, totalSaleVAT,
                    totalRefundCount, totalRefundCard, totalRefundCash, totalRefundVAT);
        }
    }
}
