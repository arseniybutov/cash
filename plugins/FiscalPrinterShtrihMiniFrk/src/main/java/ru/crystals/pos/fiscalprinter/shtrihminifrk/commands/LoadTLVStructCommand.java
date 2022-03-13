package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import java.util.Arrays;

/**
 * Команда: "Передачи телефона или email клиента".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответом} данной команды является строка или фрагмент "сыром" виде.
 * 
 * @author aperevozchikov
 */
public class LoadTLVStructCommand extends BaseCommand<String> {

    private String clientData;
    /**
     * Единственно правильный конструктор.
     *
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     */
    public LoadTLVStructCommand(String clientData, int password) {
        super(password);

        this.clientData = clientData;
    }

    @Override
    public String toString() {
        return String.format("load-client-data-cmd");
    }

    @Override
    public byte getCommandPrefix() {
        return (byte) 0xFF;
    }

    @Override
    public byte getCommandCode() {
        return (byte) 0x0C;
    }

    @Override
    public byte[] getArguments() {
        byte[] bytesClientData = clientData.getBytes();
        // 4 байта - пароль, 4 байта -заголовок TLV, данные
        byte[] result = new byte[4 + 4 + bytesClientData.length];

        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);
        result[4] = (byte) 0xF0;
        result[5] = (byte) 0x03;
        result[6] = (byte) bytesClientData.length;
        result[7] = (byte) 0x00;
        System.arraycopy(bytesClientData, 0, result, 8, bytesClientData.length);

        return result;
    }

    @Override
    public String decodeResponse(byte[] response) {
        String result;

        if (!validateResponse(response)) {
            return null;
        }
        // ответ полностью валиден и соответсвует протоколу
        
        // наш ответ - в байтах с 5го по препоследний
        result = getString(Arrays.copyOfRange(response, 5, response.length - 1));
        
        return result;
    }

    @Override
    public boolean validateResponse(byte[] response) {
        if (!super.validateResponse(response)) {
            return false;
        }

        // длина ответа должна быть как минимум 6 символов (как минимум 1 байт "полезной нагрузки" и 5 служебных байтов)
        if (response.length < 6) {
            return false;
        }

        return true;
    }
}