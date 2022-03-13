package ru.crystals.pos.visualization.payments.consumercredit.model;

import ru.crystals.pos.visualization.payments.common.interfaces.PaymentState;

/**
 * Стейты работы плагина (экраны)
 */
public enum ConsumerCreditState implements PaymentState {
    NOT_SET,
    ENTER_SUM,
    CHOOSE_BANK,
    CHOOSE_BANK_PRODUCT,
    ENTER_FIO,
    ENTER_CONTRACT_NUMBER,
    CONFIRM_PAYMENT,
    ERROR
}
