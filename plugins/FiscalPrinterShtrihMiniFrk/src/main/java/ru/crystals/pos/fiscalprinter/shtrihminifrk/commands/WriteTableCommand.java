package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import ru.crystals.pos.utils.PortAdapterUtils;

/**
 * Команда: запись таблицы.
 * <p/>
 * {@link #decodeResponse(byte[]) Ответ} от данной команды не имеет объектного представления (всегда будет <code>null</code>).
 * 
 * @author aperevozchikov
 */
public class WriteTableCommand extends BaseCommand<Object> {

    /**
     * Номер таблицы, куда хотим записать
     */
    private byte tableNo;

    /**
     * Номер строки, куда хотим записать
     */
    private int rowNo;

    /**
     * Номер поля, куда хотим записать
     */
    private byte fieldNo;

    /**
     * Значение, что собираемся записать в таблицу
     */
    private byte[] value;

    /**
     * Единственно правильный конструктор.
     * 
     * @param tableNo
     *            номер таблицы
     * @param rowNo
     *            номер строки
     * @param fieldNo
     *            номер поля, куда собираемся записывать данные
     * @param value
     *            значение, что хотим записать в это поле
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     * @throws IllegalArgumentException
     *             если аргумент <code>value</code> невалиден (пуст, например)
     */
    public WriteTableCommand(byte tableNo, int rowNo, byte fieldNo, byte[] value, int password) {
        super(password);
        if (value == null || value.length == 0) {
            throw new IllegalArgumentException("WriteTableCommand: the \"value\" argument is invalid");
        }

        this.tableNo = tableNo;
        this.rowNo = rowNo;
        this.fieldNo = fieldNo;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("write-table-cmd [tableNo: %s; rowNo: %s; fieldNo: %s; password: %s; value: %s]", 
            tableNo, rowNo, fieldNo, PortAdapterUtils.arrayToString(password), PortAdapterUtils.arrayToString(value));
    }
    
    @Override
    public byte getCommandCode() {
        return 0x1E;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == (8 + value.length) байт
        byte[] result = new byte[8 + value.length];
        
        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);
        
        // Таблица (1 байт)
        result[4] = tableNo;
        
        // Ряд (2 байта)
        //  мл. байт
        result[5] = (byte) rowNo;
        //  ст. байт
        result[6] = (byte) (rowNo >>> 8);
        
        // Поле (1 байт)
        result[7] = fieldNo;
        
        // значение:
        System.arraycopy(value, 0, result, 8, value.length);
        
        return result;
    }

    @Override
    public Object decodeResponse(byte[] response) {
        return null;
    }
}
