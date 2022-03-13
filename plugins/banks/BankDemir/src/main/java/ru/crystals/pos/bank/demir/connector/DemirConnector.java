package ru.crystals.pos.bank.demir.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.ResBundleBank;
import ru.crystals.pos.bank.TerminalConfiguration;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.localization.CoreResBundle;
import ru.crystals.pos.utils.PortAdapterException;
import ru.crystals.pos.utils.SerialPortAdapter;
import ru.crystals.pos.utils.Timer;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class DemirConnector implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DemirConnector.class);

    private static final long OVERALL_TIMEOUT = TimeUnit.MINUTES.toMillis(3);
    private static final long ACK_TIMEOUT = TimeUnit.SECONDS.toMillis(5);
    private static final long READ_BYTE_TIMEOUT = 100;

    private static final String CHARSET = "ISO-8859-5";

    private static final byte STX = 0x02;
    private static final byte ETX = 0x03;
    private static final byte ACK = 0x06;
    private static final byte NAK = 0x15;

    private final SerialPortAdapter portAdapter;

    public DemirConnector(TerminalConfiguration config) {
        portAdapter = new SerialPortAdapter();
        portAdapter.setPort(config.getPort());
        portAdapter.setBaudRate(config.getBaudRate());
    }

    public void start() throws BankCommunicationException {
        try {
            portAdapter.openPort();
        } catch (IOException | PortAdapterException e) {
            throw new BankCommunicationException(CoreResBundle.getStringCommon("ERROR_OPEN_PORT"), e);
        }
    }

    @Override
    public void close() {
        portAdapter.close();
    }

    /**
     * Frame: STX [message/text] ETX LRC
     */
    private byte[] createFrame(byte[] command) {
        byte[] frame = new byte[command.length + 3];
        frame[0] = STX;
        System.arraycopy(command, 0, frame, 1, command.length);
        frame[command.length + 1] = ETX;
        frame[frame.length - 1] = calcLrc(frame);
        return frame;
    }

    private static byte calcLrc(byte[] bytes) {
        byte lrc = 0;
        // пропускаем STX в начале сообщения и LRC в конце
        for (int i = 1; i < bytes.length - 1; i++) {
            lrc ^= bytes[i] & 0xFF;
        }
        return lrc;
    }

    private boolean checkLrc(byte[] response) {
        return response[response.length - 1] == calcLrc(response);
    }

    public String sendCommand(byte[] command) throws BankCommunicationException {
        try {
            if (LOG.isTraceEnabled()) {
                LOG.trace(new String(command, CHARSET));
            }
            portAdapter.write(createFrame(command));
            boolean ack = waitForAck();
            if (!ack) {
                throw new BankException(ResBundleBank.getString("TERMINAL_COMMUNICATION_ERROR"));
            }
            byte[] response = portAdapter.readBytes(OVERALL_TIMEOUT, READ_BYTE_TIMEOUT);
            if (!checkLrc(response)) {
                LOG.error("Response LRC is incorrect");
                portAdapter.write(NAK);
                throw new BankCommunicationException();
            }
            portAdapter.write(ACK);
            // откидываем STX ETX LRC
            byte[] responseMessage = new byte[response.length - 3];
            System.arraycopy(response, 1, responseMessage, 0, response.length - 3);
            String responseStr = new String(responseMessage, CHARSET);
            LOG.trace(responseStr);
            return responseStr;
        } catch (Exception e) {
            throw new BankCommunicationException(ResBundleBank.getString("TERMINAL_COMMUNICATION_ERROR"), e);
        }
    }

    private boolean waitForAck() throws Exception {
        Timer timer = new Timer(ACK_TIMEOUT);
        LOG.trace("waiting for ack");
        while (!Thread.interrupted() && timer.isNotExpired()) {
            if (!portAdapter.isDataAvailable()) {
                TimeUnit.MILLISECONDS.sleep(READ_BYTE_TIMEOUT);
                continue;
            }
            byte first = (byte) portAdapter.read();
            LOG.trace("received response");
            return first == ACK;
        }
        throw new BankCommunicationException(ResBundleBank.getString("TERMINAL_TIMEOUT"));
    }
}
