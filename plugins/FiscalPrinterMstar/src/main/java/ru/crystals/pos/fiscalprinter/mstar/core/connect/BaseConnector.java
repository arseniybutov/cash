package ru.crystals.pos.fiscalprinter.mstar.core.connect;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import org.slf4j.LoggerFactory;
import ru.crystals.comportemulator.mstar.MstarResponsePacket;
import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.fiscalprinter.ResBundleFiscalPrinter;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterCommunicationException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterConfigException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterOpenPortException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public abstract class BaseConnector implements AutoCloseable {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(BaseConnector.class);

    private static final String ERROR_CLOSE_STREAM_MSG = "Error while close streams";
    private static final int THREAD_SLEEP_TIME = 5;

    private String inn;
    private InputStream is = null;
    private BufferedInputStream bis = null;
    private OutputStream os = null;
    private BufferedOutputStream bos = null;
    private SerialPort serialPort = null;
    private int dataBits = SerialPort.DATABITS_8;
    private int stopBits = SerialPort.STOPBITS_1;
    private int parity = SerialPort.PARITY_NONE;
    private long timeOut = 30000;

    public static String bytesToHex(byte[] bytes) {
        return bytesToHex(bytes, bytes.length);
    }

    public static String bytesToHex(byte[] bytes, int read) {
        String result = null;
        String hex;
        if (bytes != null) {
            StringBuilder resultBuilder = new StringBuilder();
            for (int j = 0; j < read; j++) {
                int v = bytes[j] & 0xFF;
                hex = Integer.toHexString(v);
                if (hex.length() == 1) {
                    hex = "0" + hex;
                }
                resultBuilder.append(",").append(hex);
            }
            result = resultBuilder.toString();

            if (result.length() > 0) {
                result = result.substring(1, result.length());
            }
        }
        return result;
    }

    public static void logWarn(Exception e) {
        LOG.warn("", e);
    }

    public static void logWarn(String str, Exception e) {
        LOG.warn(str, e);
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public String getInn() {
        return inn;
    }

    protected void open(String portName, String baudRate) throws FiscalPrinterException {
        try {
            if (serialPort != null) {
                close();
            }

            refreshRxtxPorts();
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
            if (portIdentifier.isCurrentlyOwned()) {
                throw new FiscalPrinterConfigException("Port " + portName + " is busy", CashErrorType.FATAL_ERROR);
            }
            serialPort = (SerialPort) portIdentifier.open(this.getClass().getName(), 2000);
            serialPort.setSerialPortParams(Integer.valueOf(baudRate), dataBits, stopBits, parity);
            setPortSettings(serialPort);

            // создание потоков
            is = serialPort.getInputStream();
            bis = new BufferedInputStream(is);
            os = serialPort.getOutputStream();
            bos = new BufferedOutputStream(os);
        } catch (Exception e) {
            LOG.error("", e);
            throw new FiscalPrinterOpenPortException(ResBundleFiscalPrinter.getString("ERROR_OPEN_PORT"), CashErrorType.FATAL_ERROR);
        }
    }

    /**
     * Вызов этого метода актуализирует информацию о портах в RXTX. Его необходимо вызывать перед CommPortIdentifier.getPortIdentifier(portName)
     * Иначе, если порт portName перестанет существовать, этот вызов уничтожит текущую JVM.
     */
    private void refreshRxtxPorts() {
        CommPortIdentifier.getPortIdentifiers();
    }

    @Override
    public void close() {
        closeInputStream();
        closeOutputStream();
        closeSerialPort();
    }

    private void closeInputStream() {
        try {
            BufferedInputStream input = bis;
            bis = null;
            if (input != null) {
                input.close();
            }
        } catch (Exception e) {
            LOG.error(ERROR_CLOSE_STREAM_MSG, e);
        } finally {
            is = null;
        }
    }

    private void closeOutputStream() {
        try {
            BufferedOutputStream output = bos;
            bos = null;
            if (output != null) {
                output.close();
            }
        } catch (Exception e) {
            LOG.error(ERROR_CLOSE_STREAM_MSG, e);
        } finally {
            os = null;
        }
    }

    private void closeSerialPort() {
        try {
            revertPortSettings(serialPort);
        } catch (Exception e) {
            LOG.error(ERROR_CLOSE_STREAM_MSG, e);
        }
        try {
            serialPort.close();
        } catch (Exception e) {
            LOG.error(ERROR_CLOSE_STREAM_MSG, e);
        }
    }

    public void sendData(byte data) throws FiscalPrinterException {
        logDebug(" sendData ");
        try {
            cleanInStream();
            os.write(data);
            os.flush();
            logDebugCommand(data);
        } catch (IOException e) {
            processSendIOException(e);
        }
    }

    public void sendData(int data) throws FiscalPrinterException {
        logDebug(" sendData ");
        try {
            cleanInStream();
            os.write(data);
            os.flush();
            logDebugCommand(data);
        } catch (IOException e) {
            processSendIOException(e);
        }
    }

    public void sendData(byte[] data) throws FiscalPrinterException {
        logDebug(" sendData ");
        try {
            cleanInStream();
            os.write(data);
            os.flush();
        } catch (IOException e) {
            processSendIOException(e);
        }
        logDebugCommand(data);
    }

    public void sendDataWaitingDSR(byte[] data) throws FiscalPrinterException {
        long startTime = System.currentTimeMillis();

        logDebug(" sendDataWaitingDSR ");
        try {
            cleanInStream();
            while (!Thread.currentThread().isInterrupted()) {
                if (serialPort.isDSR()) {
                    os.write(data);
                    os.flush();
                    break;
                }
                checkTimeOut(timeOut, startTime, ResBundleFiscalPrinter.getString("ERROR_SEND_DATA_TIMEOUT"));
                Thread.sleep(THREAD_SLEEP_TIME);
            }
        } catch (IOException e) {
            processSendIOException(e);
        } catch (InterruptedException e) {
            LOG.error("", e);
        }
        logDebugCommand(data);
    }

    public void flushStreams() throws IOException {
        bos.flush();
        os.flush();
    }

    public byte[] readData(int len) throws FiscalPrinterException {
        return readData(len, null);
    }


    public byte[] readData(int len, Integer errLen) throws FiscalPrinterException {
        long startTime = System.currentTimeMillis();
        ByteArrayOutputStream buf = new ByteArrayOutputStream();

        logDebug(" readData ");
        try {
            while (!Thread.currentThread().isInterrupted()) {// ждем пока появится ответ
                while (is.available() > 0 && len != 0) {
                    int b = is.read();
                    if (b != -1) {
                        buf.write(b);
                        len--;
                        if (errLen != null)
                            errLen--;
                    }
                }
                if (len == 0) {
                    break;
                } else {
                    try {
                        checkTimeOut(timeOut, startTime, ResBundleFiscalPrinter.getString("ERROR_READ_DATA_TIMEOUT")); // выход по таймауту
                    } catch (FiscalPrinterException e) {
                        if (errLen == null || errLen != 0) {
                            throw e;
                        } else {
                            break;
                        }
                    }
                }
                Thread.sleep(THREAD_SLEEP_TIME);
            }
        } catch (IOException e) {
            processReadIOException(e);
        } catch (InterruptedException e) {
            LOG.error("", e);
        }
        logDebugResponce(buf.toByteArray());
        return buf.toByteArray();
    }

    public int readData() throws FiscalPrinterException {
        long startTime = System.currentTimeMillis();
        logDebug(" readData ");
        int data = 0;
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (is.available() > 0) {
                    data = bis.read();
                    break;
                } else {
                    checkTimeOut(timeOut, startTime, ResBundleFiscalPrinter.getString("ERROR_READ_DATA_TIMEOUT"));// выход по таймауту
                }
                Thread.sleep(THREAD_SLEEP_TIME);
            }
        } catch (IOException e) {
            processReadIOException(e);
        } catch (InterruptedException e) {
            LOG.error("", e);
        }
        logDebugResponce(data);
        return data;
    }

    public abstract void setPortSettings(SerialPort serialPort) throws UnsupportedCommOperationException;

    public abstract void revertPortSettings(SerialPort serialPort) throws UnsupportedCommOperationException;

    /**
     * Очищение входного потока
     */
    private void cleanInStream() throws IOException {
        StringBuilder garbage = new StringBuilder();
        while (is.available() > 0) {
            garbage.append(String.format("0x%X ", is.read()));
            garbage.append(',');
        }
        if (garbage.length() > 0) {
            if (garbage.charAt(garbage.length() - 1) == ',')
                garbage.deleteCharAt(garbage.length() - 1);
            logDebug("InStream garbage: " + Arrays.toString(garbage.toString().getBytes()));
        }
    }

    private void checkTimeOut(long timeOut, long startTime, String error) throws FiscalPrinterException {
        if (System.currentTimeMillis() - startTime > timeOut) {
            throw new FiscalPrinterException(error, CashErrorType.FISCAL_ERROR);
        }
    }

    private void processSendIOException(IOException e) throws FiscalPrinterException {
        logWarn(e);
        throw new FiscalPrinterCommunicationException(ResBundleFiscalPrinter.getString("ERROR_SEND_DATA"), CashErrorType.FISCAL_ERROR);
    }

    private void processReadIOException(IOException e) throws FiscalPrinterException {
        logWarn(e);
        throw new FiscalPrinterCommunicationException(ResBundleFiscalPrinter.getString("ERROR_READ_DATA"), CashErrorType.FISCAL_ERROR);
    }

    // методы для установки настроек порта
    public void setDataBits(int dataBits) {
        this.dataBits = dataBits;
    }

    public void setStopBits(int stopBits) {
        this.stopBits = stopBits;
    }

    public void setParity(int parity) {
        this.parity = parity;
    }

    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }

    // методы для установки настроек порта
    public SerialPort getSerialPort() {
        return serialPort;
    }

    public InputStream getInputStream() {
        return bis;
    }

    public OutputStream getOutputStream() {
        return bos;
    }

    public void logDebugRawResponce(byte[] text) {
        LOG.debug("<-- {}", text == null ? null : bytesToHex(text));
    }

    public void logDebug(String text) {
        LOG.debug(text);
    }

    public void logDebugCommand(String text) {
        LOG.debug("--> {}", convertDebugLog(text));
    }

    public void logDebugCommand(byte[] command) {
        LOG.debug("--> {}", getForLogBytesAsString(command));
    }

    public void logDebugCommand(byte command) {
        LOG.debug("--> {}", String.format("0x%X ", command));
    }

    public void logDebugCommand(int command) {
        LOG.debug("--> {}", String.format("0x%X ", command));
    }

    public void logDebugResponce(StringBuilder text) {
        LOG.debug("<-- {}", text == null ? null : convertDebugLog(text.toString()));
    }

    public void logDebugResponce(byte[] responce) {
        LOG.debug("<-- {}", getForLogBytesAsString(responce));
    }

    public void logDebugResponce(int responce) {
        LOG.debug("<-- {}", String.format("0x%X ", responce));
    }

    public void logDebugResponse(MstarResponsePacket packet) {
        LOG.debug("<-- {}", packet);
    }

    public void logDebugResponse(ByteArrayOutputStream bao) {
        LOG.debug("<-- {}", bao);
    }

    private String getForLogBytesAsString(byte[] command) {
        StringBuilder result = new StringBuilder();
        for (byte i : command) {
            result.append(String.format("0x%X ", i));
        }
        return result.toString();
    }

    private String convertDebugLog(String text) {
        if (LOG.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            for (Character c : text.toCharArray()) {
                if (c > 255 || c.toString().matches("[\\p{Print}]"))
                    sb.append(c);
                else
                    sb.append(String.format("{%02X}", Integer.valueOf(c)));
            }
            return sb.toString();
        } else
            return text;
    }
}
