package ru.crystals.pos.visualization.payments.bankcard.model;

import ru.crystals.pos.visualization.payments.common.interfaces.PaymentState;

/**
 * Created by agaydenger on 17.11.16.
 */
public enum BankCardPaymentState implements PaymentState {
    NOT_SET,
    PAYMENT,
    SHOW_WAIT,
    DIALOG,
    ERROR
}
