package ru.crystals.pos.visualization.payments.siebelgiftcard.model;

import ru.crystals.pos.visualization.payments.common.interfaces.PaymentState;

public enum SiebelGiftCardPaymentState implements PaymentState {
    NOT_SET,
    ENTER_CARD_NUMBER,
    CASHIER_MESSAGE,
    PAYMENT,
    VERIFICATION;
}
