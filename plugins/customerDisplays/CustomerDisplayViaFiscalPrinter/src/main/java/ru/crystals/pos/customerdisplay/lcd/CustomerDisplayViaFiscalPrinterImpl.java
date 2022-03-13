package ru.crystals.pos.customerdisplay.lcd;

import gnu.io.SerialPort;
import ru.crystals.bundles.BundleManager;
import ru.crystals.pos.customerdisplay.exception.CustomerDisplayPluginException;
import ru.crystals.pos.customerdisplay.plugin.TextCustomerDisplayPluginAbstract;
import ru.crystals.pos.fiscalprinter.FiscalPrinter;
import ru.crystals.pos.fiscalprinter.FiscalPrinterImpl;
import ru.crystals.pos.fiscalprinter.FiscalPrinterPlugin;

public class CustomerDisplayViaFiscalPrinterImpl extends TextCustomerDisplayPluginAbstract {

    private static final int MAX_CHAR_PER_LINE = 20;
    private String[] lines = new String[2];
    private DisplayThread displayThread;
    private long interval = 150;

    @Override
    public void configureSerialPort(SerialPort serialPort) {
        System.out.println("configureSerialPort");
    }

    @Override
    public void open() throws CustomerDisplayPluginException {
        System.out.println("open");
        try {
            Object obj = BundleManager.get(FiscalPrinter.class);
            if (obj != null && obj instanceof FiscalPrinterImpl) {
                FiscalPrinterImpl fiscalPrinter = (FiscalPrinterImpl) obj;
                FiscalPrinterPlugin provider = fiscalPrinter.getProvider();
                if (provider != null && provider instanceof FiscalPrinterPluginWithCustomerDisplay) {
                    FiscalPrinterPluginWithCustomerDisplay fr = (FiscalPrinterPluginWithCustomerDisplay) provider;
                    displayThread = new DisplayThread(fr, interval);
                    displayThread.start();
                }
            }

        } catch (Exception e) {
            throw new CustomerDisplayPluginException(e.getMessage());
        }
    }

    public long getInterval() {
        if (displayThread != null) {
            interval = displayThread.getInterval();
        }
        return interval;
    }

    public void setInterval(long interval) {
        if (displayThread != null) {
            displayThread.setInterval(interval);
            this.interval = interval;
        }
    }

    @Override
    public void executeCommand(byte[] command, byte[] param) {
        //
    }

    @Override
    public void executeCommand(String command) {
        //
    }

    @Override
    public byte[] executeCommandWithAnswer(String command, int answerLength) {
        System.out.println("executeCommandWithAnswer");
        return new byte[0];
    }

    @Override
    public void flush() {
        //
    }

    @Override
    public void close() {
        clearText();
    }

    @Override
    public void displayTextAt(int row, int column, String text) {
        if (text == null) {
            text = "";
        }
        text = String.format("%" + (column + 1) + "s", text);
        if (displayThread != null) {
            if (row == 0) {
                displayThread.setLine1(text);
            } else if (row == 1) {
                displayThread.setLine2(text);
            }
        }
    }

    @Override
    public void clearText() {
        lines[0] = "";
        lines[1] = "";
        if (displayThread != null) {
            displayThread.setLine1("");
            displayThread.setLine2("");
        }
    }

    @Override
    public void configureDisplay() {
        //
    }

    public void setCountryCode() {
        //
    }

    @Override
    public void verifyDevice() {
        //
    }

    @Override
    public int getMaxCharPerLine() {
        return MAX_CHAR_PER_LINE;
    }

}
