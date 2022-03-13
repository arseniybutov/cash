package ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.docticketprint;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.CbsMoney;

/**
 * Информация о налогах из DocTicketPrint.Footer
 */
class FooterTax {

    /**
     * Сумма оплаты
     */
    @JsonProperty("percent")
    private Double percent;
    /**
     * Сумма оплаты
     */
    @JsonProperty("sum")
    private CbsMoney sum;
    /**
     * Сумма оплаты
     */
    @JsonProperty("tax_type")
    private Integer taxType;

    public Double getPercent() {
        return percent;
    }

    public void setPercent(Double percent) {
        this.percent = percent;
    }

    public CbsMoney getSum() {
        return sum;
    }

    public void setSum(CbsMoney sum) {
        this.sum = sum;
    }

    public Integer getTaxType() {
        return taxType;
    }

    public void setTaxType(Integer taxType) {
        this.taxType = taxType;
    }
}
