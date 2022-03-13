package ru.crystals.pos.bank.inpas.smartsale.serial;

import jssc.SerialPort;
import jssc.SerialPortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.utils.PortAdapterException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class JsscSerialPortConnector implements SimpleSerialPortConnector {
    public static final String ID = "jssc";

    private static final Logger log = LoggerFactory.getLogger(JsscSerialPortConnector.class);

    private String portID;
    private int baudRate = SerialPort.BAUDRATE_9600;
    private int dataBits = SerialPort.DATABITS_8;
    private int stopBits = SerialPort.STOPBITS_1;
    private int parity = SerialPort.PARITY_NONE;
    private SerialPort serialPort;

    @Override
    public JsscSerialPortConnector setPort(String portID) {
        this.portID = portID;
        return this;
    }

    @Override
    public JsscSerialPortConnector setDataBits(int dataBits) {
        this.dataBits = dataBits;
        return this;
    }

    @Override
    public JsscSerialPortConnector setParity(String parity) {
        if (parity.equalsIgnoreCase("NONE")) {
            this.parity = SerialPort.PARITY_NONE;
        } else if (parity.equalsIgnoreCase("EVEN")) {
            this.parity = SerialPort.PARITY_EVEN;
        } else if (parity.equalsIgnoreCase("MARK")) {
            this.parity = SerialPort.PARITY_MARK;
        } else if (parity.equalsIgnoreCase("SPACE")) {
            this.parity = SerialPort.PARITY_SPACE;
        } else {
            this.parity = SerialPort.PARITY_ODD;
        }
        return this;
    }

    @Override
    public JsscSerialPortConnector setStopBits(String stopBits) {
        if (stopBits.equalsIgnoreCase("2")) {
            this.stopBits = SerialPort.STOPBITS_2;
        } else if (stopBits.equalsIgnoreCase("1.5") || stopBits.equalsIgnoreCase("1,5")) {
            this.stopBits = SerialPort.STOPBITS_1_5;
        } else {
            this.stopBits = SerialPort.STOPBITS_1;
        }
        return this;
    }

    @Override
    public JsscSerialPortConnector setBaudRate(String baudRate) {
        this.baudRate = Integer.parseInt(baudRate);
        return this;
    }

    private void innerOpenPort() throws SerialPortException {
        log.info("Try to open port {}", portID);
        if (serialPort != null && serialPort.isOpened()) {
            closePort();
        }
        serialPort = new SerialPort(portID);
        serialPort.openPort();
        serialPort.setParams(baudRate, dataBits, stopBits, parity);
        log.info("Port {} has opened", portID);
    }

    private void closePort() {
        try {
            log.info("Try to close port {}", portID);
            if (serialPort != null && serialPort.isOpened()) {
                serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);
                serialPort.closePort();
                log.info("Port {} has closed", portID);
            } else {
                log.warn("Port {} not opened or already closed", portID);
            }
        } catch (SerialPortException e) {
            log.error("Error on close port: {},{}", e.getMethodName(), e.getMessage());
        }
    }

    @Override
    public void openPort() throws IOException, PortAdapterException {
        if (portID == null) {
            throw new IOException("Port is null");
        }
        try {
            innerOpenPort();
        } catch (SerialPortException e) {
            throw new PortAdapterException(e.getExceptionType() + ": " + portID, e);
        }
        log.info("connection [{}] was opened successfully", this);
    }

    @Override
    public void write(byte[] b) throws IOException {
        try {
            writeInner(b);
        } catch (IOException e) {
            log.error("Error on write, port will be reopened once");
            reopenPort();
            writeInner(b);
        }
    }

    private void writeInner(byte[] b) throws IOException {
        try {
            if (serialPort == null || !serialPort.isOpened()) {
                log.warn("Port was closed, will be opened");
                innerOpenPort();
            }
            if (!serialPort.writeBytes(b)) {
                log.error("Error on writeBytes");
                throw new IOException("Error on writeBytes");
            }
        } catch (SerialPortException e) {
            log.error("Error on writeBytes ({})", e.getExceptionType(), e);
            throw new IOException("Error on writeBytes: " + e.getExceptionType(), e);
        }
    }

    private void reopenPort() throws IOException {
        try {
            log.info("Trying to reopen port");
            closePort();
            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
            innerOpenPort();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Error on reopen port (interrupted)");
        } catch (SerialPortException e) {
            log.error("Error on reopen port", e);
        }
    }

    @Override
    public void write(int b) throws IOException {
        try {
            if (!serialPort.writeInt(b)) {
                reopenPort();
                throw new IOException("Error on write");
            }
        } catch (SerialPortException e) {
            reopenPort();
            throw new IOException("Error on write: " + e.getExceptionType(), e);
        }
    }

    @Override
    public int read() throws IOException {
        try {
            int result = -1;
            if (serialPort.getInputBufferBytesCount() > 0) {
                result = serialPort.readBytes(1)[0];
            }
            return result;
        } catch (SerialPortException e) {
            throw new IOException("Error on read: " + e.getExceptionType(), e);
        }
    }

    @Override
    public int getInputStreamBufferSize() throws IOException {
        try {
            return serialPort.getInputBufferBytesCount();
        } catch (SerialPortException e) {
            throw new IOException("Error on getInputStreamBufferSize: " + e.getExceptionType(), e);
        }
    }

    @Override
    public void close() {
        closePort();
    }


}
