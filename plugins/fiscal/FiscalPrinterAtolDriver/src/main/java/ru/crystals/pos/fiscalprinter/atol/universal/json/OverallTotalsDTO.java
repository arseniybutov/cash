package ru.crystals.pos.fiscalprinter.atol.universal.json;

public class OverallTotalsDTO {

    private OverallTotals overallTotals;

    public OverallTotals getOverallTotals() {
        return overallTotals;
    }

    public void setOverallTotals(OverallTotals overallTotals) {
        this.overallTotals = overallTotals;
    }

    public static class OverallTotals {

        private Receipts receipts;

        public Receipts getReceipts() {
            return receipts;
        }

        public void setReceipts(Receipts receipts) {
            this.receipts = receipts;
        }

    }

}
