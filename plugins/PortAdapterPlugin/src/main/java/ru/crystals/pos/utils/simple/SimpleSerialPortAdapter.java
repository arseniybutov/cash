package ru.crystals.pos.utils.simple;

import jssc.SerialPort;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import ru.crystals.pos.utils.PortAdapterException;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class SimpleSerialPortAdapter implements SimplePortAdapter {

    private final Logger log;

    private SerialPort serialPort;
    private SerialPortConfiguration configuration;

    public SimpleSerialPortAdapter(Logger log) {
        Objects.requireNonNull(log, "logger could not be null");
        this.log = log;
    }

    @Override
    public void setConfiguration(SerialPortConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void openPort() throws PortAdapterException {
        Objects.requireNonNull(configuration, "Configuration could not be null");
        final String actualPort = getActualPortId(configuration.getPort());
        log.info("Opening port {} (configured as {})", actualPort, configuration.getPort());
        if (serialPort != null && serialPort.isOpened()) {
            close();
        }
        serialPort = new SerialPort(actualPort);
        try {
            serialPort.openPort();
            serialPort.setParams(configuration.getBaudRate(), configuration.getDataBits(), configuration.getStopBits(), configuration.getParity());
            log.info("Port {} has opened", actualPort);
        } catch (SerialPortException e) {
            throw new PortAdapterException("Unable to open port", e);
        }
    }

    @Override
    public void write(byte[] b) throws PortAdapterException {
        try {
            if (serialPort == null || !serialPort.isOpened()) {
                throw new PortAdapterException("Port not opened");
            }
            if (!serialPort.writeBytes(b)) {
                log.error("Error on writeBytes");
                throw new PortAdapterException("Error on writeBytes");
            }
        } catch (SerialPortException e) {
            log.error("Error on writeBytes ({})", e.getExceptionType(), e);
            throw new PortAdapterException("Error on writeBytes: " + e.getExceptionType(), e);
        }
    }

    @Override
    public void close() {
        try {
            log.debug("Try to close port {}", configuration.getPort());
            if (serialPort != null && serialPort.isOpened()) {
                serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);
                serialPort.closePort();
                log.debug("Port {} has closed", configuration.getPort());
            } else {
                log.debug("Port {} not opened or already closed", configuration.getPort());
            }
        } catch (SerialPortException e) {
            log.error("Error on close port: {},{}", e.getMethodName(), e.getMessage());
        }
    }

    private String getActualPortId(String portId) {
        if (StringUtils.startsWith(portId, "/dev/")) {
            try {
                final String result = new File(portId).getCanonicalPath();
                if (log.isDebugEnabled() && !Objects.equals(result, portId)) {
                    log.debug("Actual port for {} is {}", portId, result);
                }
                return result;
            } catch (IOException e) {
                log.warn("Unable to get real port by symlink {}", portId, e);
            }
        }
        return portId;
    }

    public void addEventListener(SerialPortEventListener listener) throws PortAdapterException {
        try {
            serialPort.addEventListener(listener);
        } catch (SerialPortException e) {
            throw new PortAdapterException("Error while add event listener");
        }
    }

    public byte[] readBytes() {
        try {
            return serialPort.readBytes();
        } catch (SerialPortException e) {
            log.error("Error while reading bytes from COM port {}", configuration.getPort());
            throw new RuntimeException(e);
        }
    }
}
