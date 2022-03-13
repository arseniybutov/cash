package ru.crystals.pos.customerdisplay.kraftway.cdk3100;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.customerdisplay.LineDisplayConfig;
import ru.crystals.pos.customerdisplay.exception.CustomerDisplayPluginException;
import ru.crystals.pos.customerdisplay.plugin.TextCustomerDisplayPluginAbstract;
import ru.crystals.pos.utils.PortAdapterException;
import ru.crystals.pos.utils.simple.SerialPortConfiguration;
import ru.crystals.pos.utils.simple.SimplePortAdapter;
import ru.crystals.pos.utils.simple.SimpleSerialPortAdapter;
import ru.crystals.pos.utils.simple.SimpleSerialPortAdapterObservable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Observable;
import java.util.Observer;

/**
 * Протокол CD5220 Command Mode
 */
public class CustomerDisplayKraftwayCDK3100PluginImpl extends TextCustomerDisplayPluginAbstract implements Observer {

    private static final Logger log = LoggerFactory.getLogger(CustomerDisplayKraftwayCDK3100PluginImpl.class);

    private static final int ESC = 0x1B;
    private static final int CLR = 0x0C;
    private static final byte[] CLEAR_COMMAND = {ESC, CLR};

    private final SimplePortAdapter serialPortAdapter;

    private boolean isDeviceAvailable = true;
    private Charset encoding = StandardCharsets.UTF_8;

    public CustomerDisplayKraftwayCDK3100PluginImpl() {
        super();
        this.serialPortAdapter = new SimpleSerialPortAdapterObservable(new SimpleSerialPortAdapter(log), log);
    }

    /**
     * Constructor for test
     */
    CustomerDisplayKraftwayCDK3100PluginImpl(SimplePortAdapter serialPortAdapter) {
        super();
        this.serialPortAdapter = serialPortAdapter;
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
            encoding = getEncoding();
            serialPortAdapter.openPort();
        } catch (Exception e) {
            log.error("Unable to open port", e);
            throw new CustomerDisplayPluginException(e.getMessage());
        }
    }

    private Charset getEncoding() {
        final String configuredEncoding = StringUtils.trimToNull(getConfig().getEncoding());
        if (configuredEncoding != null) {
            return Charset.forName(configuredEncoding);
        }
        return StandardCharsets.UTF_8;
    }

    @Override
    public void displayTextAt(int row, int column, String text) {
        executeCommand(text);
    }

    @Override
    public void clearText() {
        executeCommand(CLEAR_COMMAND);
    }

    @Override
    protected int getMaxCharPerLine() {
        return getConfig().getColumnsCount();
    }

    @Override
    public void executeCommand(String s) {
        if (!isDeviceAvailable) {
            return;
        }
        executeCommand(s.getBytes(encoding));
    }

    private void executeCommand(byte[] bytes) {
        if (!isDeviceAvailable) {
            return;
        }
        try {
            serialPortAdapter.write(bytes);
        } catch (PortAdapterException e) {
            log.error("", e);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        isDeviceAvailable = (Boolean) arg;
    }
}
