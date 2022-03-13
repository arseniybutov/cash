package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.*;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;
import ru.crystals.pos.utils.PortAdapterUtils;

import java.util.Arrays;

/**
 * Команда: "Запрос итогов последней фискализации (перерегистрации)".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответом} данной команды является данные регистрации ККT {@link ShtrihFiscalizationResult данные фискализации}.
 */
public class GetLastFiscalizationResult extends BaseCommand<ShtrihFiscalizationResult> {

    /**
     * Единственно правильный конструктор.
     *
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     */
    public GetLastFiscalizationResult(int password) {
        super(password);
    }

    @Override
    public String toString() {
        return String.format("get-fn-fiscalization-cmd[password: %s]", PortAdapterUtils.arrayToString(password));
    }

    @Override
    public byte getCommandPrefix() {
        return (byte) 0xFF;
    }

    @Override
    public byte getCommandCode() {
        return (byte) 0x09;
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
    public ShtrihFiscalizationResult decodeResponse(byte[] response) {
        ShtrihFiscalizationResult result;
        
        if (!validateResponse(response)) {
            return null;
        }

        result = new ShtrihFiscalizationResult();

        // Дата Фискализации: 5 байт DATE_TIME
        result.setFiscalizationDate(ShtrihUtils.getDate(response[7], response[6], response[5]));

        // ИНН: бйты от младшего к старшему с 9й по 21й:
        byte[] payload = Arrays.copyOfRange(response, 10, 22);
        String tin = getString(payload).trim();
        result.setTin(Long.parseLong(tin));

        // Регистрационный номер ККT записан в байтах с 21го по 41й
        payload = Arrays.copyOfRange(response, 22, 42);
        String regNum = getString(payload).trim();
        result.setRegNum(regNum);

        // Код налогообложения: 1 байт
        result.setTaxId(response[42]);

        // Режим работы: 1 байт
        result.setWorkMode(response[43]);
        
        return result;
    }
    
    @Override
    public boolean validateResponse(byte[] response) {
        if (!super.validateResponse(response)) {
            return false;
        }

        //длина ответа должна быть минимум 48 (+ 4 служебные) байт (53(+ 4) при РАСШИР. ОТВ. НА КОМАНДЫ ФОРМ. ФД)
        //Код ошибки : 1 байт
        //Дата и время: 5 байт DATE_TIME
        //ИНН : 12 байт ASCII
        //Регистрационный номер ККT: 20 байт ASCII
        //Код налогообложения: 1 байт
        //Режим работы: 1 байт
        //Номер ФД: 4 байта
        //Фискальный признак: 4 байта
        //Дата и время: 5 байт DATE_TIME при РАСШИР. ОТВ. НА КОМАНДЫ ФОРМ. ФД
        return response.length >= 52;
    }
}