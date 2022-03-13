package ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.docreportprint;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.CbsMoney;

/**
 * Описание необнуляемых сумм ZX-отчета.
 */
public class ReportNoneNullableSum {
    /**
     * Сумма продаж
     */
    @JsonProperty("sale")
    private CbsMoney sumSale;
    /**
     * Сумма возвратов продаж
     */
    @JsonProperty("sale_return")
    private CbsMoney sumSaleReturn;
    /**
     * Сумма покупок
     */
    @JsonProperty("buy")
    private CbsMoney sumBuy;
    /**
     * Сумма возвратов покупок
     */
    @JsonProperty("buy_return")
    private CbsMoney sumBuyReturn;

    public CbsMoney getSumSale() {
        return sumSale;
    }

    public void setSumSale(CbsMoney sumSale) {
        this.sumSale = sumSale;
    }

    public CbsMoney getSumSaleReturn() {
        return sumSaleReturn;
    }

    public void setSumSaleReturn(CbsMoney sumSaleReturn) {
        this.sumSaleReturn = sumSaleReturn;
    }

    public CbsMoney getSumBuy() {
        return sumBuy;
    }

    public void setSumBuy(CbsMoney sumBuy) {
        this.sumBuy = sumBuy;
    }

    public CbsMoney getSumBuyReturn() {
        return sumBuyReturn;
    }

    public void setSumBuyReturn(CbsMoney sumBuyReturn) {
        this.sumBuyReturn = sumBuyReturn;
    }
}
