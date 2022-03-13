package ru.crystals.pos.bank.sberbankqr.api.dto;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum OperationType {

    /**
     * Оплата (успешная)
     */
    PAY,

    /**
     * Возврат финансовой операции
     */
    REFUND,

    /**
     * Отмена заказа (как {@link #REFUND}, но деньги возвращаются сразу)
     */
    REVERSE,

    /**
     * Неизвестный статус
     */
    @JsonEnumDefaultValue
    UNKNOWN
}
