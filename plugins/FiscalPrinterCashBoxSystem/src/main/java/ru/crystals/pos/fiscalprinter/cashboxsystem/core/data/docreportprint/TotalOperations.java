package ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.docreportprint;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.CbsMoney;

/**
 * Описание итогов по операциям.
 */
public class TotalOperations {
    /**
     * Итоговое количество по всем операциям
     */
    @JsonProperty("count")
    private Integer count;
    /**
     * Количество продаж
     */
    @JsonProperty("count_sale")
    private Integer countSale;
    /**
     * Количество возвратов продаж
     */
    @JsonProperty("count_sale_return")
    private Integer countSaleReturn;
    /**
     * Количество покупок
     */
    @JsonProperty("count_buy")
    private Integer countBuy;
    /**
     * Количество возвратов покупок
     */
    @JsonProperty("count_buy_return")
    private Integer countBuyReturn;
    /**
     * Сумма продаж
     */
    @JsonProperty("sum_sale")
    private CbsMoney sumSale;
    /**
     * Сумма возвратов продаж
     */
    @JsonProperty("sum_sale_return")
    private CbsMoney sumSaleReturn;
    /**
     * Сумма покупок
     */
    @JsonProperty("sum_buy")
    private CbsMoney sumBuy;
    /**
     * Сумма возвратов покупок
     */
    @JsonProperty("sum_buy_return")
    private CbsMoney sumBuyReturn;

    public Integer getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Integer getCountSale() {
        return countSale;
    }

    public void setCountSale(int countSale) {
        this.countSale = countSale;
    }

    public Integer getCountSaleReturn() {
        return countSaleReturn;
    }

    public void setCountSaleReturn(int countSaleReturn) {
        this.countSaleReturn = countSaleReturn;
    }

    public Integer getCountBuy() {
        return countBuy;
    }

    public void setCountBuy(int countBuy) {
        this.countBuy = countBuy;
    }

    public Integer getCountBuyReturn() {
        return countBuyReturn;
    }

    public void setCountBuyReturn(int countBuyReturn) {
        this.countBuyReturn = countBuyReturn;
    }

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
