package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import java.util.Arrays;

/**
 * Команда: "Запрос итога активизации ЭКЛЗ".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответом} данной команды является тип ККМ (строка символов в кодировке {@link BaseCommand#ENCODING}).
 * 
 * @author aperevozchikov
 */
public class GetEklzActivationResult extends BaseCommand<String> {

    /**
     * Единственно правильный конструктор.
     * 
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     */
    public GetEklzActivationResult(int password) {
        super(password);
    }

    @Override
    public String toString() {
        return String.format("get-eklz-activation-cmd");
    }
    
    @Override
    public byte getCommandCode() {
        return (byte) 0xBB;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == 4 байт
        byte[] result = new byte[4];

        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);

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
}