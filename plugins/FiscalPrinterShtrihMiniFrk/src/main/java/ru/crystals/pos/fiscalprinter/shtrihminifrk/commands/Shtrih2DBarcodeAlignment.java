package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

/**
 * Перечисление возможных способов выравнивания 2D ШК при печати.
 * 
 * @author aperevozchikov
 */
public enum Shtrih2DBarcodeAlignment {
    LEFT((byte) 0),
    CENTER((byte) 1),
    RIGHT((byte) 2);

    /**
     * код этого типа выравнивания - в терминах протокола работы с ККТ
     */
    private byte code;

    /**
     * Единственно правильный конструктор
     * 
     * @param code
     *            код этого типа выравнивания - в терминах протокола работы с ККТ
     */
    private Shtrih2DBarcodeAlignment(byte code) {
        this.code = code;
    }

    /**
     * Вернет код (в терминах протокола работы с ККТ) этого типа выравниваняи
     * 
     * @return код
     */
    public byte getCode() {
        return code;
    }

}