package ru.crystals.pos.customerdisplay.fujitsuVF60;

import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.customerdisplay.exception.CustomerDisplayPluginException;
import ru.crystals.pos.customerdisplay.plugin.TextCustomerDisplayPluginAbstract;

public final class FujitsuVF60 extends TextCustomerDisplayPluginAbstract {
    private static final int MAX_CHAR_PER_LINE = 20;

    private static final int ESC = 0x1B;

    private static final int CR = 0x0D;

    private static final int RUS = 0x06;

    private static final int ENG = 0x00;

    @Override
    public void displayTextAt(int row, int column, String text) throws CustomerDisplayPluginException {
        executeCommand(buildPxPyCmd(row, column, text, false));
    }

    @Override
    public void clearText() throws CustomerDisplayPluginException {
        byte[] hex = {ESC, 0x5B, 0x32, 0x4A};
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
        byte[] hex = {ESC, 0x52, ENG};
        executeCommand(new String(hex));
    }

    public void setFontSet() throws CustomerDisplayPluginException {
        byte[] hex = {ESC, 0x74, RUS};
        executeCommand(new String(hex));
    }

    public void setOverwriteMode() {
        //
    }

    public void setCursorOff() throws CustomerDisplayPluginException {
        byte[] hex = {ESC, 0x5C, 0x3F, 0x4C, 0x43, 0x30};
        executeCommand(new String(hex));
    }

    public void sendData(String text, int row) throws CustomerDisplayPluginException {
        executeCommand(buildPxPyCmd(row, 1, text, true));
    }

    public void moveCursorRight() throws CustomerDisplayPluginException {
        byte[] hex = {0x08};
        executeCommand(new String(hex));
    }

    public void setCursorOn() throws CustomerDisplayPluginException {
        byte[] hex = {ESC, 0x5C, 0x3F, 0x4C, 0x43, 0x32};
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

    @Override
    public void close() throws CustomerDisplayPluginException {
        clearText();
        super.close();
    }

    private static String findPx(int column) {
        return Integer.valueOf(column <= 0 ? 1 : (Math.min(column, MAX_CHAR_PER_LINE))).toString();
    }

    private static String findPy(int row) {
        return Integer.valueOf(row <= 0 ? 1 : 2).toString();
    }

    private static String buildPxPyCmd(int row, int column, String text, boolean addCR) {
        byte[] hex1 = {ESC, 0x5B};
        byte[] hex2 = {0x3B};
        byte[] hex3 = {0x48};
        return new String(hex1) + findPy(row) + new String(hex2) + findPx(column) + new String(hex3) + text + (addCR ? new String(new byte[]{CR}) : StringUtils.EMPTY);
    }

}