package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

/**
 * Сменные счетчики, извлеченные из ФР.
 * <p/>
 * NOTE: все денежные суммы указаны в МДЕ (минимальных денежных единицах) - в "копейках".
 *
 * @author aperevozchikov
 */
public class ShtrihShiftCounters {

    /**
     * количество накопившихся денег в кассе [за смену]
     */
    private long cashSum;

    /**
     * Сумма продаж в смене
     */
    private long sumSale;

    /**
     * Сумма возвратов в смене
     */
    private long sumReturn;

    /**
     * Сумма расходов в смене
     */
    private long sumExpense;

    /**
     * Сумма возвратов расхода в смене
     */
    private long sumReturnExpense;

    /**
     * Количество продаж за смену
     */
    private long countSale;

    /**
     * Количество возвратов за смену
     */
    private long countReturn;

    /**
     * Количество расходов
     */
    private long countExpense;

    /**
     * Количество возвратов расхода
     */
    private long countReturnExpense;

    @Override
    public String toString() {
        return String.format("shtrih-shift-counters [sum: %s; sales.sum: %s; sales.count: %s; returns.sum: %s; returns.count: %s," +
                        " expense.count: %s, expense.sum: %s, expense-returns.count: %s, expense-returns.sum: %s] ",
                getCashSum(), getSumSale(), getCountSale(), getSumReturn(), getCountReturn(),
                getCountExpense(), getSumExpense(), getCountReturnExpense(), getSumReturnExpense()
        );
    }

    // getters & setters

    public long getCashSum() {
        return cashSum;
    }

    public void setCashSum(long cashSum) {
        this.cashSum = cashSum;
    }

    public long getSumSale() {
        return sumSale;
    }

    public void setSumSale(long sumSale) {
        this.sumSale = sumSale;
    }

    public long getSumReturn() {
        return sumReturn;
    }

    public void setSumReturn(long sumReturn) {
        this.sumReturn = sumReturn;
    }

    public long getSumExpense() {
        return sumExpense;
    }

    public void setSumExpense(long sumExpense) {
        this.sumExpense = sumExpense;
    }

    public long getSumReturnExpense() {
        return sumReturnExpense;
    }

    public void setSumReturnExpense(long sumReturnExpense) {
        this.sumReturnExpense = sumReturnExpense;
    }

    public long getCountSale() {
        return countSale;
    }

    public void setCountSale(long countSale) {
        this.countSale = countSale;
    }

    public long getCountReturn() {
        return countReturn;
    }

    public void setCountReturn(long countReturn) {
        this.countReturn = countReturn;
    }

    public long getCountExpense() {
        return countExpense;
    }

    public void setCountExpense(long countExpense) {
        this.countExpense = countExpense;
    }

    public long getCountReturnExpense() {
        return countReturnExpense;
    }

    public void setCountReturnExpense(long countReturnExpense) {
        this.countReturnExpense = countReturnExpense;
    }
}