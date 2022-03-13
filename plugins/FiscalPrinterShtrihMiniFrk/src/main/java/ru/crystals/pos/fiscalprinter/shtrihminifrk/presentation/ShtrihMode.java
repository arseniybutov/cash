package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

/**
 * Описание режима, в котором находится ФР семейства "Штрих".
 * <p/>
 * Implementation note: this class is immutable for a reason.
 * 
 * @author aperevozchikov
 */
public class ShtrihMode {

    /**
     * Код под-состояния {@code ShtrihStateEnum#PRINTING_FISCAL_DOCUMENT печати подкладного документа}: Ожидание извлечения [подкладного документа из
     * принтера].
     */
    public static byte AWAITING_FOR_SLP_TO_BE_EXTRACTED = 6;    

    /**
     * Собственно само полное описание режима
     */
    private byte state;

    /**
     * Единственно правильный конструктор.
     * 
     * @param state
     *            полное описание режима
     */
    public ShtrihMode(byte state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return String.format("shtrih-mode [full-code: 0x%02X, state-num: %s, state: %s]", state, getStateNumber(), getStateOfState());
    }

    /**
     * Вернет номер режима
     * 
     * @return номер режима
     */
    public ShtrihModeEnum getStateNumber() {
        // номер режима определяетмя младшим полубайтом:
        return ShtrihModeEnum.getByCode((byte) (state & 0x0F));
    }

    /**
     * Вернет статус режима
     * 
     * @return число 0..15; хотя смысл (в зависимости от {@link #getStateNumber()} имеют только максимум 0..6)
     */
    public byte getStateOfState() {
        // статус режима определяется старшим полубайтом
        return (byte) (state >> 4 & 0x0F);
    }
}