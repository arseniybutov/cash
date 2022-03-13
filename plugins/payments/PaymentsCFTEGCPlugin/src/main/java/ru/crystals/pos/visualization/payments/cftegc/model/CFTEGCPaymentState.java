package ru.crystals.pos.visualization.payments.cftegc.model;

import ru.crystals.pos.visualization.payments.common.interfaces.PaymentState;

public enum CFTEGCPaymentState implements PaymentState {
    PAYMENT,
    NOT_SET,
    REFUND,
    ENTER_CARD_NUMBER,
    ENTER_PIN_CODE;
}
