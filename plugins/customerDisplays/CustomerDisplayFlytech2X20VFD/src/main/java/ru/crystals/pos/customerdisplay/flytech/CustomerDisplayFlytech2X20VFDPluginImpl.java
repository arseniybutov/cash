package ru.crystals.pos.customerdisplay.flytech;

import ru.crystals.pos.customerdisplay.exception.CustomerDisplayPluginException;
import ru.crystals.pos.customerdisplay.plugin.TextCustomerDisplayPluginAbstract;

import java.util.concurrent.locks.LockSupport;

public class CustomerDisplayFlytech2X20VFDPluginImpl extends TextCustomerDisplayPluginAbstract {

    private static final int MAX_CHAR_PER_LINE = 20;

    private static final int ESC = 0x1B;

    private static final int Q = 0x51;

    private static final int A = 0x41;

    private static final int B = 0x42;

    private static final int CR = 0x0D;

    private static final int RUS = 0x52;

    private static final int ENG = 0x41;

    // Задержка в наносекундах
    private static final long DELAY = 1000000000;

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
        switchType();
        LockSupport.parkNanos(DELAY);
        initCharset();
        LockSupport.parkNanos(DELAY);
        setFontSet();
        clearText();
        setOverwriteMode();
        setCursorOff();
    }

    public void switchType() throws CustomerDisplayPluginException {
        byte[] hex = {0x02, 0x05, 0x43, 0x37, 0x03};
        executeCommand(new String(hex));
    }

    public void initCharset() throws CustomerDisplayPluginException {
        byte[] hex = {0x02, 0x05, 0x53, 0x3C, 0x03};
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