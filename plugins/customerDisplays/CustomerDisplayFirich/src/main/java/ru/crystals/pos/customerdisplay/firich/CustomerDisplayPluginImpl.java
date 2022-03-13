package ru.crystals.pos.customerdisplay.firich;

import ru.crystals.pos.customerdisplay.exception.CustomerDisplayPluginException;
import ru.crystals.pos.customerdisplay.plugin.TextCustomerDisplayPluginAbstract;

public class CustomerDisplayPluginImpl extends TextCustomerDisplayPluginAbstract {

    /*
     * Для корректного отображения на устройстве нужно выставить все джампера вниз, кроме 4 и 5
     */

    private static final int MAX_CHAR_PER_LINE = 20;

    private static final int ESC = 0x1B;

    private static final int Q = 0x51;

    private static final int A = 0x41;

    private static final int B = 0x42;

    private static final int CR = 0x0D;

    private static final int RUS = 0x52;

    private static final int ENG = 0x41;

    public CustomerDisplayPluginImpl() {
        //
    }

    @Override
    public void displayTextAt(int row, int column, String text) throws CustomerDisplayPluginException {
        byte[] hex = {ESC, 0x6C, (byte) (column + 1), (byte) (row + 1)};
        executeCommand(new String(hex) + text);
    }

    @Override
    public void clearText() throws CustomerDisplayPluginException {
        byte[] hex = {0x0C};
        executeCommand(new String(hex));
    }

    @Override
    public void configureDisplay() throws CustomerDisplayPluginException {
        setCodeSet();
        setFontSet();
        clearText();
        setOverwriteMode();
        setCursorOff();
    }

    public void setCodeSet() throws CustomerDisplayPluginException {
        byte[] hex = {ESC, 0x63, RUS};
        executeCommand(new String(hex));
    }

    public void setFontSet() throws CustomerDisplayPluginException {
        byte[] hex = {ESC, 0x66, ENG};
        executeCommand(new String(hex));
    }

    public void setOverwriteMode() throws CustomerDisplayPluginException {
        byte[] hex = {ESC, 0x11};
        executeCommand(new String(hex));
    }

    public void setCursorOff() throws CustomerDisplayPluginException {
        byte[] hex = {ESC, 0x5F, 0x00};
        executeCommand(new String(hex));
    }

    public void sendData(String text, int row) throws CustomerDisplayPluginException {
        byte[] hex = {ESC, Q, 0, (byte) (row + 1)};

        if (row == 0) {
            hex[2] = A;
        } else if (row == 1) {
            hex[2] = B;
        }

        byte[] hex2 = {CR};
        executeCommand(new String(hex) + text + new String(hex2));
    }

    public void moveCursorRight() throws CustomerDisplayPluginException {
        byte[] hex = {ESC, 0x5B, 0x43, 0x09};
        executeCommand(new String(hex));
    }

    public void setCursorOn() throws CustomerDisplayPluginException {
        byte[] hex = {ESC, 0x5F, 0x01};
        executeCommand(new String(hex));
    }

    @Override
    public void verifyDevice() {
        //
    }

    @Override
    protected int getMaxCharPerLine() {
        return MAX_CHAR_PER_LINE;
    }

}
