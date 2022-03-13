package ru.crystals.pos.bank.commonsbpprovider.api.status;

public enum Status {

    /**
     * операция завершена успешно
     */
    SUCCESS,

    /**
     * операция отклонена
     */
    REJECTED,

    /**
     * операция в обработке
     */
    PROCESSING,

    /**
     * операция не начата/операции не существует
     */
    NOT_STARTED,

    /**
     * другой/неизвестный результат операции
     */
    UNKNOWN

}
