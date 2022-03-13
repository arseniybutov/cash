package ru.crystals.pos.customerdisplay.fec;

import ru.crystals.pos.customerdisplay.exception.CustomerDisplayPluginException;
import ru.crystals.pos.customerdisplay.plugin.TextCustomerDisplayPluginAbstract;
import ru.crystals.pos.customerdisplay.templates.TemplateProcessor;

/**
 * Created by kdanilov on 26.10.17.
 * <p>
 * Плагин "Экран покупателя(LCM)" для touch-кассы FEC.
 * <p>
 * У данного дисплея русские шрифты имеют ряд недостатков:
 * 1. некоторые буквы из нижнего регистра являются очень большими;
 * 2. буква "Д" верхнего регистра больше, чем другие буквы из верхнего регистра.
 * 3. буква "д" нижнего регистра по размеру такая же как другие буквы из верхнего регистра.
 * <p>
 * Поэтому в данном плагине весь текст приводится к верхнему регистру, а
 * буква "Д" верхнего регистра приводится к нижнему регистру.
 */
public class CustomerDisplayPluginImpl extends TextCustomerDisplayPluginAbstract {

    private static final int CLEAR = 0x0C;
    private static final int ESC = 0x1B;

    public CustomerDisplayPluginImpl() {
        templateProcessor = new TemplateProcessor("fec-templates.xml");
    }

    @Override
    protected void displayTextAt(int row, int column, String text) throws CustomerDisplayPluginException {
        if (column < 0 || column > 17 || row < 0 || row > 1 || text == null) {
            throw new CustomerDisplayPluginException("Incorrect input parameters.");
        }

        byte[] hex = {ESC, 0x6C, (byte) (column + 1), (byte) (row + 1)};
        executeCommand(new String(hex) + text.toUpperCase().replace("Д", "д"));
    }

    @Override
    protected int getMaxCharPerLine() {
        return getConfig().getColumnsCount();
    }

    @Override
    public void clearText() throws CustomerDisplayPluginException {
        executeCommand(new byte[]{CLEAR});
    }

    @Override
    public void verifyDevice() throws CustomerDisplayPluginException {
        // not used
    }

    @Override
    public void configureDisplay() throws CustomerDisplayPluginException {
        executeCommand(new byte[]{ESC, 0x74, 0x09}); // code table = russian
        executeCommand(new byte[]{ESC, 0x52, 0x0C}); // character set = russian
        clearText();
    }

    private void executeCommand(byte[] hex) throws CustomerDisplayPluginException {
        executeCommand(new String(hex));
    }
}
