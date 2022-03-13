package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import ru.crystals.pos.utils.PortAdapterUtils;

/**
 * Команда "Загрузка расширенной графики".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответ} от данной команды не имеет объектного представления (всегда будет <code>null</code>).
 * 
 * @author aperevozchikov
 */
public class LoadExtGraphicsCommand extends BaseCommand<Object> {

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
     *            Диапазон допустимых значений: 0..1199
     * @param data
     *            графическая информация, что должна быть распечатана на этой линии. Каждый бит - это пиксель: <code>0</code> - это "белый" пиксель,
     *            <code>1</code> - черный
     *            <p/>
     *            Длина этого массива должна быть 40 байт.
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     * @throws IllegalArgumentException
     *             если <code>lineNo</code> менее <code>0</code>, либо более <code>1199</code>, либо если размер <code>data</code> не 40 байт
     */
    public LoadExtGraphicsCommand(int lineNo, byte[] data, int password) {
        super(password);

        if (lineNo < 0 || lineNo > 1199) {
            throw new IllegalArgumentException(String.format("LoadExtGraphicsCommand: the \"lineNo\" (%s) argument is INVALID!", lineNo));
        }
        this.lineNo = lineNo;

        if (data == null || data.length != 40) {
            throw new IllegalArgumentException(String.format("LoadExtGraphicsCommand: the \"data\" (length: %s) argument is INVALID!", data == null ? "(NULL)" : data.length));
        }
        this.data = data;
    }

    @Override
    public String toString() {
        return String.format("load-ext-graphics-cmd [lineNo: %s; data: %s]", lineNo, PortAdapterUtils.arrayToString(data));
    }

    @Override
    public byte getCommandCode() {
        return (byte) 0xC4;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == 6 байт + сама графическая информация
        byte[] result = new byte[6 + data.length];

        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);

        // Номер линии
        result[4] = (byte) lineNo;
        result[5] = (byte) (lineNo >>> 8);

        // сама графическая информация
        System.arraycopy(data, 0, result, 6, data.length);

        return result;
    }

    @Override
    public Object decodeResponse(byte[] response) {
        return null;
    }
}