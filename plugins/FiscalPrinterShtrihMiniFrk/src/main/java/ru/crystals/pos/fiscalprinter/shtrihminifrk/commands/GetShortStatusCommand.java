package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihFlags;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihMode;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihShortStateDescription;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihSubState;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;
import ru.crystals.pos.utils.PortAdapterUtils;

/**
 * Команда: "Короткий запрос состояния ФР".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответом} данной команды является {@link ShtrihShortStateDescription краткое состояние ФР}.
 * 
 * @author aperevozchikov
 */
public class GetShortStatusCommand extends BaseCommand<ShtrihShortStateDescription> {
    /**
     * Единственно правильный конструктор.
     * 
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     */
    public GetShortStatusCommand(int password) {
        super(password);
    }

    @Override
    public String toString() {
        return String.format("get-short-status-cmd [password: %s]", PortAdapterUtils.arrayToString(password));
    }

    @Override
    public byte getCommandCode() {
        return 0x10;
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
    public ShtrihShortStateDescription decodeResponse(byte[] response) {
        ShtrihShortStateDescription result;

        if (!validateResponse(response)) {
            return null;
        }
        // ответ полностью валиден и соответсвует протоколу

        result = new ShtrihShortStateDescription();
        
        // Флаги ФР (6й (мл. байт) и 7й (ст. байт) байты в ответе):
        result.setFlags(new ShtrihFlags((short) ShtrihUtils.getInt(response[5], response[6])));
        
        // режим ФР (8й байт):
        result.setState(new ShtrihMode(response[7]));
        
        // подрежим ФР (9й байт):
        ShtrihSubState subState = ShtrihSubState.getByCode(response[8]);
        result.setSubState(subState);
        
        // Количество операций в чеке (10й байт - младший, 15й байт - старший):
        result.setOperationsCount(ShtrihUtils.getInt(response[9], response[14]));
        
        // напряжение резервной батареи (11й байт):
        result.setUpsSupplyVoltage(ShtrihUtils.getInt(response[10], (byte) 0));
        
        // напряжение источника питания (12й байт):
        result.setMainSupplyVoltage(ShtrihUtils.getInt(response[11], (byte) 0));
        
        // код ошибки ФП (13й байт):
        result.setFiscalBoardErrorCode(response[12]);
        
        // код ошибки ЕКЛЗ (14й байт):
        result.setEklzErrorCode(response[13]);

        return result;
    }

    @Override
    public boolean validateResponse(byte[] response) {
        if (!super.validateResponse(response)) {
            return false;
        }

        // длина ответа должна быть 19 байт: 3 служебных байта и 16 байтов "полезной нагрузки"
        if (response.length != 19) {
            return false;
        }

        // 9й байт должен быть в диапазоне 0..5:
        ShtrihSubState subState = ShtrihSubState.getByCode(response[8]);
        if (subState == null) {
            // ответ все-таки не валиден!
            return false;
        }

        return true;
    }
}