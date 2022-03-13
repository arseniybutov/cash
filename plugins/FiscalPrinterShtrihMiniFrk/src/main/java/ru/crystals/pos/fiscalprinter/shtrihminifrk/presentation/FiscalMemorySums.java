package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

import java.math.BigInteger;

/**
 * суммы записей в ФП (фискальной памяти).
 * <p/>
 * Все суммы даны в МДЕ (Минимальных Денежных Единицах) - в "копейках".
 * 
 * @author aperevozchikov
 */
public class FiscalMemorySums {

    /**
     * Сумма сменных итогов продаж (8 байт)
     */
    private BigInteger salesSum;

    /**
     * Сумма сменных итог покупок. Если отрицательно - значит, информация не может быть получена (у фискального регистратора отсутствует ФП2)
     */
    private long purchasesSum = -1;

    /**
     * Сумма сменных возвратов продаж. Если отрицательно - значит, информация не может быть получена (у фискального регистратора отсутствует ФП2)
     */
    private long salesReturnsSum = -1;

    /**
     * Сумма сменных возвратов покупок. Если отрицательно - значит, информация не может быть получена (у фискального регистратора отсутствует ФП2)
     */
    private long purchasesReturnsSum = -1;

    @Override
    public String toString() {
        return String.format("fm-sums [sales: %s; purchases: %s; ret-sales: %s; ret-purchases: %s]", 
            getSalesSum(), getPurchasesSum(), getSalesReturnsSum(), getPurchasesReturnsSum());
    }
    
    // getters & setters
    
    public BigInteger getSalesSum() {
        return salesSum;
    }

    public void setSalesSum(BigInteger salesSum) {
        this.salesSum = salesSum;
    }

    public long getPurchasesSum() {
        return purchasesSum;
    }

    public void setPurchasesSum(long purchasesSum) {
        this.purchasesSum = purchasesSum;
    }

    public long getSalesReturnsSum() {
        return salesReturnsSum;
    }

    public void setSalesReturnsSum(long salesReturnsSum) {
        this.salesReturnsSum = salesReturnsSum;
    }

    public long getPurchasesReturnsSum() {
        return purchasesReturnsSum;
    }

    public void setPurchasesReturnsSum(long purchasesReturnsSum) {
        this.purchasesReturnsSum = purchasesReturnsSum;
    }
}