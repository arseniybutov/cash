package ru.crystals.pos.bank.ucs.connectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.TerminalConfiguration;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.ucs.Timeouts;
import ru.crystals.pos.bank.ucs.exceptions.CorruptDataException;
import ru.crystals.pos.bank.ucs.exceptions.DataTimeoutException;
import ru.crystals.pos.bank.ucs.exceptions.NoAckException;
import ru.crystals.pos.bank.ucs.messages.requests.Request;
import ru.crystals.pos.bank.ucs.messages.responses.ResponseType;
import ru.crystals.pos.bank.ucs.utils.Timer;
import ru.crystals.pos.utils.PortAdapter;
import ru.crystals.pos.utils.SerialPortAdapter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class RS232Connector implements Connector {
    private static final String TERMINAL_CHARSET = "cp1251";
    private static Logger log = LoggerFactory.getLogger(RS232Connector.class);
    private static final int STX = 0x02;
    private static final int ETX = 0x03;
    private static final int EOT = 0x04;
    private static final int ACK = 0x06;
    private static final int ENQ = 0x05;
    private static final int DLE = 0x10;
    private static final int NAK = 0x15;
    private static final int COMMAND_FIELD_LENGTH = 2;
    private static final int TERMINAL_FIELD_LENGTH = 10;
    private static final int LENGTH_FIELD_LENGTH = 2;
    private static final int MIN_MESSAGE_LENGTH = COMMAND_FIELD_LENGTH + TERMINAL_FIELD_LENGTH + LENGTH_FIELD_LENGTH;
    private SerialPortAdapter port;
    private Timeouts timeouts = new Timeouts();

    public RS232Connector() {
        port = new SerialPortAdapter();
    }

    protected boolean waitForENQ(long timeout) throws IOException {
        Timer enqResponseTimer = new Timer("Waiting for ENQ", timeout);
        log.trace("Waiting for ENQ");
        int read = -1;
        while (!enqResponseTimer.isExpired()) {
            read = port.read();
            if (read == ENQ) {
                log.debug(in("ENQ"));
                writeACK();
                return true;
            } else if (read == -1) {
                sleep(10);
            } else {
                log.debug(in(intToString(read) + " (unexpected)"));
            }
        }
        return false;
    }

    protected boolean waitForDLESTX() throws IOException {
        boolean isDLE = false;

        log.trace("Waiting for DLE STX");
        Timer dataStartReceiveTimer = new Timer("Data start receive", timeouts.getDataStartReceiveTimeout());
        Timer maxTimeBetweenBytes = new Timer(timeouts.getMaxTimeBetweenBytes());
        while (!dataStartReceiveTimer.isExpired()) {
            int read = port.read();
            if (!isDLE) {
                if (read == DLE) {
                    log.debug(in("DLE"));
                    isDLE = true;
                    maxTimeBetweenBytes.restart();
                } else if (read == -1) {

                } else {
                    log.debug(in(intToString(read) + " (unexpected)"));
                }
            } else {
                if (read == STX) {
                    log.debug(in("STX"));
                    return true;
                } else if (read == -1) {
                    if (maxTimeBetweenBytes.isExpired()) {
                        log.trace("Max time between bytes ({}) expired", timeouts.getMaxTimeBetweenBytes());
                        return false;
                    }
                } else {
                    log.debug(in(intToString(read) + " (unexpected)"));
                    return false;
                }
            }
            sleep(10);
        }
        return false;
    }

    @Override
    public String waitAndReadResponse(long timeout) throws BankException {
        try {
            if (waitForENQ(timeout)) {
                for (int i = 0; i < 3; i++) {
                    if (waitForDLESTX()) {
                        try {
                            String strResponse = readAnswer();
                            writeACK();
                            waitForEOT();
                            return strResponse;
                        } catch (CorruptDataException cde) {
                            writeNAK();
                        }
                    }
                }
                finishRequest();
                throw new BankCommunicationException("No data received");
            }
            throw new BankCommunicationException("No ENQ within timeout (" + timeout + ")");
        } catch (IOException e) {
            throw new BankCommunicationException(e.getMessage());
        }
    }

    protected void waitForEOT() throws IOException {
        Timer eotTimer = new Timer("Waiting for EOT", timeouts.getEOTTimeout());
        while (!eotTimer.isExpired()) {
            int result = port.read();
            if (result == EOT) {
                log.debug(in("EOT"));
                return;
            } else if (result == -1) {
                sleep(10);
            } else {
                log.debug(in(intToString(result) + " (unexpected)"));
            }
        }
        log.debug("No EOT in 100 ms");
    }

    protected void writeACK() throws IOException {
        log.debug(out("ACK"));
        port.write(ACK);
    }

    protected void writeNAK() throws IOException {
        log.debug(out("NAK"));
        port.write(NAK);
    }

    protected String readAnswer() throws IOException {
        boolean isDLE = false;
        boolean isETX = false;
        ByteArrayOutputStream answer = new ByteArrayOutputStream();
        int read = -1;
        Timer timeBetween = new Timer(timeouts.getMaxTimeBetweenBytes());
        String result = null;
        while (!timeBetween.isExpired()) {
            read = readFrom();
            if (read == -1) {
                sleep(10);
                continue;
            }
            timeBetween.restart();
            if (isDLE && isETX) {
                log.debug(in("LRC " + intToString(read)));
                answer.close();
                if ((checkLengthIsCorrect(result) && checkSumIsValid(answer.toByteArray(), read)) ||
                        StringUtils.startsWith(result, ResponseType.CONSOLE_MESSAGE.getClassAndCode()) ||
                        StringUtils.startsWith(result, ResponseType.PRINT_LINE.getClassAndCode())) {
                    return result;
                } else {
                    throw new CorruptDataException("Invalid checksum or length");
                }
            } else if (read == DLE || read == ETX) {
                if (read == DLE) {
                    result = answer.toString(TERMINAL_CHARSET);
                    log.debug(in(result));
                    log.debug(in("DLE"));
                } else {
                    log.debug(in("ETX"));
                }
                if (read == ETX && !isDLE) {
                    answer.close();
                    throw new CorruptDataException("Invalid sequence of bytes: ETX without DLE");
                }
                isDLE = (read == DLE || isDLE);
                isETX = (read == ETX);
            } else {
                answer.write(read);
            }
        }
        log.trace("Max time between bytes ({}) expired", timeouts.getMaxTimeBetweenBytes());
        throw new DataTimeoutException("Time between bytes is expired");
    }

    protected boolean checkLengthIsCorrect(String message) {
        if (message == null || message.length() < MIN_MESSAGE_LENGTH) {
            log.warn("Invalid total message length {} (< MIN_MESSAGE_LENGTH({}))", (message == null ? "null" : message.length()), MIN_MESSAGE_LENGTH);
            return false;
        }
        String declaredLengthString = message.substring(MIN_MESSAGE_LENGTH - LENGTH_FIELD_LENGTH, MIN_MESSAGE_LENGTH);
        try {
            int declaredLength = Integer.parseInt(declaredLengthString, 16);
            int expectedLength = message.length() - MIN_MESSAGE_LENGTH;
            if (expectedLength != declaredLength) {
                log.warn("Invalid length {} (declared {})", intToString(expectedLength), intToString(declaredLength));
                return false;
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid length {} (not an integer)", declaredLengthString);
            return false;
        }
        return true;
    }

    protected boolean checkSumIsValid(byte[] message, int declaredLRC) {
        int expectedLRC = calculateLrc(message);
        if (expectedLRC != declaredLRC) {
            log.warn("Invalid LRC {} (expected {})", intToString(declaredLRC), intToString(expectedLRC));
            return false;
        }
        return true;
    }

    protected void writeHead() throws IOException {
        log.debug(out("DLE"));
        port.write(DLE);
        log.debug(out("STX"));
        port.write(STX);
    }

    protected void writeTail() throws IOException {
        log.debug(out("DLE"));
        port.write(DLE);
        log.debug(out("ETX"));
        port.write(ETX);
    }

    protected void writeLRC(String message) throws IOException {
        int lrc = calculateLrc(message);
        log.debug(out("LRC " + intToString(lrc)));
        port.write(lrc);
    }

    public void writeMessage(String message) throws IOException {
        log.debug(out(message));
        port.write(message);
    }

    public void sendMessage(String message) throws IOException {
        writeHead();
        writeMessage(message);
        writeTail();
        writeLRC(message);
    }

    @Override
    public void sendRequest(Request loginRequest) throws BankException {
        try {
            for (int i = 0; i < 3; i++) {
                sendMessage(loginRequest.toString());
                if (waitForACK()) {
                    finishRequest();
                    return;
                }
            }
            finishRequest();
            throw new NoAckException("No ACK received for 3 times");
        } catch (IOException e) {
            throw new BankException(e.getMessage());
        }
    }

    public void finishRequest() throws IOException {
        log.debug(out("EOT"));
        port.write(EOT);
    }

    @Override
    public boolean startSession() throws BankException {
        try {
            for (int i = 0; i < 3; i++) {
                writeENQ();
                if (waitForACK()) {
                    return true;
                }
            }
            finishRequest();
            return false;
        } catch (IOException e) {
            throw new BankException(e);
        }
    }

    public boolean waitForACK() throws IOException {
        int response;
        Timer ackTimer = new Timer("Wait for ACK", timeouts.getAckTimeout());
        while ((response = readFrom()) != ACK) {
            if (response == NAK) {
                log.debug(in("NAK"));
                return false;
            }
            if (ackTimer.isExpired()) {
                return false;
            }
            sleep(timeouts.getWhileSleepAckTimeout());
        }
        log.debug(in("ACK"));
        return true;
    }

    public void writeENQ() throws IOException {
        log.debug(out("ENQ"));
        port.write(ENQ);
    }

    private int readFrom() throws IOException {
        return port.read();
    }

    private static String in(String s) {
        return "<- " + s;
    }

    private static String out(String s) {
        return "-> " + s;
    }

    private static String intToString(int i) {
        return String.format("0x%02X", i & 0xFF);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignore) {
            //
        }
    }

    protected int calculateLrc(String expectedTerminalId) {
        if (expectedTerminalId == null) {
            return 0;
        }
        return calculateLrc(expectedTerminalId.getBytes());
    }

    protected int calculateLrc(byte[] bytes) {
        int lrc = 0;
        for (byte b : bytes) {
            lrc ^= b & 0xFF;
        }
        return lrc;
    }

    @Override
    public RS232Connector setPortAdapter(PortAdapter port) {
        this.port = (SerialPortAdapter) port;
        return this;
    }

    @Override
    public void init() throws BankException {
        try {
            port.openPort();
        } catch (Exception e) {
            throw new BankException("CONNECTION ERROR", e);
        }
    }

    @Override
    public void close() {
        port.close();
    }

    public Timeouts getTimeouts() {
        return timeouts;
    }

    public RS232Connector setTimeouts(Timeouts timeouts) {
        this.timeouts = timeouts;
        return this;
    }

    public SerialPortAdapter getPortAdapter() {
        return port;
    }

    @Override
    public void endSession() {
        //
    }

    @Override
    public Connector setTerminalConfiguration(TerminalConfiguration terminalConfiguration) {
        port.setPort(terminalConfiguration.getPort());
        port.setBaudRate(terminalConfiguration.getBaudRate());
        return this;
    }

    @Override
    public void openSession() {
        //
    }

    @Override
    public void closeSession() {
        //
    }
}
