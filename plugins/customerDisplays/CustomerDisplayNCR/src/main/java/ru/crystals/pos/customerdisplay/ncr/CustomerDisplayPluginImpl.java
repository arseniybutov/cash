package ru.crystals.pos.customerdisplay.ncr;

import ru.crystals.pos.customerdisplay.exception.CustomerDisplayPluginException;
import ru.crystals.pos.customerdisplay.plugin.TextCustomerDisplayPluginAbstract;

public class CustomerDisplayPluginImpl extends TextCustomerDisplayPluginAbstract {

    private static final int ESC = 0x1B;

    private static final int CR = 0x13;

    private static final int STX = 0x02;

    private static final int ENQ = 0x05;

    private static final int RUS = 0x22;

    private static final int MAX_CHAR_PER_LINE = 20;

    private int columnCount;

    public CustomerDisplayPluginImpl() {
        columnCount = getConfig().getColumnsCount();
    }

    @Override
    public void displayTextAt(int row, int column, String text) throws CustomerDisplayPluginException {
        byte[] hex = {ESC, CR, (byte) (row * columnCount + column)};
        executeCommand(new String(hex) + text);
    }

    @Override
    public void clearText() throws CustomerDisplayPluginException {
        byte[] hex = {ESC, STX};
        executeCommand(new String(hex));
    }

    @Override
    public void configureDisplay() throws CustomerDisplayPluginException {
        setDisplayOn();
        setCharacterSet();
        clearText();
    }

    public void setCharacterSet() throws CustomerDisplayPluginException {
        byte[] hex = {ESC, RUS};
        executeCommand(new String(hex));
    }

    private void setDisplayOn() throws CustomerDisplayPluginException {
        byte[] hex = {ESC, ENQ};
        executeCommand(new String(hex));
    }

    @Override
    public void verifyDevice() throws CustomerDisplayPluginException {
        // NOP
    }

    @Override
    protected int getMaxCharPerLine() {
        return MAX_CHAR_PER_LINE;
    }

}
