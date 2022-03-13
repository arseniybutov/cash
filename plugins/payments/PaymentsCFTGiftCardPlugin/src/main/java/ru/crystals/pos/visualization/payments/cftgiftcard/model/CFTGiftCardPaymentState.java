package ru.crystals.pos.visualization.payments.cftgiftcard.model;

import ru.crystals.pos.visualization.payments.common.interfaces.PaymentState;

public enum CFTGiftCardPaymentState implements PaymentState {
    PAYMENT,
    NOT_SET,
    REFUND,
    ENTER_CARD_NUMBER;
}
