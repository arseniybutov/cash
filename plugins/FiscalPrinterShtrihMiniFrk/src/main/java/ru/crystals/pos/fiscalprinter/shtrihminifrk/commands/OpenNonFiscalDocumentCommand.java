package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

/**
 * Команда "Открыть нефискальный документ".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответ} от данной команды не имеет объектного представления (всегда будет <code>null</code>).
 * 
 * @author aperevozchikov
 */
public class OpenNonFiscalDocumentCommand extends BaseCommand<Object> {
    /**
     * Единственно правильный конструктор.
     *
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     */
    public OpenNonFiscalDocumentCommand(int password) {
        super(password);
    }

    @Override
    public String toString() {
        return String.format("open-non-fiscal-cmd");
    }

    @Override
    public byte getCommandCode() {
        return (byte) 0xE2;
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
