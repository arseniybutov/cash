package ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.docticketprint;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.CbsMoney;

/**
 * Итоги чека из DocTicketPrint.Footer
 */
class Amounts {
    /**
     * Сумма сдачи
     */
    @JsonProperty("change")
    private CbsMoney change;
    /**
     * Сумма скидки
     */
    @JsonProperty("discount")
    private CbsMoney discount;
    /**
     * Сумма наценки
     */
    @JsonProperty("markup")
    private CbsMoney markup;
    /**
     * Итоговая сумма чека с учетом скидок/наценок
     */
    @JsonProperty("total")
    private CbsMoney total;

    public CbsMoney getChange() {
        return change;
    }

    public void setChange(CbsMoney change) {
        this.change = change;
    }

    public CbsMoney getDiscount() {
        return discount;
    }

    public void setDiscount(CbsMoney discount) {
        this.discount = discount;
    }

    public CbsMoney getMarkup() {
        return markup;
    }

    public void setMarkup(CbsMoney markup) {
        this.markup = markup;
    }

    public CbsMoney getTotal() {
        return total;
    }

    public void setTotal(CbsMoney total) {
        this.total = total;
    }
}
