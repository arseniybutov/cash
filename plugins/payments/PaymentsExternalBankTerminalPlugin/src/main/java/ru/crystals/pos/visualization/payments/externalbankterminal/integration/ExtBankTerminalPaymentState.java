package ru.crystals.pos.visualization.payments.externalbankterminal.integration;

import ru.crystals.pos.visualization.payments.common.interfaces.PaymentState;

public enum ExtBankTerminalPaymentState implements PaymentState {
    ENTER_SUM,
    NOT_SET,
    REFUND,
    ENTER_LAST_4_DIGITS,
    ENTER_AUTH_CODE,
    ENTER_RECEIPT_NUMBER;
}
