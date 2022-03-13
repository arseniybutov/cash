package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import java.nio.ByteBuffer;
import java.util.Arrays;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihFNStateOne;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihMode;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;

/**
 * Команда: "Запрос состояния ФН".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответом} данной команды является строка или фрагмент "сыром" виде.
 * 
 * @author aperevozchikov
 */
public class GetFNStateCommand extends BaseCommand<ShtrihFNStateOne> {
    /**
     * Единственно правильный конструктор.
     *
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     */
    public GetFNStateCommand(int password) {
        super(password);
    }

    @Override
    public String toString() {
        return String.format("get-fn-state-cmd");
    }

    @Override
    public byte getCommandPrefix() {
        return (byte) 0xFF;
    }

    @Override
    public byte getCommandCode() {
        return (byte) 0x01;
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
    public ShtrihFNStateOne decodeResponse(byte[] response) {
        ShtrihFNStateOne result = new ShtrihFNStateOne();

        if (!validateResponse(response)) {
            return null;
        }
        // ответ полностью валиден и соответсвует протоколу

        result.setShiftOpen(response[8] == (byte)0x01 ? true : false);
        result.setFnNum( getString(Arrays.copyOfRange(response, 15, 30)) );

        result.setLastFdNum(ShtrihUtils.getLong(new byte[]{response[34], response[33], response[32],response[31]}));
        
        return result;
    }

    @Override
    public boolean validateResponse(byte[] response) {
        if (!super.validateResponse(response)) {
            return false;
        }

        // длина ответа должна быть как минимум 6 символов (как минимум 1 байт "полезной нагрузки" и 5 служебных байтов)
        if (response.length < 31) {
            return false;
        }

        return true;
    }
}