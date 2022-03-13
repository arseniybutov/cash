package ru.crystals.pos.visualization.payments.bankcard.model;

import ru.crystals.pos.visualization.payments.common.AbstractPaymentModel;

/**
 * Created by agaydenger on 17.11.16.
 */
public class BankCardPaymentModel extends AbstractPaymentModel<BankCardPaymentState, BankCardPaymentInfo> {

    @Override
    protected BankCardPaymentInfo getDefaultPaymentInfo() {
        return new BankCardPaymentInfo();
    }

    @Override
    protected BankCardPaymentState getDefaultPaymentState() {
        return BankCardPaymentState.NOT_SET;
    }

}
