package ru.crystals.pos.customerdisplay.wnba64;

import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import ru.crystals.pos.customerdisplay.LineDisplayConfig;
import ru.crystals.pos.customerdisplay.ResBundleCustomerDisplay;
import ru.crystals.pos.customerdisplay.exception.CustomerDisplayPluginException;
import ru.crystals.pos.customerdisplay.plugin.TextCustomerDisplayPluginAbstract;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class CustomerDisplayPluginImpl extends TextCustomerDisplayPluginAbstract {

    private static final int MAX_CHAR_PER_LINE = 20;

    public CustomerDisplayPluginImpl() {
    }

    @Override
    public void configureSerialPort(SerialPort serialPort) throws CustomerDisplayPluginException {
        try {
            LineDisplayConfig config = getConfig();
            serialPort.setSerialPortParams(config.getBaudRate(), config.getDataBits(), config.getStopBits(), SerialPort.PARITY_ODD);
            serialPort.notifyOnDSR(false);
            serialPort.notifyOnCTS(true);
        } catch (UnsupportedCommOperationException e) {
            throw new CustomerDisplayPluginException(ResBundleCustomerDisplay.getString("ERROR_UNSUPPORTED_COMM_OPERATION"));
        }
    }

    @Override
    public void displayTextAt(int row, int column, String text) throws CustomerDisplayPluginException {
        OutputStreamWriter out = getOut();
        try {
            out.write(0x1B);
            out.write(0x5B);
            out.write(Byte.toString((byte) (row + 1)));
            out.write(0x3B);
            out.write(Byte.toString((byte) (column + 1)));
            out.write(0x48);
            out.write(text);
            flush();
        } catch (IOException e) {
            throw new CustomerDisplayPluginException(ResBundleCustomerDisplay.getString("ERROR_IO"));
        }
    }

    @Override
    public void clearText() throws CustomerDisplayPluginException {
        OutputStreamWriter out = getOut();
        try {
            out.write(0x1B);
            out.write(0x5B);
            out.write(0x32);
            out.write(0x4A);
            flush();
        } catch (IOException e) {
            throw new CustomerDisplayPluginException(ResBundleCustomerDisplay.getString("ERROR_IO"));
        }
    }

    @Override
    public void configureDisplay() throws CustomerDisplayPluginException {
        setCountryCode();
        clearText();
    }

    public void setCountryCode() throws CustomerDisplayPluginException {
        OutputStreamWriter out = getOut();
        try {
            out.write(0x1B);
            out.write(0x52);
            out.write(0x35);
            flush();
        } catch (IOException e) {
            throw new CustomerDisplayPluginException(ResBundleCustomerDisplay.getString("ERROR_IO"));
        }
    }

    @Override
    public void verifyDevice() throws CustomerDisplayPluginException {
        // Nothing to see here
    }

    @Override
    public int getMaxCharPerLine() {
        return MAX_CHAR_PER_LINE;
    }

}
