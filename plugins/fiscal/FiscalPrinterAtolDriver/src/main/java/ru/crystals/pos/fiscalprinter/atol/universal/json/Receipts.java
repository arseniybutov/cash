package ru.crystals.pos.fiscalprinter.atol.universal.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Receipts {

    private ReceiptType buy;
    private ReceiptType buyReturn;
    private ReceiptType sell;
    private ReceiptType sellReturn;

    public ReceiptType getBuy() {
        return buy;
    }

    public void setBuy(ReceiptType buy) {
        this.buy = buy;
    }

    public ReceiptType getBuyReturn() {
        return buyReturn;
    }

    public void setBuyReturn(ReceiptType buyReturn) {
        this.buyReturn = buyReturn;
    }

    public ReceiptType getSell() {
        return sell;
    }

    public void setSell(ReceiptType sell) {
        this.sell = sell;
    }

    public ReceiptType getSellReturn() {
        return sellReturn;
    }

    public void setSellReturn(ReceiptType sellReturn) {
        this.sellReturn = sellReturn;
    }

    public static class ReceiptType {

        private Payments payments;
        private double sum;
        // только для сменных итогов
        private long count;

        public Payments getPayments() {
            return payments;
        }

        public void setPayments(Payments payments) {
            this.payments = payments;
        }

        public double getSum() {
            return sum;
        }

        public void setSum(double sum) {
            this.sum = sum;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public static class Payments {
            private double cash;
            private double credit;
            private double electronically;
            private double other;
            private double prepaid;
            @JsonProperty("userPaymentType-5")
            private double userPaymentType5;
            @JsonProperty("userPaymentType-6")
            private double userPaymentType6;
            @JsonProperty("userPaymentType-7")
            private double userPaymentType7;
            @JsonProperty("userPaymentType-8")
            private double userPaymentType8;
            @JsonProperty("userPaymentType-9")
            private double userPaymentType9;

            public double getCash() {
                return cash;
            }

            public void setCash(double cash) {
                this.cash = cash;
            }

            public double getCredit() {
                return credit;
            }

            public void setCredit(double credit) {
                this.credit = credit;
            }

            public double getElectronically() {
                return electronically;
            }

            public void setElectronically(double electronically) {
                this.electronically = electronically;
            }

            public double getOther() {
                return other;
            }

            public void setOther(double other) {
                this.other = other;
            }

            public double getPrepaid() {
                return prepaid;
            }

            public void setPrepaid(double prepaid) {
                this.prepaid = prepaid;
            }

            public double getUserPaymentType5() {
                return userPaymentType5;
            }

            public void setUserPaymentType5(double userPaymentType5) {
                this.userPaymentType5 = userPaymentType5;
            }

            public double getUserPaymentType6() {
                return userPaymentType6;
            }

            public void setUserPaymentType6(double userPaymentType6) {
                this.userPaymentType6 = userPaymentType6;
            }

            public double getUserPaymentType7() {
                return userPaymentType7;
            }

            public void setUserPaymentType7(double userPaymentType7) {
                this.userPaymentType7 = userPaymentType7;
            }

            public double getUserPaymentType8() {
                return userPaymentType8;
            }

            public void setUserPaymentType8(double userPaymentType8) {
                this.userPaymentType8 = userPaymentType8;
            }

            public double getUserPaymentType9() {
                return userPaymentType9;
            }

            public void setUserPaymentType9(double userPaymentType9) {
                this.userPaymentType9 = userPaymentType9;
            }
        }
    }
}
