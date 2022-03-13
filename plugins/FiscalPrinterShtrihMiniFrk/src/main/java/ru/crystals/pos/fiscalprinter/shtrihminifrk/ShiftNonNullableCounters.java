package ru.crystals.pos.fiscalprinter.shtrihminifrk;

public class ShiftNonNullableCounters {

    /**
     * Cумма необнуляемых сменных итогов продаж (наличными)
     */
    private long sumNonNullableSales;

    /**
     * Cумма необнуляемых сменных итогов покупок
     */
    private long sumNonNullablePurchases;

    /**
     * Cумма необнуляемых сменных итогов возвратов продаж
     */
    private long sumNonNullableReturnSales;

    /**
     * Cумма необнуляемых сменных итогов возвратов покупок
     */
    private long sumNonNullableReturnPurchases;

    public long getSumNonNullableSales() {
        return sumNonNullableSales;
    }

    public void setSumNonNullableSales(long sumNonNullableSales) {
        this.sumNonNullableSales = sumNonNullableSales;
    }

    public long getSumNonNullablePurchases() {
        return sumNonNullablePurchases;
    }

    public void setSumNonNullablePurchases(long sumNonNullablePurchases) {
        this.sumNonNullablePurchases = sumNonNullablePurchases;
    }

    public long getSumNonNullableReturnSales() {
        return sumNonNullableReturnSales;
    }

    public void setSumNonNullableReturnSales(long sumNonNullableReturnSales) {
        this.sumNonNullableReturnSales = sumNonNullableReturnSales;
    }

    public long getSumNonNullableReturnPurchases() {
        return sumNonNullableReturnPurchases;
    }

    public void setSumNonNullableReturnPurchases(long sumNonNullableReturnPurchases) {
        this.sumNonNullableReturnPurchases = sumNonNullableReturnPurchases;
    }

    @Override
    public String toString() {
        return "ShiftNonNullableCounters{" +
                "sumNonNullableSales=" + sumNonNullableSales +
                ", sumNonNullablePurchases=" + sumNonNullablePurchases +
                ", sumNonNullableReturnSales=" + sumNonNullableReturnSales +
                ", sumNonNullableReturnPurchases=" + sumNonNullableReturnPurchases +
                '}';
    }
}
