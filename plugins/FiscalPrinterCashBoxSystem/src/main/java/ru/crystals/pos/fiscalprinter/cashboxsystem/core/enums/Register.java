package ru.crystals.pos.fiscalprinter.cashboxsystem.core.enums;

/**
 * Список счетчиков CBS
 */
public enum Register {

    /**
     * Количество наличных в денежном ящике. Тип данных "double"
     */
    REGISTER_CASH_SUM(1),
    /**
     * Сумма внесений наличных за текущую смену. Тип данных "double"
     */
    REGISTER_CASH_IN_SUM(2),
    /**
     * Сумма изъятия наличных за текущую смену. Тип данных "double"
     */
    REGISTER_CASH_OUT_SUM(3),
    /**
     * Количество внесений наличных за текущую смену. Тип данных "int32"
     */
    REGISTER_CASH_IN_COUNT(4),
    /**
     * Количество изъятий наличных за текущую смену. Тип данных "int32"
     */
    REGISTER_CASH_OUT_COUNT(5),
    /**
     * Сумма продаж за текущую смену. Тип данных "double"
     */
    REGISTER_SALE_SUM(6),
    /**
     * Количество продаж за текущую смену. Тип данных "int32"
     */
    REGISTER_SALE_COUNT(7),
    /**
     * Сумма возвратов продаж за текущую смену. Тип данных "double"
     */
    REGISTER_RETURN_SALE_SUM(8),
    /**
     * Количество возвратов продаж за текущую смену. Тип данных "int32"
     */
    REGISTER_RETURN_SALE_COUNT(9),
    /**
     * Сумма покупок за текущую смену. Тип данных "double"
     */
    REGISTER_BUY_SUM(10),
    /**
     * Количество покупок за текущую смену. Тип данных "int32"
     */
    REGISTER_BUY_COUNT(11),
    /**
     * Сумма возвратов покупок за текущую смену. Тип данных "double"
     */
    REGISTER_RETURN_BUY_SUM(12),
    /**
     * Количество возвратов покупок за текущую смену. Тип данных "int32"
     */
    REGISTER_RETURN_BUY_COUNT(13),
    /**
     * Флаг открытой смены. Тип данных "bool"
     */
    REGISTER_OPEN_SHIFT(14),
    /**
     * Сумма платежей наличными по продажам. Тип данных "double"
     */
    REGISTER_PAYMENT_SALE_CASH(15),
    /**
     * Сумма платежей дебитной картой по продажам. Тип данных "double"
     */
    REGISTER_PAYMENT_SALE_DEBIT(16),
    /**
     * Сумма платежей кредитом по продажам. Тип данных "double"
     */
    REGISTER_PAYMENT_SALE_CREDIT(17),
    /**
     * Сумма платежей тарой по продажам. Тип данных "double"
     */
    REGISTER_PAYMENT_SALE_TARE(18),
    /**
     * Сумма платежей наличными по возвратам продаж. Тип данных "double"
     */
    REGISTER_PAYMENT_SALE_RETURN_CASH(19),
    /**
     * Сумма платежей дебитной картой по возвратам продаж. Тип данных "double"
     */
    REGISTER_PAYMENT_SALE_RETURN_DEBIT(20),
    /**
     * Сумма платежей кредитом по возвратам продаж. Тип данных "double"
     */
    REGISTER_PAYMENT_SALE_RETURN_CREDIT(21),
    /**
     * Сумма платежей тарой по возвратам продаж. Тип данных "double"
     */
    REGISTER_PAYMENT_SALE_RETURN_TARE(22),
    /**
     * Сумма платежей наличными по покупкам. Тип данных "double"
     */
    REGISTER_PAYMENT_BUY_CASH(23),
    /**
     * Сумма платежей дебитной картой по покупкам. Тип данных "double"
     */
    REGISTER_PAYMENT_BUY_DEBIT(24),
    /**
     * Сумма платежей кредитом по покупкам. Тип данных "double"
     */
    REGISTER_PAYMENT_BUY_CREDIT(25),
    /**
     * Сумма платежей тарой по покупкам. Тип данных "double"
     */
    REGISTER_PAYMENT_BUY_TARE(26),
    /**
     * Сумма платежей наличными по возвратам покупок. Тип данных "double"
     */
    REGISTER_PAYMENT_BUY_RETURN_CASH(27),
    /**
     * Сумма платежей дебитной картой по возвратам покупок. Тип данных "double"
     */
    REGISTER_PAYMENT_BUY_RETURN_DEBIT(28),
    /**
     * Сумма платежей кредитом по возвратам покупок. Тип данных "double"
     */
    REGISTER_PAYMENT_BUY_RETURN_CREDIT(29),
    /**
     * Сумма платежей тарой по возвратам покупок. Тип данных "double"
     */
    REGISTER_PAYMENT_BUY_RETURN_TARE(30),
    /**
     * Выручка за текущую смену. Тип данных "double"
     */
    REGISTER_REVENUE(31),
    /**
     * Дата/время открытия смены в UTC0. Тип данных "ISO 8601, 2000-01-01T20:15:00Z"
     */
    REGISTER_DATE_TIME_OPEN_SHIFT(32),
    /**
     * Номер текущей смены. Тип данных "int32"
     */
    REGISTER_SHIFT_NUMBER(33),
    /**
     * Номер документа в пределах текущей смены. Тип данных "int32"
     */
    REGISTER_SHIFT_TICKET_DOCUMENT_NUMBER(34),
    /**
     * Оставшееся время работы в оффлайн режиме в секундах. Тип данных "int32"
     */
    REGISTER_ELAPSED_OFFLINE_TIME_SEC(35),
    /**
     * Дата/время начала оффлайн режима в UTC0. Тип данных "ISO 8601, 2000-01-01T20:15:00Z"
     */
    REGISTER_OFFLINE_TIME_BEGIN(36);


    private final int code;

    Register(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}