package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

/**
 * Флаги ФП (фискальной платы) ФР (фискального регистратора) семейства "Штрих".
 * 
 * @author aperevozchikov
 */
public class ShtrihBoardFlags {

    /**
     * Побитовое представление флагов
     */
    private byte flags;

    /**
     * Единственно правильный конструктор
     * 
     * @param flags
     *            побитовое представление флагов ФП
     */
    public ShtrihBoardFlags(byte flags) {
        this.flags = flags;
    }

    @Override
    public String toString() {
        return String.format("shtrih-fiscal-board-flags [0x%02X]", flags);
    }

    // getters

    /**
     * Наличие ФП1
     */
    public boolean hasFiscalBoardOne() {
        return (flags & 0b00000001) != 0;
    }

    /**
     * Наличие ФП2
     */
    public boolean hasFiscalBoardTwo() {
        return (flags & 0b00000010) != 0;
    }

    /**
     * Введена ли лицензия
     */
    public boolean isLicenseActivated() {
        return (flags & 0b00000100) != 0;
    }

    /**
     * Переполнение ФП: <code>true</code> - означает, что переполнение есть
     */
    public boolean isOverflown() {
        return (flags & 0b00001000) != 0;
    }

    /**
     * батарея ФП: вернет <code>true</code>, если заряд батареи ниже 80%.
     */
    public boolean isBatteryChargeLow() {
        return (flags & 0b00010000) != 0;
    }

    /**
     * Флаг корректности последней записи в ФП: если <code>false</code> - значит последняя запись испорчена
     */
    public boolean isTheLastRecordCorrect() {
        return (flags & 0b00100000) != 0;
    }

    /**
     * Флаг "открытости" смены в ФП: если <code>true</code> - значит, смена открыта
     */
    public boolean isShiftOpen() {
        return (flags & 0b01000000) != 0;
    }

    /**
     * Флаг "открытости" смены в ФП более 24 часов: если <code>true</code> - значит, смена открыта уже более 24 часов
     */
    public boolean isShiftOpenForMoreThanADay() {
        return (flags & 0b10000000) != 0;
    }
}