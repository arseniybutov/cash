package ru.crystals.pos.visualization.payments.bankqr.model;

import ru.crystals.pos.visualization.payments.common.interfaces.PaymentState;

/**
 * Состояния оплаты для PaymentModel
 */
public enum BankQRPaymentState implements PaymentState {
    /**
     * Состояние по умолчанию
     */
    NOT_SET,
    /**
     * Установлена сущность оплаты
     */
    PAYMENT,
    /**
     * Ожидание выполнения работы
     */
    SHOW_WAIT,
    /**
     * Банковская операция в процессе,
     * отличается от обычного ожидания наличием кнопки отмены
     */
    SHOW_ABORT_WAIT,
    /**
     * Отображение диалога для работы с банком
     */
    DIALOG,
    /**
     * Ошибка в банковской операции
     */
    ERROR,
    /**
     * Ожидание сканирования QR кода кассиром
     */
    SCAN_CUSTOMER_QR
}
