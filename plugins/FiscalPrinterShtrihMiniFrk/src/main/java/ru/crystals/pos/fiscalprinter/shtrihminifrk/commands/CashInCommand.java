package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;
import ru.crystals.pos.utils.PortAdapterUtils;

/**
 * Команда "Внесение".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответом} данной команды будет СПНД (Сквозной порядковый номер документа) зарегистрированного документа [внесения].
 * 
 * @author aperevozchikov
 */
public class CashInCommand extends BaseCommand<Integer> {

    /**
     * Сумма внесения, в МДЕ (минимальных денежных единицах) - в "копейках".
     */
    private long sum;
    
    /**
     * Единственно правильный конструктор.
     * 
     * @param sum сумма внесения, в МДЕ
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     */
    public CashInCommand(long sum, int password) {
        super(password);
        this.sum = sum;
    }    

    @Override
    public String toString() {
        return String.format("cash-in-cmd [sum: %s; password: %s]", sum, PortAdapterUtils.arrayToString(password));
    }
    
    @Override
    public byte getCommandCode() {
        return 0x50;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == 9 байт
        byte[] result = new byte[9];
        
        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);
        
        // Сумма внесения (5 байт)
        byte[] cashInSum = ShtrihUtils.getLongAsByteArray(sum);
        cashInSum = ShtrihUtils.inverse(cashInSum);
        System.arraycopy(cashInSum, 0, result, 4, 5);
        
        return result;
    }

    @Override
    public Integer decodeResponse(byte[] response) {
        int result;
        
        if (!validateResponse(response)) {
            return null;
        }
        
        // СПНД записан в 6м (мл) и 7й (ст) байтах:
        result = ShtrihUtils.getInt(response[5], response[6]);
        
        return result;
    }
    
    @Override
    public boolean validateResponse(byte[] response) {
        if (!super.validateResponse(response)) {
            return false;
        }
        
        // длина ответа должна быть 8 байт: 
        //  STX, байт длины, байт команды, код ошибки, номер оператора, СПНД (2 байта), LRC
        if (response.length != 8) {
            return false;
        }
        
        return true;
    }
}