package ru.crystals.pos.bank.tinkoffsbp.api.response;

import ru.crystals.pos.bank.commonsbpprovider.api.status.Status;

public enum ResponseStatus {

    /**
     * Денежные средства списаны
     */
    CONFIRMED(Status.SUCCESS),

    /**
     * Произведен частичный возврат денежных средств
     */
    PARTIAL_REFUNDED(Status.SUCCESS),

    /**
     * Произведен возврат денежных средств
     */
    REFUNDED(Status.SUCCESS),

    /**
     * Средства заблокированы, но не списаны
     */
    AUTHORIZED(Status.SUCCESS),

    /**
     * Происходит процесс возврата
     */
    ASYNC_REFUNDING(Status.SUCCESS),

    /**
     * Проверка платежных данных Покупателя
     */
    PREAUTHORIZING(Status.SUCCESS),

    /**
     * Платежная сессия была успешно отменена (до проведения процесса оплаты)
     */
    CANCELED(Status.SUCCESS),

    /**
     * Покупатель начал аутентификацию
     */
    AUTHORIZING(Status.PROCESSING),

    /**
     * Начало отмены блокировки средств
     */
    REVERSING(Status.PROCESSING),

    /**
     * Начало списания денежных средств
     */
    CONFIRMING(Status.PROCESSING),

    /**
     * Начало возврата денежных средств
     */
    REFUNDING(Status.PROCESSING),

    /**
     * Платеж зарегистрирован в шлюзе, но его обработка в
     * процессинге не начата
     */
    NEW(Status.NOT_STARTED),

    /**
     * Покупатель переправлен на страницу оплаты
     */
    FORM_SHOWED(Status.NOT_STARTED),

    /**
     * Платеж отклонен Банком
     */
    REJECTED(Status.REJECTED),

    /**
     * Статус не определен
     */
    UNKNOWN(Status.UNKNOWN);

    private final Status commonStatus;

    ResponseStatus(Status commonStatus) {
        this.commonStatus = commonStatus;
    }

    public Status getCommonStatus() {
        return commonStatus;
    }
}
