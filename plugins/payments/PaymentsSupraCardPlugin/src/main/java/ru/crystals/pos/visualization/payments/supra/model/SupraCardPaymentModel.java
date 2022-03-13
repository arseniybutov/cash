package ru.crystals.pos.visualization.payments.supra.model;

import ru.crystals.pos.visualization.payments.common.AbstractPaymentModel;

/**
 * Created by s.pavlikhin on 08.06.2017.
 */
public class SupraCardPaymentModel extends AbstractPaymentModel<SupraCardPaymentState, SupraCardPaymentInfo> {

    @Override
    protected SupraCardPaymentInfo getDefaultPaymentInfo() {
        return new SupraCardPaymentInfo();
    }

    @Override
    protected SupraCardPaymentState getDefaultPaymentState() {
        return SupraCardPaymentState.ENTER_CARD;
    }
}
