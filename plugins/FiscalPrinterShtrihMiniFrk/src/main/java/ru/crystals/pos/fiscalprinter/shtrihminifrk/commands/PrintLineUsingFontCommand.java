package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import org.apache.commons.lang.StringUtils;

import ru.crystals.pos.utils.PortAdapterUtils;

/**
 * Команда "Печать строки данным шрифтом".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответ} от данной команды не имеет объектного представления (всегда будет <code>null</code>).
 * 
 * @author aperevozchikov
 */
public class PrintLineUsingFontCommand extends BaseCommand<Object> {
    /**
     * Минимальная длина строки по протоколу. Если длина реальной строки менее 40 символов, то последние символы просто передаем 0x00.
     */
    private static final int MIN_LINE_LENGTH = 40;

    /**
     * Флаги, что определяют ленту, на которой будет текст печататься. Бит 0 – контрольная лента, Бит 1 – чековая лента
     */
    private byte destination;

    /**
     * Номер шрифта, которым печатать
     */
    private byte font;

    /**
     * Сам текст, что надо будет распечатать
     */
    private byte[] text;

    /**
     * Единственно правильный конструктор.
     * 
     * @param text
     *            текст, что надо распечатать; если <code>null</code>, то будет распечатана пустая строка
     * @param font
     *            номер шрифта, которым надо распечатать этот текст
     * @param printOnEjRibbon
     *            флаг-признак: надо ли этот тест печатать на контрольной ленте
     * @param printOnReceiptRibbon
     *            флаг-признак: надо ли этот тест печатать на чековой ленте
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     */
    public PrintLineUsingFontCommand(String text, byte font, boolean printOnEjRibbon, boolean printOnReceiptRibbon, int password) {
        super(password);
        this.text = StringUtils.isEmpty(text) ? new byte[] {SPACE} : getBytes(text);
        this.font = font;
        this.destination = 0;
        if (printOnEjRibbon) {
            this.destination |= 0b00000001;
        }
        if (printOnReceiptRibbon) {
            this.destination |= 0b00000010;
        }
    }

    @Override
    public String toString() {
        return String.format("print-using-font-cmd [text: \"%s\"; font: %s;]", PortAdapterUtils.arrayToString(text),
            PortAdapterUtils.toUnsignedByte(font));
    }

    @Override
    public byte getCommandCode() {
        return 0x2F;
    }

    @Override
    public byte[] getArguments() {
        // длина аргументов: пароль (4) + флаги (1) + №шрифта (1) + сам текст (НО НЕ МЕНЕЕ 40 байт!):
        byte[] result = new byte[(text.length < MIN_LINE_LENGTH ? MIN_LINE_LENGTH : text.length) + 6];

        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);

        // Флаги
        result[4] = destination;

        // номер шрифта
        result[5] = font;

        // сам текст
        System.arraycopy(text, 0, result, 6, text.length);

        return result;
    }

    @Override
    public Object decodeResponse(byte[] response) {
        return null;
    }
}
