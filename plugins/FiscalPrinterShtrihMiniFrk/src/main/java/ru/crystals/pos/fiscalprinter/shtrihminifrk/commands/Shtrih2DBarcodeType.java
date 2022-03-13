package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

/**
 * Перечисление возможных типов 2D штрих-кодов.
 * 
 * @author aperevozchikov
 */
public enum Shtrih2DBarcodeType {
    PDF417((byte) 0),
    DATAMATRIX((byte) 1),
    AZTEC((byte) 2),
    QR((byte) 3);

    /**
     * код этого типа ШК - в терминах протокола работы с ККТ
     */
    private byte code;

    /**
     * Единственно правильный конструктор
     * 
     * @param code
     *            код этого типа ШК - в терминах протокола работы с ККТ
     */
    private Shtrih2DBarcodeType(byte code) {
        this.code = code;
    }

    /**
     * Вернет код (в терминах протокола работы с ККТ) этого типа ШК
     * 
     * @return код
     */
    public byte getCode() {
        return code;
    }
}
