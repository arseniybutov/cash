package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import java.util.Arrays;

import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihRegNum;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;
import ru.crystals.pos.utils.PortAdapterUtils;

/**
 * Команда: "Запрос длинного заводского номера и длинного РНМ".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответом} данной команды является {@link ShtrihRegNum Заводской и регистрационный номера ФР}.
 * 
 * @author aperevozchikov
 */
public class GetRegNoCommand extends BaseCommand<ShtrihRegNum> {
    /**
     * Единственно правильный конструктор.
     * 
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     */
    public GetRegNoCommand(int password) {
        super(password);
    }

    @Override
    public String toString() {
        return String.format("get-reg-no-cmd [password: %s]", PortAdapterUtils.arrayToString(password));
    }

    @Override
    public byte getCommandCode() {
        return 0x0F;
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
    public ShtrihRegNum decodeResponse(byte[] response) {
        ShtrihRegNum result;

        if (!validateResponse(response)) {
            return null;
        }
        // ответ полностью валиден и соответсвует протоколу

        result = new ShtrihRegNum();

        // Заводской номер: байты с 5го по 11й:
        result.setDeviceNo(ShtrihUtils.getLong(ShtrihUtils.inverse(Arrays.copyOfRange(response, 4, 11))));
        
        // РНМ (регистрационный номер): байты с 12го по 18й:
        result.setRegNo(ShtrihUtils.getLong(ShtrihUtils.inverse(Arrays.copyOfRange(response, 11, 18))));

        return result;
    }

    @Override
    public boolean validateResponse(byte[] response) {
        if (!super.validateResponse(response)) {
            return false;
        }

        // длина ответа должна быть 19 байт: STX, len, CMD, ERR, 15 байтов "полезной нагрузки", LRC
        if (response.length != 19) {
            return false;
        }

        return true;
    }
}
