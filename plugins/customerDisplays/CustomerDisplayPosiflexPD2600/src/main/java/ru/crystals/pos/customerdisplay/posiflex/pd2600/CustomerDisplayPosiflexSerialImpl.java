package ru.crystals.pos.customerdisplay.posiflex.pd2600;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.utils.PortAdapter;
import ru.crystals.pos.utils.PortAdapterException;
import ru.crystals.pos.utils.SerialPortAdapter;
import ru.crystals.pos.utils.SerialPortAdapterObservable;

import java.io.File;
import java.io.IOException;

public class CustomerDisplayPosiflexSerialImpl extends AbstractCustomerDisplayPosiflexImpl {
    private final Logger logger = LoggerFactory.getLogger(CustomerDisplayPosiflexSerialImpl.class);
    private SerialPortAdapter portAdapter = new SerialPortAdapter();
    private SerialPortAdapterObservable serialPortAdapterObservable;

    public void setPort(String port) {
        String resultPort = port;
        if (StringUtils.startsWith(port, "/dev/")) {
            try {
                resultPort = new File(port).getCanonicalPath();
                createPortAdapterObservable();
                serialPortAdapterObservable.addObserver(this);
                serialPortAdapterObservable.setSettingsPortID(port);
            } catch (IOException e) {
                logger.warn("Unable to get real port by symlink " + port, e);
            }
        }
        portAdapter.setPort(resultPort);
    }

    @Override
    protected PortAdapter getAdapter() throws IOException, PortAdapterException {
        return serialPortAdapterObservable == null ? portAdapter : serialPortAdapterObservable;
    }

    //для тестов
    void createPortAdapterObservable() {
        serialPortAdapterObservable = new SerialPortAdapterObservable(portAdapter);
    }

    void setSerialPortAdapterObservable(SerialPortAdapterObservable serialPortAdapterObservable) {
        this.serialPortAdapterObservable = serialPortAdapterObservable;
    }
}
