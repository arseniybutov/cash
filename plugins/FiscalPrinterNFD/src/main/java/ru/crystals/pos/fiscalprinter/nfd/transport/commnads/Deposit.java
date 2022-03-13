package ru.crystals.pos.fiscalprinter.nfd.transport.commnads;


import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResponse;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.deposit.DepositResponse;

import java.math.BigDecimal;

/**
 * Выполнение операции внесения.
 */
public class Deposit extends BaseRequest {

    private static final String METHOD_NAME = "deposit";

    /**
     * Сумма внесения.
     */
    private static final String SUM_PARAM_NAME = "sum";

    public Deposit() {
        putMethodParam(operatorPasswordParamName, operatorPasswordDefault);
    }

    public Deposit(BigDecimal sum) {
        putMethodParam(operatorPasswordParamName, operatorPasswordDefault);
        setSum(sum);
    }

    @Override
    public String getMethodName() {
        return METHOD_NAME;
    }

    @Override
    public Class<? extends BaseResponse> getClassResponse() {
        return DepositResponse.class;
    }

    public BigDecimal getSum() {
        return (BigDecimal) getMethodParam(SUM_PARAM_NAME);
    }

    public void setSum(BigDecimal sum) {
        putMethodParam(SUM_PARAM_NAME, sum);
    }

}
