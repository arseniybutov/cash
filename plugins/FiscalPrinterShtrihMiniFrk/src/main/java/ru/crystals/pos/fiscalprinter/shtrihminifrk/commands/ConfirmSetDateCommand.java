package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import java.util.Date;

import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;

/**
 * Команда "Подтверждение программирования даты".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответ} от данной команды не имеет объектного представления (всегда будет <code>null</code>).
 * 
 * @author aperevozchikov
 */
public class ConfirmSetDateCommand extends BaseCommand<Object> {
    /**
     * Дата (день), что программирование которой подтверждаем
     */
    private Date date;

    /**
     * Единственно правильный конструктор.
     *
     * @param date
     *            Дата (день), что программирование которой подтверждаем
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     * @throws IllegalArgumentException
     *             если <code>date</code> == <code>null</code>
     */
    public ConfirmSetDateCommand(Date date, int password) {
        super(password);
        this.date = date;
    }

    @Override
    public String toString() {
        return String.format("confirm-set-date-cmd [date: %tF]", date);
    }

    @Override
    public byte getCommandCode() {
        return 0x23;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == 7 байт
        byte[] result = new byte[7];

        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);

        // Дата (3 байта) ДД-ММ-ГГ
        byte[] fullDate = ShtrihUtils.getDateTime(date);
        System.arraycopy(fullDate, 0, result, 4, 3);

        return result;
    }

    @Override
    public Object decodeResponse(byte[] response) {
        return null;
    }
}
