package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import java.util.Arrays;

import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihDeviceType;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;

/**
 * Команда: "Получить тип устройства".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответом} данной команды является {@link GetDeviceTypeCommand описание типа ФР}.
 * 
 * @author aperevozchikov
 */
public class GetDeviceTypeCommand extends BaseCommand<ShtrihDeviceType> {
    /**
     * Единственно правильный конструктор.
     * 
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     */
    public GetDeviceTypeCommand(int password) {
        super(password);
    }

    @Override
    public String toString() {
        return String.format("get-device-type-cmd");
    }

    @Override
    public byte getCommandCode() {
        return (byte) 0xFC;
    }

    @Override
    public byte[] getArguments() {
        // у этой команды нету аргументов: видимо, для совместимости с более старыми версиями
        return null;
    }

    @Override
    public ShtrihDeviceType decodeResponse(byte[] response) {
        ShtrihDeviceType result;

        if (!validateResponse(response)) {
            return null;
        }
        // ответ полностью валиден и соответсвует протоколу

        result = new ShtrihDeviceType();
        
        // Тип устройства. 5й байт:
        result.setTypeId(ShtrihUtils.getInt(response[4], (byte) 0));
        
        // подтип. 6й байт
        result.setSubTypeId(ShtrihUtils.getInt(response[5], (byte) 0));
        
        // версия протокола. 7й
        result.setProtocolVersion(ShtrihUtils.getInt(response[6], (byte) 0));
        
        // подверсия протокола. 8й
        result.setProtocolSubVersion(ShtrihUtils.getInt(response[7], (byte) 0));
        
        // модель устройства. 9й
        result.setDeviceId(ShtrihUtils.getInt(response[8], (byte) 0));
        
        // язый устройства. 10й
        result.setLanguage(ShtrihUtils.getInt(response[9], (byte) 0));
        
        // название устройства: байты 11й -...
        if (response.length > 11) {
            String name = getString(Arrays.copyOfRange(response, 10, response.length - 1));
            result.setName(name);
        }
        
        return result;
    }

    @Override
    public boolean validateResponse(byte[] response) {
        if (!super.validateResponse(response)) {
            return false;
        }

        // длина ответа должна быть как минимум 11 символов: 3 служебных + 8 гарантированных байт данных
        if (response.length < 11) {
            return false;
        }

        return true;
    }
}