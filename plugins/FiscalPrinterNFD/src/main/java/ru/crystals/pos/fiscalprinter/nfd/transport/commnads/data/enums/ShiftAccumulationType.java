package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums;

public enum ShiftAccumulationType {
    /**
     * Накопления денежной наличности в смене.
     */
    CASH_SUM,

    /**
     * Накопления по служебным операциям в смене.
     */
    MONEY_PLACEMENT,

    /**
     * Выручка.
     */
    REVENUE,

    /**
     * Накопления по модификаторам в смене.
     */
    MODIFIER,

    /**
     * Не обнуляемые накопления по торговым операциям в смене.
     */
    NON_NULLABLE,

    /**
     * Накопления по типам оплат в смене.
     */
    PAYMENT,

    /**
     * Накопления по секциям в смене.
     */
    SECTION,

    /**
     * Итоговые накопления по секциям в смене.
     */
    SECTION_TOTAL,

    /**
     * Накопления по налогам в смене.
     */
    TAX,

    /**
     * Итоговые накопления по торговым операциям.
     */
    TOTAL

}
