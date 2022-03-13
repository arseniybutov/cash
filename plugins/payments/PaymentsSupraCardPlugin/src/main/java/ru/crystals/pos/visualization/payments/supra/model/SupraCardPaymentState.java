package ru.crystals.pos.visualization.payments.supra.model;

import ru.crystals.pos.visualization.payments.common.interfaces.PaymentState;

/**
 * Created by s.pavlikhin on 08.06.2017.
 */
public enum SupraCardPaymentState implements PaymentState {
    NOT_SET,
    ENTER_CARD,
    ENTER_VERIFY,
    ENTER_AMOUNT,
    ERROR,
}
