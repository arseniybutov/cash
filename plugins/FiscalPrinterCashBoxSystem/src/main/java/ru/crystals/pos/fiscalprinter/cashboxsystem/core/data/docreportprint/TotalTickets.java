package ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.docreportprint;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.Payments;

/**
 * Описание итогов по чекам с учетом скидок и наценок
 */
public class TotalTickets {
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
     * Информация об оплате по продажам
     */
    @JsonProperty("payments_sale")
    private Payments paymentsSale;
    /**
     * Информация об оплате по возвратам продаж
     */
    @JsonProperty("payments_sale_return")
    private Payments paymentsSaleReturn;
    /**
     * Информация об оплате по покупкам
     */
    @JsonProperty("payments_buy")
    private Payments paymentsBuy;
    /**
     * Информация об оплате по возвратам покупок
     */
    @JsonProperty("payments_buy_return")
    private Payments paymentsBuyReturn;

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

    public Payments getPaymentsSale() {
        return paymentsSale;
    }

    public void setPaymentsSale(Payments paymentsSale) {
        this.paymentsSale = paymentsSale;
    }

    public Payments getPaymentsSaleReturn() {
        return paymentsSaleReturn;
    }

    public void setPaymentsSaleReturn(Payments paymentsSaleReturn) {
        this.paymentsSaleReturn = paymentsSaleReturn;
    }

    public Payments getPaymentsBuy() {
        return paymentsBuy;
    }

    public void setPaymentsBuy(Payments paymentsBuy) {
        this.paymentsBuy = paymentsBuy;
    }

    public Payments getPaymentsBuyReturn() {
        return paymentsBuyReturn;
    }

    public void setPaymentsBuyReturn(Payments paymentsBuyReturn) {
        this.paymentsBuyReturn = paymentsBuyReturn;
    }
}
