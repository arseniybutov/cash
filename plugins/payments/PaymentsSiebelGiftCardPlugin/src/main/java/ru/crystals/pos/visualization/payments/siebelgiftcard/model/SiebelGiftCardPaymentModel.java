package ru.crystals.pos.visualization.payments.siebelgiftcard.model;

import ru.crystals.pos.visualization.payments.common.AbstractPaymentModel;

public class SiebelGiftCardPaymentModel extends AbstractPaymentModel<SiebelGiftCardPaymentState, SiebelGiftCardPaymentInfo> {
    @Override
    protected SiebelGiftCardPaymentInfo getDefaultPaymentInfo() {
        return new SiebelGiftCardPaymentInfo();
    }

    @Override
    protected SiebelGiftCardPaymentState getDefaultPaymentState() {
        return SiebelGiftCardPaymentState.NOT_SET;
    }
}
