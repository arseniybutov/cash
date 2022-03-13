package ru.crystals.pos.visualization.payments.siebelbonuscard.model;

import ru.crystals.pos.visualization.payments.common.AbstractPaymentModel;

public class SiebelBonusCardPaymentModel extends AbstractPaymentModel<SiebelBonusCardPaymentState, SiebelBonusCardPaymentInfo> {
    @Override
    protected SiebelBonusCardPaymentInfo getDefaultPaymentInfo() {
        return new SiebelBonusCardPaymentInfo();
    }

    @Override
    protected SiebelBonusCardPaymentState getDefaultPaymentState() {
        return SiebelBonusCardPaymentState.NOT_SET;
    }
}
