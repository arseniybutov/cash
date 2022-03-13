package ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.docticketprint;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.CbsMoney;

/**
 * Позиция чека из DocTicketPrint.Body
 */
class Item {
    /**
     * Процент скидки
     */
    @JsonProperty("discount")
    private Double discount;
    /**
     * Процент наценки
     */
    @JsonProperty("markup")
    private Double markup;
    /**
     * Наименование позиции чека
     */
    @JsonProperty("name")
    private String name;
    /**
     * Цена за единицу
     */
    @JsonProperty("price")
    private CbsMoney price;
    /**
     * Количество
     */
    @JsonProperty("quantity")
    private Double quantity;
    /**
     * Итоговая сумма позиции с учетом наценки/скидки и округления
     */
    @JsonProperty("sum")
    private CbsMoney sum;

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Double getMarkup() {
        return markup;
    }

    public void setMarkup(Double markup) {
        this.markup = markup;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CbsMoney getPrice() {
        return price;
    }

    public void setPrice(CbsMoney price) {
        this.price = price;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public CbsMoney getSum() {
        return sum;
    }

    public void setSum(CbsMoney sum) {
        this.sum = sum;
    }
}
