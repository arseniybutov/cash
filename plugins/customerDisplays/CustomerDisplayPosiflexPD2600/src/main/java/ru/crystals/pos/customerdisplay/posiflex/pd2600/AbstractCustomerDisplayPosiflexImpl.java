package ru.crystals.pos.customerdisplay.posiflex.pd2600;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.customerdisplay.exception.CustomerDisplayPluginException;
import ru.crystals.pos.customerdisplay.plugin.TextCustomerDisplayPluginAbstract;
import ru.crystals.pos.utils.PortAdapter;
import ru.crystals.pos.utils.PortAdapterException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Observable;
import java.util.Observer;

public abstract class AbstractCustomerDisplayPosiflexImpl extends TextCustomerDisplayPluginAbstract implements Observer {
    private static final int MAX_CHAR_PER_LINE = 20;
    protected static final int ESC = 0x1B;
    private static final byte[] RUSSIAN_LOCALE = new byte[]{ESC, 0x74, 0x06};
    private static final byte[] ENABLE_OVERWRITE_MODE = new byte[]{0x1F, 0x01};
    private static final byte[] HIDE_CURSOR = new byte[]{0x1F, 0x43, 0x00};
    private static final byte[] CLEAR_DISPLAY = new byte[]{0x0C};
    private static final String ENCODING = "cp866";
    protected boolean isDeviceConnected = false;
    private Logger logger = LoggerFactory.getLogger(AbstractCustomerDisplayPosiflexImpl.class);

    @Override
    public void open() throws CustomerDisplayPluginException {
        try {
            getAdapter().openPort();
            isDeviceConnected = true;
            configureDisplay();
        } catch (Exception e) {
            throw new CustomerDisplayPluginException(e.getMessage());
        }
    }

    @Override
    public synchronized void displayTextAt(int row, int column, String text) throws CustomerDisplayPluginException {
        try {
            executeCommand(text.getBytes(ENCODING));
        } catch (UnsupportedEncodingException e) {
            throw new CustomerDisplayPluginException(e.getMessage());
        }
    }

    @Override
    public void configureDisplay() throws CustomerDisplayPluginException {
        setCodeSet();
        setOverwriteMode();
        clearText();
    }

    public void setCodeSet() throws CustomerDisplayPluginException {
        executeCommand(RUSSIAN_LOCALE);
    }

    public void setOverwriteMode() throws CustomerDisplayPluginException {
        executeCommand(ENABLE_OVERWRITE_MODE);
    }

    public void hideCursor() throws CustomerDisplayPluginException {
        executeCommand(HIDE_CURSOR);
    }

    @Override
    public void clearText() throws CustomerDisplayPluginException {
        executeCommand(CLEAR_DISPLAY);
    }

    @Override
    public void verifyDevice() throws CustomerDisplayPluginException {

    }

    @Override
    protected int getMaxCharPerLine() {
        return MAX_CHAR_PER_LINE;
    }


    public void executeCommand(byte[] buffer) throws CustomerDisplayPluginException {
        if (isDeviceConnected) {
            try {
                getAdapter().write(buffer);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        } else {
            logger.error("Device is not connected");
        }
    }

    protected abstract PortAdapter getAdapter() throws IOException, PortAdapterException;

    @Override
    public void update(Observable o, Object arg) {
        isDeviceConnected = (Boolean) arg;
        if (isDeviceConnected) {
            try {
                configureDisplay();
            } catch (CustomerDisplayPluginException e) {
                logger.error(e.getMessage());
            }
        } else {
            logger.error("Device is not available");
        }
    }

    /**
     * Setter для тестов
     */
    void setDeviceConnected(boolean deviceConnected) {
        isDeviceConnected = deviceConnected;
    }
}
