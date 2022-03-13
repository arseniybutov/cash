package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

/**
 * Начать открытие смены
 */
public class BeginOpenShiftCommand extends BaseCommand<String> {

    /**
     * Единственно правильный конструктор.
     *
     * @param password пароль оператора, от имени которого собираемся выполнить эту команду
     */
    public BeginOpenShiftCommand(int password) {
        super(password);
    }

    @Override
    public String toString() {
        return String.format("begin-open-shift-cmd");
    }

    @Override
    public byte getCommandPrefix() {
        return (byte) 0xFF;
    }

    @Override
    public byte getCommandCode() {
        return (byte) 0x41;
    }

    @Override
    public byte[] getArguments() {
        // 4 байта - пароль, 4 байта -заголовок TLV, данные
        byte[] result = new byte[4];

        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);
        return result;
    }

    @Override
    public String decodeResponse(byte[] response) {
        return null;
    }
}