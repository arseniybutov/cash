package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

/**
 * Команда "Печать графики с масштабированием".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответ} от данной команды не имеет объектного представления (всегда будет <code>null</code>).
 * 
 * @author aperevozchikov
 */
public class PrintScaledGraphicsCommand extends BaseCommand<Object> {

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
     * Масштабирование точки по горизонтали (1 байт) «0» – нет
     */
    private int scaleX;
    
    /**
     * Масштабирование точки по вертикали (1 байт) «0» – нет
     */
    private int scaleY;
    
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
     * @param scaleX
     *            Масштабирование точки по горизонтали (1 байт) «0» – нет
     * @param scaleY
     *            Масштабирование точки по вертикали (1 байт) «0» – нет
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     * @throws IllegalArgumentException
     *             если <code>lastLineNo</code> или <code>lastLineNo</code> вне допустимого диапазона (1..200), либо, если <code>lastLineNo</code>
     *             больше чем <code>lastLineNo</code>
     */
    public PrintScaledGraphicsCommand(int firstLineNo, int lastLineNo, int scaleX, int scaleY, int password) {
        super(password);

        if (firstLineNo < 0 || firstLineNo > 200) {
            throw new IllegalArgumentException(String.format("PrintScaledGraphicsCommand: the \"firstLineNo\" (%s) argument is INVALID!", firstLineNo));
        }
        this.firstLineNo = firstLineNo;

        if (lastLineNo < 0 || lastLineNo > 200) {
            throw new IllegalArgumentException(String.format("PrintScaledGraphicsCommand: the \"lastLineNo\" (%s) argument is INVALID!", lastLineNo));
        }
        this.lastLineNo = lastLineNo;

        if (firstLineNo > lastLineNo) {
            throw new IllegalArgumentException(String.format("PrintScaledGraphicsCommand: the arguments are INCONSISTENT: " +
                "\"lastLineNo\" (%s) is LESS than \"firstLineNo\" (%s)!",
                lastLineNo, firstLineNo));
        }
        
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    @Override
    public String toString() {
        return String.format("print-scaled-graphics-cmd [first: %s; last: %s; scaleX: %s; scaleY: %s]", 
            firstLineNo, lastLineNo, scaleX, scaleY);
    }

    @Override
    public byte getCommandCode() {
        return (byte) 0x4F;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == 8 байт
        byte[] result = new byte[8];

        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);

        // Номер линии 1й линии
        result[4] = (byte) firstLineNo;

        // Номер линии последней линии
        result[5] = (byte) lastLineNo;
        
        // Масштабирование по вертикали
        result[6] = (byte) scaleY;
        
        // масштабирование по горизонтали
        result[7] = (byte) scaleX;

        return result;
    }

    @Override
    public Object decodeResponse(byte[] response) {
        return null;
    }
    
}