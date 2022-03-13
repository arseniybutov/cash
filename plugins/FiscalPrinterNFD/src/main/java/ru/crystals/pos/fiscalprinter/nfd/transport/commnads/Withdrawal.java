package ru.crystals.pos.fiscalprinter.nfd.transport.commnads;


import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResponse;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.withdrawal.WithdrawalResponse;

import java.math.BigDecimal;

/**
 * Выполнение операции выплаты.
 */
public class Withdrawal extends BaseRequest {

    private static final String METHOD_NAME = "withdrawal";

    /**
     * Сумма выплаты.
     */
    private static final String SUM_PARAM_NAME = "sum";

    public Withdrawal() {
        putMethodParam(operatorPasswordParamName, operatorPasswordDefault);
    }

    public Withdrawal(BigDecimal sum) {
        putMethodParam(operatorPasswordParamName, operatorPasswordDefault);
        setSum(sum);
    }

    @Override
    public String getMethodName() {
        return METHOD_NAME;
    }

    @Override
    public Class<? extends BaseResponse> getClassResponse() {
        return WithdrawalResponse.class;
    }

    public BigDecimal getSum() {
        return (BigDecimal) getMethodParam(SUM_PARAM_NAME);
    }

    public void setSum(BigDecimal sum) {
        putMethodParam(SUM_PARAM_NAME, sum);
    }

}
