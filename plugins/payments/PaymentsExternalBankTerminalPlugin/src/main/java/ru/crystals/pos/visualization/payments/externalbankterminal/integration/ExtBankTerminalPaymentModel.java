package ru.crystals.pos.visualization.payments.externalbankterminal.integration;

import ru.crystals.pos.visualization.payments.common.AbstractPaymentModel;

public class ExtBankTerminalPaymentModel extends AbstractPaymentModel<ExtBankTerminalPaymentState, ExtBankTerminalPaymentInfo> {
    @Override
    protected ExtBankTerminalPaymentInfo getDefaultPaymentInfo() {
        return new ExtBankTerminalPaymentInfo();
    }

    @Override
    protected ExtBankTerminalPaymentState getDefaultPaymentState() {
        return ExtBankTerminalPaymentState.NOT_SET;
    }
}
