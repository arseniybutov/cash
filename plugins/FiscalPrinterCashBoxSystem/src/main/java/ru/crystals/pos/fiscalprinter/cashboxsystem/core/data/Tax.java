package ru.crystals.pos.fiscalprinter.cashboxsystem.core.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.enums.TaxationType;

/**
 * Описывает тип налога и режим налогооблажения.
 */
public class Tax {

    @JsonProperty("tax_type")
    private Integer taxType;
    /**
     * Режим налогооблажения, по умолчанию используем ОСН
     */
    @JsonProperty("taxation_type")
    private Integer taxationType = TaxationType.TAXATION_RTS.getCode();
    /**
     * Процентная ставка налога, должна быть больше 0 и меньше 100
     */
    @JsonProperty("percent")
    private Double percent;

    public Integer getTaxType() {
        return taxType;
    }

    public void setTaxType(int taxType) {
        this.taxType = taxType;
    }

    public Integer getTaxationType() {
        return taxationType;
    }

    public void setTaxationType(int taxationType) {
        this.taxationType = taxationType;
    }

    public Double getPercent() {
        return percent;
    }

    public void setPercent(Double percent) {
        this.percent = percent;
    }
}
