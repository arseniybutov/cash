package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

/**
 * Команда: "Отменить документ ФН".
 */
public class CancelDocument extends BaseCommand<Object> {
    /**
     * Единственно правильный конструктор.
     *
     * @param password пароль оператора, от имени которого собираемся выполнить эту команду
     */
    public CancelDocument(int password) {
        super(password);
    }

    @Override
    public String toString() {
        return String.format("fn-cancel-document-cmd");
    }

    @Override
    public byte getCommandPrefix() {
        return (byte) 0xFF;
    }

    @Override
    public byte getCommandCode() {
        return (byte) 0x08;
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
    public Object decodeResponse(byte[] response) {
        return null;
    }
}