package ru.crystals.pos.fiscalprinter.atol.universal.json;

public class ShiftTotalsDTO {

    private ShiftTotals shiftTotals;

    public ShiftTotals getShiftTotals() {
        return shiftTotals;
    }

    public void setShiftTotals(ShiftTotals shiftTotals) {
        this.shiftTotals = shiftTotals;
    }

    public static class ShiftTotals {

        private MoneyOperation cashDrawer;
        private MoneyOperation income;
        private MoneyOperation outcome;
        private Receipts receipts;
        private long shiftNumber;

        public MoneyOperation getCashDrawer() {
            return cashDrawer;
        }

        public void setCashDrawer(MoneyOperation cashDrawer) {
            this.cashDrawer = cashDrawer;
        }

        public MoneyOperation getIncome() {
            return income;
        }

        public void setIncome(MoneyOperation income) {
            this.income = income;
        }

        public MoneyOperation getOutcome() {
            return outcome;
        }

        public void setOutcome(MoneyOperation outcome) {
            this.outcome = outcome;
        }

        public Receipts getReceipts() {
            return receipts;
        }

        public void setReceipts(Receipts receipts) {
            this.receipts = receipts;
        }

        public long getShiftNumber() {
            return shiftNumber;
        }

        public void setShiftNumber(long shiftNumber) {
            this.shiftNumber = shiftNumber;
        }

        /**
         * Для денег в ДЯ, внесений и изъятий
         */
        public static class MoneyOperation {
            /**
             * Количество операций (внесений/изъятий)
             */
            private long count;
            /**
             * Общая сумма операций
             */
            private double sum;

            public long getCount() {
                return count;
            }

            public void setCount(long count) {
                this.count = count;
            }

            public double getSum() {
                return sum;
            }

            public void setSum(double sum) {
                this.sum = sum;
            }
        }
    }
}
