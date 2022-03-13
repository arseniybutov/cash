package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import java.util.Arrays;

import ru.crystals.pos.utils.PortAdapterUtils;

/**
 * Команда: чтение таблицы.
 * <p/>
 * {@link #decodeResponse(byte[]) Ответом} данной команды является содержимое запрашиваемого поля в "сыром" виде.
 * 
 * @author aperevozchikov
 */
public class ReadTableCommand extends BaseCommand<byte[]> {
    
    /**
     * Номер таблицы, из которой хотим считать данные
     */
    private byte tableNo;
    
    /**
     * Номер строки. из которой хотим считать данные
     */
    private int rowNo;
    
    /**
     * Номер поля, из которого хотим считать данные
     */
    private byte fieldNo;

    /**
     * Единственно правильный конструктор.
     * 
     * @param tableNo
     *            номер таблицы
     * @param rowNo
     *            номер строки
     * @param fieldNo
     *            номер поля из которого считываем данные
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     */
    public ReadTableCommand(byte tableNo, int rowNo, byte fieldNo, int password) {
        super(password);
        
        this.tableNo = tableNo;
        this.rowNo = rowNo;
        this.fieldNo = fieldNo;
    }
    
    @Override
    public String toString() {
        return String.format("read-table-cmd [tableNo: %s; rowNo: %s; fieldNo: %s; password: %s]", 
            tableNo, rowNo, fieldNo, PortAdapterUtils.arrayToString(password));
    }
    
    @Override
    public byte getCommandCode() {
        return 0x1F;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == 8 байт
        byte[] result = new byte[8];
        
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
        
        return result;
    }

    @Override
    public byte[] decodeResponse(byte[] response) {
        byte[] result;
        
        if (!validateResponse(response)) {
            return null;
        }
        
        // наш ответ: с 5го байта и до предпоследнего
        result = Arrays.copyOfRange(response, 4, response.length - 1);
        
        return result;
    }

    @Override
    public boolean validateResponse(byte[] response) {
        if (!super.validateResponse(response)) {
            return false;
        }
        
        // длина ответа должна быть МИНИМУМ 6 байт: 
        //  STX, байт длины, байт команды, код ошибки, данные (минимум 1 байт), LRC
        if (response.length < 6) {
            return false;
        }
        
        return true;
    }
    
    
    
}
