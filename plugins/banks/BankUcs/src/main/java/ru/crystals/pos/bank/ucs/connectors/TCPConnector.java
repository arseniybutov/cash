package ru.crystals.pos.bank.ucs.connectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.TerminalConfiguration;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.ucs.ResBundleBankUcs;
import ru.crystals.pos.bank.ucs.messages.requests.Request;
import ru.crystals.pos.bank.ucs.utils.Timer;
import ru.crystals.pos.utils.PortAdapter;
import ru.crystals.pos.utils.TCPPortAdapter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TCPConnector implements Connector {

    private static final Logger log = LoggerFactory.getLogger(TCPConnector.class);

    private static final String TERMINAL_CHARSET = "cp1251";

    private static final int COMMAND_FIELD_LENGTH = 2;
    private static final int TERMINAL_FIELD_LENGTH = 10;
    private static final int LENGTH_FIELD_LENGTH = 2;
    private static final int MIN_MESSAGE_LENGTH = COMMAND_FIELD_LENGTH + TERMINAL_FIELD_LENGTH + LENGTH_FIELD_LENGTH;

    private TCPPortAdapter port = new TCPPortAdapter();

    @Override
    public String waitAndReadResponse(long timeout) throws BankException {
        Timer responseTimer = new Timer(timeout);
        String response;
        try {
            while (responseTimer.isNotExpired()) {
                response = readMessage();
                if (response != null) {
                    log.debug(in(response));
                    return response;
                } else {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ignore) {

                    }
                }
            }
            throw new BankCommunicationException("No answer in timeout");
        } catch (IOException e) {
            throw new BankCommunicationException(e.getMessage());
        }
    }

    private byte[] readBytesForSpecifiedLength(int length) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int countOfRead = 0;
        while (countOfRead < length && port.getInputStreamBufferSize() > 0) {
            baos.write(port.read());
            countOfRead++;
        }
        if (countOfRead == 0) {
            return new byte[0];
        }
        if (baos.size() != length) {
            baos.close();
            log.trace("Size of read bytes ({}) is not equal to expected ({})", baos.size(), length);
            throw new IOException("Size of read bytes (" + baos.size() + ") is not equal to expected (" + length + ")");
        }
        return baos.toByteArray();
    }

    protected String readMessage() throws IOException {
        String response;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        baos.write(readBytesForSpecifiedLength(MIN_MESSAGE_LENGTH));
        if (baos.size() == 0) {
            return null;
        }
        response = baos.toString(TERMINAL_CHARSET);
        String declaredLengthString = response.substring(MIN_MESSAGE_LENGTH - LENGTH_FIELD_LENGTH, MIN_MESSAGE_LENGTH);
        int declaredDataLength = 0;
        try {
            declaredDataLength = Integer.parseInt(declaredLengthString, 16);
        } catch (NumberFormatException e) {
            log.warn("Invalid length {} (not an integer)", declaredLengthString);
        }
        if (declaredDataLength == 0) {
            return response;
        }
        baos.write(readBytesForSpecifiedLength(declaredDataLength));
        return baos.toString(TERMINAL_CHARSET);
    }

    @Override
    public void sendRequest(Request request) throws BankException {
        try {
            writeMessage(request.toString());
        } catch (IOException e) {
            log.error("Unable to send request", e);
            throw new BankCommunicationException("Unable to send request");
        }
    }

    @Override
    public boolean startSession() {
        return true;
    }

    @Override
    public void close() {
        endSession();
    }

    @Override
    public void endSession() {
        port.close();
    }

    @Override
    public TCPConnector setPortAdapter(PortAdapter port) {
        this.port = (TCPPortAdapter) port;
        return this;
    }

    public void writeMessage(String message) throws IOException {
        log.debug(out(message));
        port.write(message);
    }

    private static String in(String s) {
        return "<- " + s;
    }

    private static String out(String s) {
        return "-> " + s;
    }

    @Override
    public Connector setTerminalConfiguration(TerminalConfiguration terminalConfiguration) {
        log.info(terminalConfiguration.toString());
        port.setTcpAddress(terminalConfiguration.getTerminalIp());
        port.setTcpPort(terminalConfiguration.getTerminalTcpPort());
        return this;
    }

    @Override
    public void openSession() throws BankException {
        try {
            port.openPort();
        } catch (Exception e) {
            log.error("Unable to open port", e);
            throw new BankCommunicationException(ResBundleBankUcs.getString("TERMINAL_TIMEOUT"));
        }
    }

    @Override
    public void closeSession() {
        port.close();
    }
}
