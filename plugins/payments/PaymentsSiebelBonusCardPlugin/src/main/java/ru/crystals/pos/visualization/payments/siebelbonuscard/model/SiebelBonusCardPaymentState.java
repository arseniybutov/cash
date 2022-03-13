package ru.crystals.pos.visualization.payments.siebelbonuscard.model;

import ru.crystals.pos.visualization.payments.common.interfaces.PaymentState;

public enum SiebelBonusCardPaymentState implements PaymentState {
    PAYMENT,
    NOT_SET,
    ENTER_CARD_NUMBER,
    PARSE_INTERNAL_CARD,
    ERROR;
}
