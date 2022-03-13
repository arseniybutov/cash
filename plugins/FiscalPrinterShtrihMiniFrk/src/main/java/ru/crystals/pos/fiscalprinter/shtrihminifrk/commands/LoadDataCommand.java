package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import ru.crystals.pos.utils.PortAdapterUtils;

/**
 * Команда "Загрузка данных".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответ} от данной команды не имеет объектного представления (всегда будет <code>null</code>).
 * 
 * @author aperevozchikov
 */
public class LoadDataCommand extends BaseCommand<Object> {

    /**
     * Тип загружаемых данных; <code>0</code> - данные для двумерного штрих-кода
     */
    private byte dataType;

    /**
     * Порядковый номер блока данных
     */
    private int dataChunkNo;

    /**
     * сами данные (64 байта)
     */
    private byte[] data;

    /**
     * Единственно правильный конструктор.
     *
     * @param dataType
     *            Тип загружаемых данных; <code>0</code> - данные для двумерного штрих-кода
     * @param dataChunkNo
     *            Порядковый номер блока данных
     * @param data
     *            сами данные (64 байта)
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     * @throws IllegalArgumentException
     *             если размер <code>data</code> не равен 64 байта
     */
    public LoadDataCommand(byte dataType, int dataChunkNo, byte[] data, int password) {
        super(password);

        if (data == null || data.length != 64) {
            throw new IllegalArgumentException(String.format("LoadDataCommand: the \"data\" argument is INVALID (length: %s)", 
                data == null ? "(NULL)" : data.length));
        }

        this.dataType = dataType;
        this.dataChunkNo = dataChunkNo;
        this.data = data;
    }

    @Override
    public String toString() {
        return String.format("load-data-cmd [type: %s; chunk-no: %s; data: %s]", dataType, dataChunkNo, PortAdapterUtils.arrayToString(data));
    }

    @Override
    public byte getCommandCode() {
        return (byte) 0xDD;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == 6 байт + сами данные
        byte[] result = new byte[6 + data.length];

        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);

        // Тип данных (1 байт)
        result[4] = dataType;

        // Порядковый номер блока данных (1 байт)
        result[5] = (byte) dataChunkNo;

        // сами данные
        System.arraycopy(data, 0, result, 6, data.length);

        return result;
    }

    @Override
    public Object decodeResponse(byte[] response) {
        return null;
    }
}