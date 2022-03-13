package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums;

/**
 * Тип документа.
 * <li>{@link #SELL} - Продажа </li>
 * <li>{@link #BUY} - Покупка</li>
 * <li>{@link #SELL_RETURN} - Возврат продажи</li>
 * <li>{@link #BUY_RETURN} - Возврат покупки</li>
 */
public enum TradeOperationType {

    /**
     * Продажа.
     */
    SELL,

    /**
     * Покупка.
     */
    BUY,

    /**
     * Возврат продажи.
     */
    SELL_RETURN,

    /**
     * Возврат покупки.
     **/
    BUY_RETURN
}
