package ru.crystals.pos.visualization.payments.bankqr.model;

import ru.crystals.pos.visualization.payments.common.AbstractPaymentModel;

public class BankQRPaymentModel extends AbstractPaymentModel<BankQRPaymentState, BankQRPaymentInfo> {

    @Override
    protected BankQRPaymentInfo getDefaultPaymentInfo() {
        return new BankQRPaymentInfo();
    }

    @Override
    protected BankQRPaymentState getDefaultPaymentState() {
        return BankQRPaymentState.NOT_SET;
    }

}
