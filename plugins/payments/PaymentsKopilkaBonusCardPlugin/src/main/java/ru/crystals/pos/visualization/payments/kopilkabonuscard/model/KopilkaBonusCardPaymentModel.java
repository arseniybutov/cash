package ru.crystals.pos.visualization.payments.kopilkabonuscard.model;

import ru.crystals.pos.visualization.payments.common.AbstractPaymentModel;

public class KopilkaBonusCardPaymentModel extends AbstractPaymentModel<KopilkaBonusCardPaymentState, KopilkaBonusCardPaymentInfo> {

    @Override
    protected KopilkaBonusCardPaymentInfo getDefaultPaymentInfo() {
        return new KopilkaBonusCardPaymentInfo();
    }

    @Override
    protected KopilkaBonusCardPaymentState getDefaultPaymentState() {
        return KopilkaBonusCardPaymentState.NOT_SET;
    }
}
