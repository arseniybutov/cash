package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

/**
 * Команда "Отрезка чека".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответ} от данной команды не имеет объектного представления (всегда будет <code>null</code>).
 * 
 * @author aperevozchikov
 */
public class CutCommand extends BaseCommand<Object> {

    /**
     * флаг-признак: делать полную отрезку чека (<code>true</code>) или частичную (<code>false</code>)
     */
    private boolean cutOff;

    /**
     * Единственно правильный конструктор.
     *
     * @param cutOff
     *            флаг-признак: делать полную отрезку чека (<code>true</code>) или частичную (<code>false</code>)
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     */
    public CutCommand(boolean cutOff, int password) {
        super(password);
        this.cutOff = cutOff;
    }

    @Override
    public String toString() {
        return String.format("cut-cmd [cut-off: %s]", cutOff);
    }

    @Override
    public byte getCommandCode() {
        return (byte) 0x25;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == 5 байт
        byte[] result = new byte[5];

        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);

        result[4] = cutOff ? (byte) 0 : (byte) 1;

        return result;
    }

    @Override
    public Object decodeResponse(byte[] response) {
        return null;
    }

}
