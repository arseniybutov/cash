package ru.crystals.pos.customerdisplay.csi2020;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.customerdisplay.LineDisplayConfig;
import ru.crystals.pos.customerdisplay.exception.CustomerDisplayPluginException;
import ru.crystals.pos.customerdisplay.plugin.TextCustomerDisplayPluginAbstract;
import ru.crystals.pos.utils.PortAdapterException;
import ru.crystals.pos.utils.simple.SerialPortConfiguration;
import ru.crystals.pos.utils.simple.SimplePortAdapter;
import ru.crystals.pos.utils.simple.SimpleSerialPortAdapter;

import java.nio.charset.Charset;

public class CustomerDisplayCSI2020 extends TextCustomerDisplayPluginAbstract {

    private static final Logger log = LoggerFactory.getLogger(CustomerDisplayCSI2020.class);

    private static final int MAX_CHAR_PER_LINE = 20;
    private static final int ESC = 0x1B;
    private static final String STR0 = new String(new byte[]{ESC, 0x51, 0x41});
    private static final String STR1 = new String(new byte[]{ESC, 0x51, 0x42});
    private static final String CR = "\r";
    private static final byte[] CLEAR_COMMAND = {0x0C};
    private static final byte[] INIT_COMMAND = {ESC, 0x40};
    private static final byte[] CODESET_COMMAND = {ESC, 0x74, 0x07};
    private static final byte[] FONTSET_COMMAND = {ESC, 0x52, 0x0C};

    private SimplePortAdapter serialPortAdapter;

    public CustomerDisplayCSI2020() {
        super();
        this.serialPortAdapter = new SimpleSerialPortAdapter(log);
    }

    @Override
    public void open() throws CustomerDisplayPluginException {
        final LineDisplayConfig config = getConfig();
        serialPortAdapter.setConfiguration(SerialPortConfiguration.builder()
                .port(config.getPort())
                .baudRate(config.getBaudRate())
                .dataBits(config.getDataBits())
                .stopBits(config.getStopBits())
                .parity(config.getParity())
                .build());
        try {
            serialPortAdapter.openPort();
        } catch (Exception e) {
            log.error("Unable to open port", e);
            throw new CustomerDisplayPluginException(e.getMessage());
        }
        configureDisplay();
    }

    @Override
    public void configureDisplay() throws CustomerDisplayPluginException {
        initDisplay();
        setCodeSet();
        setFontSet();
        clearText();
    }

    public void initDisplay() throws CustomerDisplayPluginException {
        executeCommand(INIT_COMMAND);
    }

    public void setCodeSet() throws CustomerDisplayPluginException {
        executeCommand(CODESET_COMMAND);
    }

    public void setFontSet() throws CustomerDisplayPluginException {
        executeCommand(FONTSET_COMMAND);
    }

    @Override
    public void displayTextAt(int row, int column, String text) throws CustomerDisplayPluginException {
        executeCommand(((row == 0) ? STR0 : STR1) + text + CR);
    }

    @Override
    public void clearText() throws CustomerDisplayPluginException {
        executeCommand(CLEAR_COMMAND);
    }

    @Override
    protected int getMaxCharPerLine() {
        return MAX_CHAR_PER_LINE;
    }

    @Override
    public void executeCommand(String s) throws CustomerDisplayPluginException {
        executeCommand(s.getBytes(Charset.forName("cp866")));
    }

    private void executeCommand(byte[] bytes) throws CustomerDisplayPluginException {
        try {
            serialPortAdapter.write(bytes);
        } catch (PortAdapterException e) {
            log.error("", e);
        }
    }
}
