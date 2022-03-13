package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import java.util.Arrays;

/**
 * Команда "Печать расширенной графики".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответ} от данной команды не имеет объектного представления (всегда будет <code>null</code>).
 *
 * @author aperevozchikov
 */
public class PrintExtGraphicsCommand extends BaseCommand<Object> {

    /**
     * Номер первой линии (из картинки ранее загруженной в буфер командами {@link LoadExtGraphicsCommand}), что собираемся вывести на печать.
     * <p/>
     * NOTE: нумерация с <code>1</code>
     */
    private int firstLineNo;

    /**
     * Номер последней линии (из картинки ранее загруженной в буфер командами {@link LoadExtGraphicsCommand}), что собираемся вывести на печать.
     * <p/>
     * NOTE: нумерация с <code>1</code>
     */
    private int lastLineNo;

    /**
     * Флаги печати
     * Параметр может отсутствовать в команде принтера, см. параметры модели
     */
    private Integer printFlag;

    /**
     * Конструктор с флагом печати на контрольной ленте.
     *
     * @param firstLineNo Номер первой линии (из картинки ранее загруженной в буфер), что собираемся вывести на печать
     *                    <p/>
     *                    Диапазон допустимых значений: 1..1200
     * @param lastLineNo  Номер последней линии (из картинки ранее загруженной в буфер), что собираемся вывести на печать
     *                    <p/>
     *                    Диапазон допустимых значений: 1..1200
     * @param password    пароль оператора, от имени которого собираемся выполнить эту команду
     * @throws IllegalArgumentException если <code>lastLineNo</code> или <code>lastLineNo</code> вне допустимого диапазона (1..1200), либо, если <code>lastLineNo</code>
     *                                  больше чем <code>lastLineNo</code>
     */
    public PrintExtGraphicsCommand(int firstLineNo, int lastLineNo, int password) {
        this(firstLineNo, lastLineNo, password, 0);
    }

    /**
     * Конструктор с указанием флагов печати
     *
     * @param printFlag Флаги печати:<br/>
     *                  Бит 0 – контрольная лента, Бит 1 – чековая лента, Бит 2 – подкладной документ, Бит 3 – слип чек; Бит 7 – отложенная печать графики<br/>
     *                  Для моделей не поддерживающих параметр передавать NULL
     * @throws IllegalArgumentException если <code>lastLineNo</code> или <code>lastLineNo</code> вне допустимого диапазона (1..1200), либо, если <code>lastLineNo</code>
     *                                  больше чем <code>lastLineNo</code>
     */
    public PrintExtGraphicsCommand(int firstLineNo, int lastLineNo, int password, Integer printFlag) {
        super(password);

        if (firstLineNo < 1 || firstLineNo > 1200) {
            throw new IllegalArgumentException(String.format("PrintExtGraphicsCommand: the \"firstLineNo\" (%s) argument is INVALID!", firstLineNo));
        }
        this.firstLineNo = firstLineNo;

        if (lastLineNo < 1 || lastLineNo > 1200) {
            throw new IllegalArgumentException(String.format("PrintExtGraphicsCommand: the \"lastLineNo\" (%s) argument is INVALID!", lastLineNo));
        }
        this.lastLineNo = lastLineNo;

        if (firstLineNo > lastLineNo) {
            throw new IllegalArgumentException(String.format("PrintExtGraphicsCommand: the arguments are INCONSISTENT: " +
                    "\"lastLineNo\" (%s) is LESS than \"firstLineNo\" (%s)!", lastLineNo, firstLineNo));
        }

        this.printFlag = printFlag;
    }

    @Override
    public String toString() {
        return String.format("print-ext-graphics-cmd [first: %s; last: %s]", firstLineNo, lastLineNo);
    }

    @Override
    public byte getCommandCode() {
        return (byte) 0xC3;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == 8 байт
        byte[] result = new byte[8];

        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);

        // Номер линии 1й линии
        result[4] = (byte) firstLineNo;
        result[5] = (byte) (firstLineNo >>> 8);

        // Номер линии последней линии
        result[6] = (byte) lastLineNo;
        result[7] = (byte) (lastLineNo >>> 8);

        // Флаги печати, если присутствуют, увеличивают длину на 1 байт
        if (printFlag != null) {
            result = Arrays.copyOf(result, 9);
            result[8] = printFlag.byteValue();
        }

        return result;
    }

    @Override
    public Object decodeResponse(byte[] response) {
        return null;
    }

}