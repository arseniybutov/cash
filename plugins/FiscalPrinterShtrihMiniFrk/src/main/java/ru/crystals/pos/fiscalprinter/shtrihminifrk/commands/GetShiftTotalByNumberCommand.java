package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import java.util.Arrays;

/**
 * Команда: "Запрос в ЭКЛЗ итогов смены по номеру смены".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответом} данной команды является тип ККМ (строка символов в кодировке {@link BaseCommand#ENCODING}).
 * 
 * @author aperevozchikov
 */
public class GetShiftTotalByNumberCommand extends BaseCommand<String> {

    /**
     * Номер смены, итоги которой хотим получить
     */
    private int shiftNo;

    /**
     * Единственно правильный конструктор.
     * 
     * @param shiftNo
     *            Номер смены, итоги которой хотим получить
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     */
    public GetShiftTotalByNumberCommand(int shiftNo, int password) {
        super(password);
        this.shiftNo = shiftNo;
    }

    @Override
    public String toString() {
        return String.format("get-shift-total-by-number-cmd [shiftNo: %s]", shiftNo);
    }
    
    @Override
    public byte getCommandCode() {
        return (byte) 0xBA;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == 6 байт
        byte[] result = new byte[6];

        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);

        // Номер смены (2 байта) 0000…2100
        result[4] = (byte) shiftNo;
        result[5] = (byte) (shiftNo >>> 8);

        return result;
    }

    @Override
    public String decodeResponse(byte[] response) {
        String result;
        
        if (!validateResponse(response)) {
            return null;
        }
        
        // ответ - тип ККМ - в байтах с 5го по предпоследний:
        byte[] payload = Arrays.copyOfRange(response, 4, response.length - 1);
        result = getString(payload);
        
        return result;
    }
    
    @Override
    public boolean validateResponse(byte[] response) {
        if (!super.validateResponse(response)) {
            return false;
        }

        // длина ответа должна быть минимум 6 байт (вместе со служебными символами)
        if (response.length < 6) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public long getMaxResponseTime() {
        // время выполнения команды - до 40 сек.
        return 40_000L;
    }
    
}