package ru.crystals.pos.customerdisplay.datecs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.bundles.BundleManager;
import ru.crystals.bundles.BundleRef;
import ru.crystals.pos.InternalCashPoolExecutor;
import ru.crystals.pos.customerdisplay.exception.CustomerDisplayPluginException;
import ru.crystals.pos.customerdisplay.plugin.TextCustomerDisplayPluginAbstract;

public class CustomerDisplayZQVFD2300PluginImpl extends TextCustomerDisplayPluginAbstract {

    private final Logger LOG = LoggerFactory.getLogger(TextCustomerDisplayPluginAbstract.class);

    /*
     * Для корректного отображения на устройстве нужно выставить все джампера вверх, кроме 3-го
     */

    private static final int MAX_CHAR_PER_LINE = 20;

    private static final int ESC = 0x1B;

    private static final int HOME = 0x0B;

    private static final int DOWN = 0x0A;

    private static final int R = 0x52;

    private static final int T = 0x74;

    private static final int CR = 0x0D;

    private static final int RUS = 0x07;

    /**
     * Переодичность с которой посылается команда в дисплей,
     * для предотвращения появления скринсейвера "welcome"
     */
    private static final int WAKE_UP_TIME = 2 * 60 * 1000;

    @BundleRef
    private InternalCashPoolExecutor executor;

    public CustomerDisplayZQVFD2300PluginImpl() {
        //
    }

    @Override
    public void open() throws CustomerDisplayPluginException {
        super.open();
        BundleManager.applyWhenAvailable(InternalCashPoolExecutor.class, it -> executor = it);
        executor.execute(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(WAKE_UP_TIME);
                    setOverwriteMode();
                }
            } catch (Exception e) {
                LOG.error("Error in wake up loop: ", e);
                Thread.currentThread().interrupt();
            }
        });
    }

    @Override
    public void displayTextAt(int row, int column, String text) throws CustomerDisplayPluginException {
        if (column < 0 || column > 20) {
            throw new CustomerDisplayPluginException("Incorrect input parameters. column=" + column);
        } else if (row < 0 || row > 1) {
            throw new CustomerDisplayPluginException("Incorrect input parameters. row=" + row);
        } else if (text == null) {
            throw new CustomerDisplayPluginException("Incorrect input parameters. text=null");
        }

        switch (row) {
            case 0:
                byte[] firstRow = {HOME};
                executeCommandSync(new String(firstRow));
                break;
            case 1:
                byte[] secondRow = {HOME, DOWN};
                executeCommandSync(new String(secondRow));
                break;
            default:
                break;
        }
        executeCommandSync(text);
    }

    @Override
    public void clearText() throws CustomerDisplayPluginException {
        byte[] hex = {0x0C};
        executeCommandSync(new String(hex));
    }

    @Override
    public void configureDisplay() throws CustomerDisplayPluginException {
        initDisplay();
        setCodeSet();
        setFontSet();
        setOverwriteMode();
        setCursorOff();
        clearText();
    }

    private void initDisplay() throws CustomerDisplayPluginException {
        byte[] hex = {ESC, 0x40};
        executeCommandSync(new String(hex));
    }

    public void setCodeSet() throws CustomerDisplayPluginException {
        byte[] hex = {ESC, T, RUS};
        executeCommandSync(new String(hex));
    }

    public void setFontSet() throws CustomerDisplayPluginException {
        byte[] hex = {ESC, R, RUS};
        executeCommandSync(new String(hex));
    }

    public void setOverwriteMode() throws CustomerDisplayPluginException {
        byte[] hex = {0x1F, 0x01};
        executeCommandSync(new String(hex));
    }

    public void sendData(String text, int row) throws CustomerDisplayPluginException {
        byte[] hex = {ESC, 0x24, 0, (byte) (row + 1)};
        byte[] hex2 = {CR};
        executeCommandSync(new String(hex) + text + new String(hex2));
    }

    public void setCursorOn() throws CustomerDisplayPluginException {
        byte[] hex = {ESC, 0x43, 0x01};
        executeCommandSync(new String(hex));
    }

    public void setCursorOff() throws CustomerDisplayPluginException {
        byte[] hex = {ESC, 0x43, 0x00};
        executeCommandSync(new String(hex));
    }

    @Override
    protected int getMaxCharPerLine() {
        return MAX_CHAR_PER_LINE;
    }

    @Override
    public void verifyDevice() {
        //Устройство данные не передает, верифицировать не получится
    }

    synchronized private void executeCommandSync(String command) throws CustomerDisplayPluginException {
        executeCommand(command);
    }
}
