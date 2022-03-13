package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import ru.crystals.pos.utils.PortAdapterUtils;

/**
 * Команда "Загрузка графики".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответ} от данной команды не имеет объектного представления (всегда будет <code>null</code>).
 * 
 * @author aperevozchikov
 */
public class LoadGraphicsCommand extends BaseCommand<Object> {

    /**
     * Номер линии, что собираемся загрузить в память
     * <p/>
     * NOTE: нумерация с <code>0</code>
     */
    private int lineNo;

    /**
     * графическая информация, что должна быть распечатана на этой линии. Каждый бит - это пиксель: <code>0</code> - это "белый" пиксель,
     * <code>1</code> - черный
     */
    private byte[] data;

    /**
     * Единственно правильный конструктор.
     *
     * @param lineNo
     *            Номер этой линии в картинке, что собираемся (в будущем) распечатать.
     *            <p/>
     *            Диапазон допустимых значений: 0..199
     * @param data
     *            графическая информация, что должна быть распечатана на этой линии. Каждый бит - это пиксель: <code>0</code> - это "белый" пиксель,
     *            <code>1</code> - черный
     *            <p/>
     *            Длина этого массива должна быть 40 байт.
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     * @throws IllegalArgumentException
     *             если <code>lineNo</code> менее <code>0</code>, либо более <code>199</code>, либо если размер <code>data</code> не 40 байт
     */
    public LoadGraphicsCommand(int lineNo, byte[] data, int password) {
        super(password);

        if (lineNo < 0 || lineNo > 200) {
            throw new IllegalArgumentException(String.format("LoadGraphicsCommand: the \"lineNo\" (%s) argument is INVALID!", lineNo));
        }
        this.lineNo = lineNo;

        if (data == null || data.length != 40) {
            throw new IllegalArgumentException(String.format("LoadGraphicsCommand: the \"data\" (length: %s) argument is INVALID!", data == null ? "(NULL)" : data.length));
        }
        this.data = data;
    }

    @Override
    public String toString() {
        return String.format("load-graphics-cmd [lineNo: %s; data: %s]", lineNo, PortAdapterUtils.arrayToString(data));
    }

    @Override
    public byte getCommandCode() {
        return (byte) 0xC0;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == 5 байт + сама графическая информация
        byte[] result = new byte[5 + data.length];

        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);

        // Номер линии
        result[4] = (byte) lineNo;

        // сама графическая информация
        System.arraycopy(data, 0, result, 5, data.length);

        return result;
    }

    @Override
    public Object decodeResponse(byte[] response) {
        return null;
    }
}
