package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import java.util.Arrays;

/**
 * Команда: "Запрос номера ФН".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответом} данной команды является строка или фрагмент "сыром" виде.
 * 
 * @author aperevozchikov
 */
public class GetFNNumberCommand extends BaseCommand<String> {
    /**
     * Единственно правильный конструктор.
     *
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     */
    public GetFNNumberCommand(int password) {
        super(password);
    }

    @Override
    public String toString() {
        return String.format("get-fn-number-cmd");
    }

    @Override
    public byte getCommandPrefix() {
        return (byte) 0xFF;
    }

    @Override
    public byte getCommandCode() {
        return (byte) 0x02;
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