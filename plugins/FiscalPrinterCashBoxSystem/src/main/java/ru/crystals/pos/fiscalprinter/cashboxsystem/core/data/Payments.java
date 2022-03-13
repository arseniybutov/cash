package ru.crystals.pos.fiscalprinter.cashboxsystem.core.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Объект с информацией об оплатах
 */
public class Payments {

    /**
     * Сумма оплаты наличными
     */
    @JsonProperty("cash")
    private CbsMoney cash;
    /**
     * Сумма оплаты дебитной картой
     */
    @JsonProperty("debit")
    private CbsMoney debit;
    /**
     * Сумма оплаты кредитом
     */
    @JsonProperty("credit")
    private CbsMoney credit;
    /**
     * Сумма оплата тарой
     */
    @JsonProperty("tare")
    private CbsMoney tare;

    public CbsMoney getCash() {
        return cash;
    }

    public void setCash(CbsMoney cash) {
        this.cash = cash;
    }

    public CbsMoney getDebit() {
        return debit;
    }

    public void setDebit(CbsMoney debit) {
        this.debit = debit;
    }

    public CbsMoney getCredit() {
        return credit;
    }

    public void setCredit(CbsMoney credit) {
        this.credit = credit;
    }

    public CbsMoney getTare() {
        return tare;
    }

    public void setTare(CbsMoney tare) {
        this.tare = tare;
    }
}
