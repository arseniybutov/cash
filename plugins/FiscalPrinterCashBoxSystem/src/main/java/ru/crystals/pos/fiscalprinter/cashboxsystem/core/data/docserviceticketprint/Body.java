package ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.docserviceticketprint;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.CbsMoney;

/**
 * Описание объекта тела чека внесения/изъятия наличных.
 */
public class Body {
    /**
     * Сумма операции
     */
    @JsonProperty("sum")
    private CbsMoney sum;
    /**
     * Наименование операции
     */
    @JsonProperty("operation_name")
    private String operationName;

    public CbsMoney getSum() {
        return sum;
    }

    public void setSum(CbsMoney sum) {
        this.sum = sum;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }
}
