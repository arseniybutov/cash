package ru.crystals.pos.customerdisplay.emulator;

import ru.crystals.bundles.BundleManager;
import ru.crystals.pos.customerdisplay.exception.CustomerDisplayPluginException;
import ru.crystals.pos.customerdisplay.plugin.TextCustomerDisplayPluginAbstract;

import java.util.LinkedList;

public class CustomerDisplayEmulator extends TextCustomerDisplayPluginAbstract implements ru.crystals.pos.customerdisplay.CustomerDisplayEmulator {

    private static final char BORDER_CHAR = '*';
    public static final int LINE_COUNT = 2;
    private static final int MAX_CHAR_PER_LINE = 20;
    private String[] screen = new String[]{"", ""};
    private LinkedList<String[]> previousStates = new LinkedList<>();

    @Override
    protected void displayTextAt(int row, int column, String text) throws CustomerDisplayPluginException {
        if (row < LINE_COUNT) {
            screen[row] = BORDER_CHAR + text + BORDER_CHAR;
        }
    }

    @Override
    public String getFirstLine(int depth) {
        if (depth == 0) {
            return screen[0];
        } else {
            return previousStates.get(depth - 1)[0];
        }

    }

    @Override
    public String getSecondLine(int depth) {
        if (depth == 0) {
            return screen[1];
        } else {
            return previousStates.get(depth - 1)[1];
        }
    }

    @Override
    protected int getMaxCharPerLine() {
        return MAX_CHAR_PER_LINE;
    }

    @Override
    public void clearText() {
        previousStates.addFirst(new String[]{screen[0], screen[1]});
        screen[0] = "";
        screen[1] = "";
    }

    @Override
    public void verifyDevice() {
        //
    }

    public void open() {
        BundleManager.add(ru.crystals.pos.customerdisplay.CustomerDisplayEmulator.class, this);
    }

    public void executeCommand(byte[] command, byte[] param) {
        //
    }

    public void executeCommand(String command) {
        //
    }

    public byte[] executeCommandWithAnswer(String command, int answerLenght) {
        return null;
    }

    public void flush() {
        //
    }

    public void close() {
        //
    }

    public void setPort(String port) {
        //
    }

    public void setBaudRate(int baudRate) {
        //
    }

    public void setDataBits(int dataBits) {
        //
    }

    public void setStopBits(int stopBits) {
        //
    }

    public void setParity(int parity) {
        //
    }
}
