package ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.docreportprint;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.CbsMoney;

/**
 * Описание итогов по налогам
 */
public class TotalTax {
    /**
     * Сумму налога по покупкам
     */
    @JsonProperty("sum_buy")
    private CbsMoney sumBuy;
    /**
     * Сумму налога по возвратам покупок
     */
    @JsonProperty("sum_buy_return")
    private CbsMoney sumBuyReturn;
    /**
     * Сумму налога по продажам
     */
    @JsonProperty("sum_sale")
    private CbsMoney sumSale;
    /**
     * Сумму налога по возвратам продаж
     */
    @JsonProperty("sum_sale_return")
    private CbsMoney sumSaleReturn;
    /**
     * Тип налога
     */
    @JsonProperty("tax_type")
    private Integer taxType;
    /**
     * Сумму оборота налога по покупкам
     */
    @JsonProperty("turnover_buy")
    private CbsMoney turnoverBuy;
    /**
     * Сумму оборота налога по возвратам покупок
     */
    @JsonProperty("turnover_buy_return")
    private CbsMoney turnoverBuyReturn;
    /**
     * Сумму оборота по продажам
     */
    @JsonProperty("turnover_sale")
    private CbsMoney turnoverSale;
    /**
     * Сумму оборота налога по возвратам продаж
     */
    @JsonProperty("turnover_sale_return")
    private CbsMoney turnoverSaleReturn;

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

    public Integer getTaxType() {
        return taxType;
    }

    public void setTaxType(Integer taxType) {
        this.taxType = taxType;
    }

    public CbsMoney getTurnoverBuy() {
        return turnoverBuy;
    }

    public void setTurnoverBuy(CbsMoney turnoverBuy) {
        this.turnoverBuy = turnoverBuy;
    }

    public CbsMoney getTurnoverBuyReturn() {
        return turnoverBuyReturn;
    }

    public void setTurnoverBuyReturn(CbsMoney turnoverBuyReturn) {
        this.turnoverBuyReturn = turnoverBuyReturn;
    }

    public CbsMoney getTurnoverSale() {
        return turnoverSale;
    }

    public void setTurnoverSale(CbsMoney turnoverSale) {
        this.turnoverSale = turnoverSale;
    }

    public CbsMoney getTurnoverSaleReturn() {
        return turnoverSaleReturn;
    }

    public void setTurnoverSaleReturn(CbsMoney turnoverSaleReturn) {
        this.turnoverSaleReturn = turnoverSaleReturn;
    }
}
