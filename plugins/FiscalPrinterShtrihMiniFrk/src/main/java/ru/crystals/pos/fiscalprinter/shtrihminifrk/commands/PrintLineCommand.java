package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import ru.crystals.pos.utils.PortAdapterUtils;

/**
 * Команда "Печать линии".
 * <p>
 * {@link #decodeResponse(byte[]) Ответ} от данной команды не имеет объектного представления (всегда будет <code>null</code>).
 *
 * @author aperevozchikov
 */
public class PrintLineCommand extends BaseCommand<Object> {

    /**
     * Количество повторов (сколько раз подряд печатать эту линию)
     */
    private int times;

    /**
     * графическая информация, что должна быть распечатана на этой линии. Каждый бит - это пиксель: <code>0</code> - это "белый" пиксель,
     * <code>1</code> - черный
     */
    private byte[] data;

    /**
     * Надо ли переворачивать байты
     */
    private boolean needRevertBytes = false;

    /**
     * Единственно правильный конструктор.
     *
     * @param times             Количество повторов (сколько раз подряд печатать эту линию)
     * @param data              графическая информация, что должна быть распечатана на этой линии. Каждый бит - это пиксель: <code>0</code> - это "белый" пиксель,
     *                          <code>1</code> - черный
     * @param password          пароль оператора, от имени которого собираемся выполнить эту команду
     * @param needRevertBytes надо ли переворачивать байты
     * @throws IllegalArgumentException если <code>times</code> менее <code>1</code>, либо если <code>data</code> пуст
     */
    public PrintLineCommand(int times, byte[] data, int password, boolean needRevertBytes) {
        super(password);

        if (times < 1) {
            throw new IllegalArgumentException("PrintLineCommand: the \"times\" argument is non-positive!");
        }
        this.times = times;

        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("PrintLineCommand: the \"data\" argument is EMPTY!");
        }
        this.data = data;
        this.needRevertBytes = needRevertBytes;
    }

    @Override
    public String toString() {
        return String.format("print-line-cmd [times: %s; data: %s]", times, PortAdapterUtils.arrayToString(data));
    }


    @Override
    public byte getCommandCode() {
        return (byte) 0xC5;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == 6 байт + сама графическая информация
        byte[] result = new byte[6 + data.length];

        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);

        // Количество повторов
        result[4] = (byte) times; // мл
        result[5] = (byte) (times >>> 8); // ст

        if (needRevertBytes) {
            invertDataBytes();
        }
        // сама графическая информация
        System.arraycopy(data, 0, result, 6, data.length);

        return result;
    }

    /**
     * Инвертирует каждый байт данных побитово
     * <p>
     * например:
     * <br>
     * bin: 1100 0011 -> 1100 0011, 0101 0101 -> 1010 1010
     * <br>
     * hex: 0xC3 -> 0xC3, 0x55 -> 0xAA
     */
    private void invertDataBytes() {
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (((data[i] >> 4) & 15) | ((data[i] & 15) << 4));
            data[i] = (byte) (((data[i] >> 2) & 51) | ((data[i] & 51) << 2));
            data[i] = (byte) (((data[i] >> 1) & 85) | ((data[i] & 85) << 1));
        }
    }

    @Override
    public Object decodeResponse(byte[] response) {
        return null;
    }
}