package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import java.util.Date;

import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;

/**
 * Команда "Программирование времени".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответ} от данной команды не имеет объектного представления (всегда будет <code>null</code>).
 * 
 * @author aperevozchikov
 */
public class SetTimeCommand extends BaseCommand<Object> {
    /**
     * Время (ТОЛЬКО ВРЕМЯ учитывается: дата (день) игнорируется), что собираемся записать/запрограммировать
     */
    private Date time;

    /**
     * Единственно правильный конструктор.
     *
     * @param time
     *            Время (ТОЛЬКО ВРЕМЯ учитывается: дата (день) игнорируется), что собираемся записать/запрограммировать
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     * @throws IllegalArgumentException
     *             если <code>date</code> == <code>null</code>
     */
    public SetTimeCommand(Date time, int password) {
        super(password);
        this.time = time;
    }

    @Override
    public String toString() {
        return String.format("set-time-cmd [date: %tT]", time);
    }

    @Override
    public byte getCommandCode() {
        return 0x21;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == 7 байт
        byte[] result = new byte[7];

        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);

        // Дата (3 байта) ДД-ММ-ГГ
        byte[] fullDate = ShtrihUtils.getDateTime(time);
        System.arraycopy(fullDate, 3, result, 4, 3);

        return result;
    }

    @Override
    public Object decodeResponse(byte[] response) {
        return null;
    }
}
