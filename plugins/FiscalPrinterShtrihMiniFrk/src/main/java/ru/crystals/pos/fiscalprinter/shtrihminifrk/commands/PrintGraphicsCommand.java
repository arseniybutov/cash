package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

/**
 * Команда "Печать графики".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответ} от данной команды не имеет объектного представления (всегда будет <code>null</code>).
 * 
 * @author aperevozchikov
 */
public class PrintGraphicsCommand extends BaseCommand<Object> {

    /**
     * Номер первой линии (из картинки ранее загруженной в буфер командами {@link LoadGraphicsCommand}), что собираемся вывести на печать.
     * <p/>
     * NOTE: нумерация с <code>1</code>
     */
    private int firstLineNo;

    /**
     * Номер последней линии (из картинки ранее загруженной в буфер командами {@link LoadGraphicsCommand}), что собираемся вывести на печать.
     * <p/>
     * NOTE: нумерация с <code>1</code>
     */
    private int lastLineNo;

    /**
     * Единственно правильный конструктор.
     *
     * @param firstLineNo
     *            Номер первой линии (из картинки ранее загруженной в буфер), что собираемся вывести на печать
     *            <p/>
     *            Диапазон допустимых значений: 1..200
     * @param lastLineNo
     *            Номер последней линии (из картинки ранее загруженной в буфер), что собираемся вывести на печать
     *            <p/>
     *            Диапазон допустимых значений: 1..200
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     * @throws IllegalArgumentException
     *             если <code>lastLineNo</code> или <code>lastLineNo</code> вне допустимого диапазона (1..200), либо, если <code>lastLineNo</code>
     *             больше чем <code>lastLineNo</code>
     */
    public PrintGraphicsCommand(int firstLineNo, int lastLineNo, int password) {
        super(password);

        if (firstLineNo < 1 || firstLineNo > 200) {
            throw new IllegalArgumentException(String.format("PrintGraphicsCommand: the \"firstLineNo\" (%s) argument is INVALID!", firstLineNo));
        }
        this.firstLineNo = firstLineNo;

        if (lastLineNo < 1 || lastLineNo > 200) {
            throw new IllegalArgumentException(String.format("PrintGraphicsCommand: the \"lastLineNo\" (%s) argument is INVALID!", lastLineNo));
        }
        this.lastLineNo = lastLineNo;
        
        if (firstLineNo > lastLineNo) {
            throw new IllegalArgumentException(String.format("PrintGraphicsCommand: the arguments are INCONSISTENT: " +
                "\"lastLineNo\" (%s) is LESS than \"firstLineNo\" (%s)!", lastLineNo, firstLineNo));
        }
    }

    @Override
    public String toString() {
        return String.format("print-graphics-cmd [first: %s; last: %s]", firstLineNo, lastLineNo);
    }

    @Override
    public byte getCommandCode() {
        return (byte) 0xC1;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == 6 байт
        byte[] result = new byte[6];

        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);

        // Номер линии 1й линии
        result[4] = (byte) firstLineNo;

        // Номер линии последней линии
        result[5] = (byte) lastLineNo;

        return result;
    }

    @Override
    public Object decodeResponse(byte[] response) {
        return null;
    }
}