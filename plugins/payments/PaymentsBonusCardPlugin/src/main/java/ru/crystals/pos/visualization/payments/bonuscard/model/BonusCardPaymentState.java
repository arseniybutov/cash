package ru.crystals.pos.visualization.payments.bonuscard.model;

import ru.crystals.pos.visualization.payments.common.interfaces.PaymentState;


public enum BonusCardPaymentState implements PaymentState {
    PAYMENT,
    NOT_SET,
    ENTER_CARD_NUMBER,
    PARSE_INTERNAL_CARD,
    ERROR,
    CONFIRM_AMOUNT,
    CHOOSE_ACCOUNT;
}
