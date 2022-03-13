package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;

/**
 * Команда: "Запрос денежного регистра".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответом} данной команды является содержимое денежного регистра (соответсвующая сумма, в МДЕ (в "копейках")).
 * 
 * @author aperevozchikov
 */
public class GetCashRegistryCommand extends BaseCommand<Long> {
    /**
     * номер денежного регистра, содержимое которого надо вернуть
     */
    private byte registryNo;

    /**
     * Единственно правильный конструктор.
     * 
     * @param registryNo
     *            номер денежного регистра, содержимое которого надо вернуть
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     */
    public GetCashRegistryCommand(byte registryNo, int password) {
        super(password);
        this.registryNo = registryNo;
    }

    @Override
    public String toString() {
        return String.format("get-cash-reg-cmd [regNo: %s]", registryNo);
    }

    @Override
    public byte getCommandCode() {
        return 0x1A;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == 5 байт
        byte[] result = new byte[5];

        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);

        // Номер таблицы
        result[4] = registryNo;

        return result;
    }

    @Override
    public Long decodeResponse(byte[] response) {
        Long result;

        if (!validateResponse(response)) {
            return null;
        }
        // ответ полностью валиден и соответсвует протоколу

        // ответ - в байтах с 6го по 11й - от мл. байта к старшему
        result = ShtrihUtils.getLong(new byte[] {response[10], response[9], response[8], response[7], response[6], response[5]});

        return result;
    }

    @Override
    public boolean validateResponse(byte[] response) {
        if (!super.validateResponse(response)) {
            return false;
        }

        // длина ответа должна быть 12 байт (вместе со служебными символами)
        if (response.length != 12) {
            return false;
        }

        return true;
    }
}
