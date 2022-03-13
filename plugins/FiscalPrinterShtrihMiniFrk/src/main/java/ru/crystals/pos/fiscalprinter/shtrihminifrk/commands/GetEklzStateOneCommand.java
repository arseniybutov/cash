package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihEklzStateOne;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;
import ru.crystals.pos.utils.PortAdapterUtils;


/**
 * Команда: "Запрос состояния по коду 1 ЭКЛЗ".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответом} данной команды является {@link ShtrihEklzStateOne состояние по коду 1 ЭКЛЗ}.
 * 
 * @author aperevozchikov
 *
 */
public class GetEklzStateOneCommand extends BaseCommand<ShtrihEklzStateOne> {
    
    /**
     * Единственно правильный конструктор.
     * 
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     */
    public GetEklzStateOneCommand(int password) {
        super(password);
    }

    @Override
    public String toString() {
        return String.format("get-eklz1-cmd [password: %s]", PortAdapterUtils.arrayToString(password));
    }

    @Override
    public byte getCommandCode() {
        return (byte) 0xAD;
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
    public ShtrihEklzStateOne decodeResponse(byte[] response) {
        ShtrihEklzStateOne result;

        if (!validateResponse(response)) {
            return null;
        }
        // ответ полностью валиден и соответсвует протоколу
        
        result = new ShtrihEklzStateOne();
        
        // Итог документа последнего КПК: байты с 5го (самый младший) по 9й (самый старший):
        result.setLastKpkTotal(ShtrihUtils.getLong(new byte[] {response[8], response[7], response[6], response[5], response[4]}));
        
        // Дата последнего КПК: байты: 10 (ДД), 11 (ММ), 12 (ГГ), 13 (ЧЧ), 14 (ММ - минуты):
        result.setLastKpkDate(ShtrihUtils.getDate(response[9], response[10], response[11], response[12], response[13], (byte) 0));
        
        // Номер последнего КПК: байты с 15го (мл) по 18й (ст):
        result.setLastKpk(ShtrihUtils.getLong(new byte[] {response[17], response[16], response[15], response[14]}));
        
        // Номер ЭКЛЗ: байты с 19го (самый младший) по 23й (самый старший):
        result.setEklzNum(ShtrihUtils.getLong(new byte[] {response[22], response[21], response[20], response[19], response[18]}));
        
        // Флаги ЭКЛЗ: 24й байт:
        result.setEklzFlags(response[23]);
        
        return result;
    }

    @Override
    public boolean validateResponse(byte[] response) {
        if (!super.validateResponse(response)) {
            return false;
        }

        // длина ответа должна быть 25 байт: 3 служебных байта и 22 байт "полезной нагрузки"
        if (response.length != 25) {
            return false;
        }

        return true;
    }
}