package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;

/**
 * Команда: "Запрос операционного регистра".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответом} данной команды является содержимое операционного регистра (количество операций).
 * 
 * @author aperevozchikov
 */
public class GetOperationRegistryCommand extends BaseCommand<Integer> {
    /**
     * номер операционного регистра, содержимое которого надо вернуть
     */
    private byte registryNo;

    /**
     * Единственно правильный конструктор.
     * 
     * @param registryNo
     *            номер операционного регистра, содержимое которого надо вернуть
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     */
    public GetOperationRegistryCommand(byte registryNo, int password) {
        super(password);
        this.registryNo = registryNo;
    }

    @Override
    public String toString() {
        return String.format("get-oper-reg-cmd [regNo: %s]", registryNo);
    }

    @Override
    public byte getCommandCode() {
        return 0x1B;
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
    public Integer decodeResponse(byte[] response) {
        Integer result;

        if (!validateResponse(response)) {
            return null;
        }
        // ответ полностью валиден и соответсвует протоколу

        // ответ - в байтах с 6го по 7й - от мл. байта к старшему
        result = ShtrihUtils.getInt(response[5], response[6]);

        return result;
    }

    @Override
    public boolean validateResponse(byte[] response) {
        if (!super.validateResponse(response)) {
            return false;
        }

        // длина ответа должна быть 8 байт (вместе со служебными символами)
        if (response.length != 8) {
            return false;
        }

        return true;
    }

}
