package ru.crystals.pos.visualization.payments.kopilkabonuscard.model;

import ru.crystals.pos.visualization.payments.common.interfaces.PaymentState;

/**
 * Состояния при оплате бонусами Копилка
 */
public enum KopilkaBonusCardPaymentState implements PaymentState {
    /**
     * Оплата.
     */
    PAYMENT,
    /**
     * Начало, без состояния.
     */
    NOT_SET,
    /**
     * Ввод номера карты Копилка.
     */
    ENTER_CARD_NUMBER,
    /**
     * Разбор карты.
     */
    PARSE_CARD,
    /**
     * Ошибка.
     */
    ERROR;
}
