package ru.crystals.pos.visualization.payments.consumercredit.model;

import ru.crystals.pos.visualization.payments.common.AbstractPaymentModel;

/**
 * Created by myaichnikov on 19.11.2014.
 */
public class ConsumerCreditModel extends AbstractPaymentModel<ConsumerCreditState, ConsumerCreditInfo> {
    @Override
    protected ConsumerCreditInfo getDefaultPaymentInfo() {
        return new ConsumerCreditInfo();
    }

    @Override
    protected ConsumerCreditState getDefaultPaymentState() {
        return ConsumerCreditState.NOT_SET;
    }
}
