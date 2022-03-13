package ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.docreportprint;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.CbsMoney;

/**
 * Итоги по операциям внесения/изъятия
 */
public class TotalMoneyOperations {
    /**
     * Сумма внесения наличных в кассу
     */
    @JsonProperty("sum_deposit")
    private CbsMoney sumDeposit;
    /**
     * Сумма изъятия наличных из кассы
     */
    @JsonProperty("sum_withdraw")
    private CbsMoney sumWithdraw;

    public CbsMoney getSumDeposit() {
        return sumDeposit;
    }

    public void setSumDeposit(CbsMoney sumDeposit) {
        this.sumDeposit = sumDeposit;
    }

    public CbsMoney getSumWithdraw() {
        return sumWithdraw;
    }

    public void setSumWithdraw(CbsMoney sumWithdraw) {
        this.sumWithdraw = sumWithdraw;
    }
}
