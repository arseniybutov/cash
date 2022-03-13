package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihFontProperties;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;

/**
 * Команда: "Прочитать параметры шрифта".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответом} данной команды является {@link ShtrihFontProperties описание свойств} запрошенного шрифта.
 * 
 * @author aperevozchikov
 */
public class GetFontPropertiesCommand extends BaseCommand<ShtrihFontProperties> {
    /**
     * Номер шрифта, свойства которого хотим получить
     */
    private int fontNo;

    /**
     * Единственно правильный конструктор.
     * 
     * @param fontNo
     *            Номер шрифта, свойства которого хотим получить
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     */
    public GetFontPropertiesCommand(byte fontNo, int password) {
        super(password);
        this.fontNo = 0;
        this.fontNo |= fontNo;
    }

    @Override
    public String toString() {
        return String.format("get-font-props-cmd [font-no: %s]", fontNo);
    }

    @Override
    public byte getCommandCode() {
        return 0x26;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == 5 байт
        byte[] result = new byte[5];

        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);

        // Номер шрифта (1 байт)
        result[4] = (byte) fontNo;

        return result;
    }

    @Override
    public ShtrihFontProperties decodeResponse(byte[] response) {
        ShtrihFontProperties result = null;

        if (!validateResponse(response)) {
            return null;
        }
        // ответ полностью валиден и соответсвует протоколу

        result = new ShtrihFontProperties();
        result.setFontNumber(fontNo);

        // Ширина области печати в точках (2 байта): 5й (мл) и 6й (ст) байты:
        int paWidth = ShtrihUtils.getInt(response[4], response[5]);
        result.setPrintableAreaWidth(paWidth);

        // Ширина символа с учетом межсимвольного интервала в точках (1 байт): 7й байт:
        int width = ShtrihUtils.getInt(response[6], (byte) 0);
        result.setSymbolWidth(width);

        // Высота символа с учетом межстрочного интервала в точках (1 байт): 8й байт:
        int height = ShtrihUtils.getInt(response[7], (byte) 0);
        result.setSymbolHeight(height);

        // Количество шрифтов в ФР (1 байт): 9й байт
        int count = ShtrihUtils.getInt(response[8], (byte) 0);
        result.setFontsCount(count);

        return result;
    }

    @Override
    public boolean validateResponse(byte[] response) {
        if (!super.validateResponse(response)) {
            return false;
        }

        // длина ответа должна быть 10 байт (вместе со служебными символами)
        if (response.length != 10) {
            return false;
        }

        return true;
    }

}
