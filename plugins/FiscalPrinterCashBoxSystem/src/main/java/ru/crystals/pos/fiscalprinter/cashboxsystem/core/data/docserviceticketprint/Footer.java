package ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.docserviceticketprint;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.CbsMoney;

/**
 * Описание объекта подвала чека внесения/изъятия наличных.
 */
public class Footer {
    /**
     * Текущая сумма в кассе
     */
    @JsonProperty("current_cash_sum")
    private CbsMoney currentCashSum;

    public CbsMoney getCurrentCashSum() {
        return currentCashSum;
    }

    public void setCurrentCashSum(CbsMoney currentCashSum) {
        this.currentCashSum = currentCashSum;
    }
}
