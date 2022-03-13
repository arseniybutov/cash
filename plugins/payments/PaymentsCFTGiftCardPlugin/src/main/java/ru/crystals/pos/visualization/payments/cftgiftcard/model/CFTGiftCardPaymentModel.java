package ru.crystals.pos.visualization.payments.cftgiftcard.model;

import ru.crystals.pos.visualization.payments.common.AbstractPaymentModel;

public class CFTGiftCardPaymentModel extends AbstractPaymentModel<CFTGiftCardPaymentState, CFTGiftCardPaymentInfo> {

    @Override
    protected CFTGiftCardPaymentInfo getDefaultPaymentInfo() {
        return new CFTGiftCardPaymentInfo();
    }

    @Override
    protected CFTGiftCardPaymentState getDefaultPaymentState() {
        return CFTGiftCardPaymentState.NOT_SET;
    }
}
