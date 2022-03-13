package ru.crystals.pos.fiscalprinter.pirit.core.connect;

import gnu.io.SerialPort;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.fiscalprinter.ResBundleFiscalPrinter;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterCommunicationException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterConfigException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterOpenPortException;
import ru.crystals.rxtxadapter.CommPortIdentifierSource;
import ru.crystals.rxtxadapter.CommPortIdentifierWrapper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class BaseConnector {

    private static final org.slf4j.Logger Log = LoggerFactory.getLogger(BaseConnector.class);

    private CommPortIdentifierSource commPortIdentifierSource;

    private InputStream is = null;

    private BufferedInputStream bis = null;

    private OutputStream os = null;

    private BufferedOutputStream bos = null;

    private SerialPort serialPort = null;

    private int dataBits = SerialPort.DATABITS_8;

    private int stopBits = SerialPort.STOPBITS_1;

    private int parity = SerialPort.PARITY_NONE;

    private String encoding = "cp866";

    protected String inn;

    public void setInn(String inn) {
        this.inn = inn;
    }

    protected void open(String portName, String baudRate) throws FiscalPrinterException {
        try {
            if (commPortIdentifierSource == null) {
                commPortIdentifierSource = new CommPortIdentifierSource();
            }

            if (serialPort != null) {
                close();
            }

            refreshRxtxPorts();
            CommPortIdentifierWrapper portIdentifier = commPortIdentifierSource.getPortIdentifier(portName);
            if (portIdentifier.isCurrentlyOwned()) {
                throw new FiscalPrinterConfigException("Port " + portName + " is busy", CashErrorType.FATAL_ERROR);
            }
            serialPort = (SerialPort) portIdentifier.open(this.getClass().getName(), 2000);
            serialPort.setSerialPortParams(Integer.parseInt(baudRate), dataBits, stopBits, parity);

            // создание потоков
            initStreams(serialPort.getInputStream(), serialPort.getOutputStream());
        } catch (Exception e) {
            Log.error("", e);
            throw new FiscalPrinterOpenPortException(ResBundleFiscalPrinter.getString("ERROR_OPEN_PORT"), CashErrorType.FATAL_ERROR);
        }
    }

    protected void initStreams(InputStream is, OutputStream os) {
        this.is = is;
        bis = new BufferedInputStream(is);
        this.os = os;
        bos = new BufferedOutputStream(os);
    }

    /**
     * Вызов этого метода актуализирует информацию о портах в RXTX. Его необходимо вызывать перед CommPortIdentifier.getPortIdentifier(portName)
     * Иначе, если порт portName перестанет существовать, этот вызов уничтожит текущую JVM.
     */
    private void refreshRxtxPorts() {
        commPortIdentifierSource.getPortIdentifiers();
    }

    public void close() {
        try {
            closeStreams();
        } catch (Exception e) {
            Log.error("Error while close streams", e);
        }

        try {
            serialPort.close();
        } catch (Exception e) {
            Log.error("Error while close streams", e);
        }
    }

    protected void closeStreams() throws IOException {
        is.close();
        bis.close();
        os.close();
        bos.close();
    }

    // sendData
    void sendData(byte data) throws FiscalPrinterException {
        Log.debug(" sendData ");
        try {
            os.write(data);
            os.flush();
            logDebugCommand(data);
        } catch (IOException e) {
            processSendIOException(e);
        }
    }

    void sendData(byte[] data) throws FiscalPrinterException {
        Log.debug(" sendData ");
        try {
            os.write(data);
            os.flush();
        } catch (IOException e) {
            processSendIOException(e);
        }
        logDebugCommand(data);
    }

    void flushStreams() throws IOException {
        bos.flush();
        os.flush();
    }

    private void processSendIOException(IOException e) throws FiscalPrinterException {
        Log.warn("", e);
        throw new FiscalPrinterCommunicationException(ResBundleFiscalPrinter.getString("ERROR_SEND_DATA"), CashErrorType.FISCAL_ERROR);
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

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getEncoding() {
        return encoding;
    }

    // методы для установки настроек порта
    public SerialPort getSerialPort() {
        return serialPort;
    }

    InputStream getInputStream() {
        return bis;
    }

    OutputStream getOutputStream() {
        return bos;
    }

    void logDebugRawResponce(byte[] text) {
        Log.debug("<-- {}", text == null ? null : bytesToHex(text));
    }

    private static String bytesToHex(byte[] bytes) {
        return bytesToHex(bytes, bytes.length);
    }

    private static String bytesToHex(byte[] bytes, int read) {
        String result = null;
        String hex;
        if (bytes != null) {
            result = "";
            for (int j = 0; j < read; j++) {
                int v = bytes[j] & 0xFF;
                hex = Integer.toHexString(v);
                if (hex.length() == 1) {
                    hex = "0" + hex;
                }
                result = result + "," + hex;
            }

            if (result.length() > 0) {
                result = result.substring(1);
            }
        }
        return result;
    }

    void logDebugCommand(String text) {
        Log.debug("--> {}", convertDebugLog(text));
    }

    private void logDebugCommand(byte[] data) {
        if (Log.isDebugEnabled()) {
            Log.debug("--> {}", getForLogBytesAsString(data));
        }
    }

    private void logDebugCommand(byte command) {
        Log.debug("--> {}", String.format("0x%X ", command));
    }

    public void logDebugCommand(int command) {
        Log.debug("--> {}", String.format("0x%X ", command));
    }

    public void logDebugResponse(StringBuilder text) {
        Log.debug("<-- {}", text == null ? null : convertDebugLog(text.toString()));
    }

    public void logDebugResponse(byte[] responce) {
        Log.debug("<-- {}", getForLogBytesAsString(responce));
    }

    void logDebugResponse(int responce) {
        Log.debug("<-- {}", String.format("0x%X ", responce));
    }

    void logDebugResponse(PiritResponsePacket packet) {
        Log.debug("<-- {}", packet);
    }

    void logDebugResponse(ByteArrayOutputStream bao) {
        Log.debug("<-- {}", bao);
    }

    private String getForLogBytesAsString(byte[] command) {
        StringBuilder result = new StringBuilder();
        for (byte i : command) {
            result.append(String.format("0x%X ", i));
        }
        return result.toString();
    }

    private String convertDebugLog(String text) {
        if (Log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            for (Character c : text.toCharArray()) {
                if (c > 255 || c.toString().matches("[\\p{Print}]")) {
                    sb.append(c);
                } else {
                    sb.append(String.format("{%02X}", Integer.valueOf(c)));
                }
            }
            return sb.toString();
        } else {
            return text;
        }
    }

    public void setCommPortIdentifierSource(CommPortIdentifierSource commPortIdentifierSource) {
        this.commPortIdentifierSource = commPortIdentifierSource;
    }
}
