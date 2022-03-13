package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums;

/**
 * Типы платежей.
 * <li>{@link #CASH} - Наличные </li>
 * <li>{@link #CARD} - Карта</li>
 * <li>{@link #CREDIT} - Кредит</li>
 * <li>{@link #TARE} - Тара</li>
 */
public enum PaymentType {

    /**
     * Наличные.
     */
    CASH,

    /**
     * Карта.
     */
    CARD,

    /**
     * Кредит.
     */
    CREDIT,

    /**
     * Тара.
     */
    TARE,

}
