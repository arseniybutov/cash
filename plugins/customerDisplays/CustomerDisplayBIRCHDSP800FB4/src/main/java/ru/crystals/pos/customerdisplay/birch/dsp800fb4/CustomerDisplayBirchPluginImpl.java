package ru.crystals.pos.customerdisplay.birch.dsp800fb4;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.customerdisplay.exception.CustomerDisplayPluginException;
import ru.crystals.pos.customerdisplay.plugin.TextCustomerDisplayPluginAbstract;
import ru.crystals.pos.utils.SerialPortAdapter;

import java.io.IOException;


//Протокол CD5220 Command Mode
public class CustomerDisplayBirchPluginImpl extends TextCustomerDisplayPluginAbstract {

    private static final Logger log = LoggerFactory.getLogger(CustomerDisplayBirchPluginImpl.class);

    private static final int ESC = 0x1B;
    private static final int AT = 0x40;
    private static final int CLR = 0x0C;
    private static final int C = 0x63;
    private static final int CR = 0x6C;
    private static final int RUS = 0x4C;
    private static final int MAX_CHAR_PER_LINE = 20;
    private SerialPortAdapter adapter;

    @Override
    public void open() throws CustomerDisplayPluginException {
        adapter = getAdapter();
        configureDisplay();
    }

    @Override
    public void displayTextAt(int row, int column, String text) throws CustomerDisplayPluginException {
        byte[] hex = {ESC, CR, (byte) column, (byte) row};
        executeCommand(new String(hex) + text);
    }

    @Override
    public void clearText() throws CustomerDisplayPluginException {
        byte[] hex = {ESC, CLR};
        executeCommand(new String(hex));
    }

    @Override
    public void configureDisplay() throws CustomerDisplayPluginException {
        setDisplayOn();
        setCharacterSet();
        clearText();
    }

    public void setCharacterSet() throws CustomerDisplayPluginException {
        byte[] hex = {ESC, C, RUS};
        executeCommand(new String(hex));
    }

    protected void setDisplayOn() throws CustomerDisplayPluginException {
        byte[] hex = {ESC, AT};
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

    protected SerialPortAdapter getAdapter() throws CustomerDisplayPluginException {
        if (adapter == null) {
            adapter =
                    new SerialPortAdapter().setPort(getConfig().getPort()).setBaudRate(getConfig().getBaudRate()).setDataBits(getConfig().getDataBits())
                            .setStopBits(getConfig().getStopBits()).setParity(getConfig().getParity()).setOwner(this.getClass().getName());
            try {
                adapter.openPort();
            } catch (Exception e) {
                throw new CustomerDisplayPluginException(e.getMessage());
            }
        }
        return adapter;
    }

    protected void setAdapter(SerialPortAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void executeCommand(String s) throws CustomerDisplayPluginException {
        try {
            byte[] buffer;
            if (getConfig().getEncoding() != null && !getConfig().getEncoding().isEmpty()) {
                buffer = s.getBytes(getConfig().getEncoding());
            } else {
                buffer = s.getBytes();
            }
            adapter.write(buffer);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
