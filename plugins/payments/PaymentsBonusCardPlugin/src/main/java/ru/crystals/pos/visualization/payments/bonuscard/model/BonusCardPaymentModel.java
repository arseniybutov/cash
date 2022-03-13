package ru.crystals.pos.visualization.payments.bonuscard.model;

import ru.crystals.pos.visualization.payments.common.AbstractPaymentModel;


public class BonusCardPaymentModel extends AbstractPaymentModel<BonusCardPaymentState, BonusCardPaymentInfo> {
    @Override
    protected BonusCardPaymentInfo getDefaultPaymentInfo() {
        return new BonusCardPaymentInfo();
    }

    @Override
    protected BonusCardPaymentState getDefaultPaymentState() {
        return BonusCardPaymentState.NOT_SET;
    }
}
