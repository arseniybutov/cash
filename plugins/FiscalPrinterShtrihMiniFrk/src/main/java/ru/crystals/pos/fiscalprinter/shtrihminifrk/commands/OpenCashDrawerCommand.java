package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

/**
 * Команда "Открыть денежный ящик".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответ} от данной команды не имеет объектного представления (всегда будет <code>null</code>).
 * 
 * @author aperevozchikov
 */
public class OpenCashDrawerCommand extends BaseCommand<Object> {
    /**
     * номер денежного ящика, что хотим открыть
     */
    private byte cashDrawerNumber;

    /**
     * Единственно правильный конструктор.
     *
     * @param cashDrawerNumber
     *            номер денежного ящика, что хотим открыть (0..1)
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     */
    public OpenCashDrawerCommand(byte cashDrawerNumber, int password) {
        super(password);
        this.cashDrawerNumber = cashDrawerNumber;
    }

    @Override
    public String toString() {
        return String.format("open-cash-drawer-cmd [drawerNo: %s]", cashDrawerNumber);
    }

    @Override
    public byte getCommandCode() {
        return (byte) 0x28;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == 5 байт
        byte[] result = new byte[5];

        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);

        // Номер денежного ящика (1 байт)
        result[4] = cashDrawerNumber;

        return result;
    }

    @Override
    public Object decodeResponse(byte[] response) {
        return null;
    }
}
