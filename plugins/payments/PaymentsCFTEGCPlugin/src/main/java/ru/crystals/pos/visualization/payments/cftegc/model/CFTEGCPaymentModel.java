package ru.crystals.pos.visualization.payments.cftegc.model;

import ru.crystals.pos.visualization.payments.common.AbstractPaymentModel;

public class CFTEGCPaymentModel extends AbstractPaymentModel<CFTEGCPaymentState, CFTEGCPaymentInfo> {

    @Override
    protected CFTEGCPaymentInfo getDefaultPaymentInfo() {
        return new CFTEGCPaymentInfo();
    }

    @Override
    protected CFTEGCPaymentState getDefaultPaymentState() {
        return CFTEGCPaymentState.NOT_SET;
    }
}
