package ru.crystals.pos.fiscalprinter.cashboxsystem.core.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Объект с необнуляемыми суммами.
 */
public class NoneNullableSum {
    /**
     * Сумма продаж
     */
    @JsonProperty("sale")
    private CbsMoney sale;
    /**
     * Сумма возвратов продаж
     */
    @JsonProperty("sale_return")
    private CbsMoney saleReturn;
    /**
     * Сумма покупок
     */
    @JsonProperty("buy")
    private CbsMoney buy;
    /**
     * Сумма возвратов покупок
     */
    @JsonProperty("buy_return")
    private CbsMoney buyReturn;

    public CbsMoney getSale() {
        return sale;
    }

    public void setSale(CbsMoney sale) {
        this.sale = sale;
    }

    public CbsMoney getSaleReturn() {
        return saleReturn;
    }

    public void setSaleReturn(CbsMoney saleReturn) {
        this.saleReturn = saleReturn;
    }

    public CbsMoney getBuy() {
        return buy;
    }

    public void setBuy(CbsMoney buy) {
        this.buy = buy;
    }

    public CbsMoney getBuyReturn() {
        return buyReturn;
    }

    public void setBuyReturn(CbsMoney buyReturn) {
        this.buyReturn = buyReturn;
    }
}
