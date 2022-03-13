package ru.crystals.pos.fiscalprinter.mstar.core.connect;

import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.comportemulator.mstar.MstarCommand;
import ru.crystals.comportemulator.mstar.MstarRequestPacket;
import ru.crystals.comportemulator.mstar.MstarResponsePacket;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterCommunicationException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterExceptionType;
import ru.crystals.pos.fiscalprinter.mstar.MstarPluginConfig;
import ru.crystals.pos.fiscalprinter.mstar.core.ResBundleFiscalPrinterMstar;
import ru.crystals.pos.fiscalprinter.transport.mstar.DataPacket;
import ru.crystals.pos.utils.Timer;

public class MstarConnector extends BaseConnector {
    private static final Logger LOG = LoggerFactory.getLogger(MstarConnector.class);

    public static final long READ_TIME_OUT = 4000L;
    public static final long PING_TIME_OUT = 2000L;
    private static final int MIN_ANSWER_PACKET_LENGTH = 9;
    private static final long TIME_OUT_BETWEEN_BYTES = 200L;
    private static final int STX = 0x02;
    private static final int ETX = 0x03;
    private static final int ENQ = 0x05;
    private static final int ACK = 0x06;
    private static final int NAK = 0x15;
    private static final int MIN_PACKET_ID = 0x20;
    private static final int MAX_PACKET_ID = 0x3F;
    private static final long THREAD_SLEEP_TIME_MILLIS = 10;
    private int packetId = MIN_PACKET_ID;
    private CommandStack commandStack = new CommandStack();
    private MstarPluginConfig config;

    public MstarConnector() {
    }

    public void setParams(MstarPluginConfig config) {
        this.config = config;
    }

    private synchronized void reconnect() throws FiscalPrinterException {
        close();
        open(config.getPort(), config.getBaudRate());
    }

    public synchronized void connect() throws FiscalPrinterException {
        open(config.getPort(), config.getBaudRate());
    }

    @Override
    public void setPortSettings(SerialPort serialPort) throws UnsupportedCommOperationException {
        if (config.isUseFlowControl()) {
            serialPort.setFlowControlMode((SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT));
        }
    }

    @Override
    public void revertPortSettings(SerialPort serialPort) throws UnsupportedCommOperationException {
        if (config.isUseFlowControl()) {
            serialPort.setFlowControlMode((SerialPort.FLOWCONTROL_NONE));
        }
    }

    public DataPacket sendRequest(MstarCommand command) throws FiscalPrinterException {
        return sendRequest(command, null, false);
    }

    public DataPacket sendRequest(MstarCommand command, boolean isAsyncMode) throws FiscalPrinterException {
        return sendRequest(command, null, isAsyncMode);
    }

    public DataPacket sendRequest(MstarCommand command, DataPacket dataPacket) throws FiscalPrinterException {
        return sendRequest(command, dataPacket, false);
    }

    public synchronized DataPacket sendRequest(MstarCommand command, DataPacket dp, boolean isAsyncMode) throws FiscalPrinterException {
        int expectedPacketId = sendPacket(command.getCode(), dp);
        if (!isAsyncMode) {
            return readPacketByCommand(command.getCode(), command.getReadTimeOut(), command.getPingTimeOut(), expectedPacketId);
        }
        return new DataPacket();
    }

    public int sendPacket(int cmd, DataPacket dataPacket) throws FiscalPrinterException {
        int expectedPacketId = packetIdIncrementAndGet();
        MstarRequestPacket packet = new MstarRequestPacket((byte) expectedPacketId, MstarCommand.getCommandID(cmd), dataPacket);
        sendPacket(packet);
        return expectedPacketId;
    }

    private void sendPacket(MstarRequestPacket packet) throws FiscalPrinterException {
        try {
            byte[] packetByteArray = packet.toByteArray();
            if (getOutputStream() == null) {
                connect();
            }
            getOutputStream().write(packetByteArray);
            flushStreams();
            logDebugCommand(packet.toString());
            commandStack.addRequestPacket(packet);
        } catch (IOException e) {
            LOG.info("Error sendPacket for fiscal {}", getInn());
            logWarn(e);
            FiscalPrinterCommunicationException ex = new FiscalPrinterCommunicationException(ResBundleFiscalPrinterMstar.getString("ERROR_SEND_DATA"), MstarErrorMsg.getErrorType());
            ex.setErrorInn(getInn());
            reconnect();
            throw ex;
        }
    }

    private synchronized int packetIdIncrementAndGet() {
        packetId++;
        if (packetId > MAX_PACKET_ID) {
            packetId = MIN_PACKET_ID + 1;
        }
        return packetId;
    }

    public DataPacket readPacketByCommand(int cmd, int expectedPacketId) throws FiscalPrinterException {
        return readPacketByCommand(cmd, READ_TIME_OUT, PING_TIME_OUT, expectedPacketId);
    }

    public DataPacket readPacketByCommand(int expectedCMD, long readTimeOut, long pingTimeOut, int expectedPacketId) throws FiscalPrinterException {
        try {
            // если мы вдруг начнем грузить логотип (команда 0x17) и сохранять полученное изображение в памяти (0x18),
            // то нужно будет учесть двухэтапность этих команд:
            // сначала возвращается ACK, а не пакет с STX
            MstarResponsePacket mstarResponsePacket = commandStack.getResponsePacketBeforeRead(expectedPacketId);
            while (mstarResponsePacket == null) {
                if (waitForSTX(readTimeOut, pingTimeOut)) {
                    byte[] packet = readOutAllData();
                    mstarResponsePacket = new MstarResponsePacket(packet);
                    logDebugResponse(mstarResponsePacket);
                    mstarResponsePacket = commandStack.getResponsePacket(mstarResponsePacket, expectedPacketId);
                } else {
                    commandStack.markRequestAsLost(expectedPacketId);
                    FiscalPrinterException exception = new FiscalPrinterException("TimeOut read of STX");
                    exception.setExceptionType(FiscalPrinterExceptionType.TIMEOUT_EXPIRED);
                    throw exception;
                }
            }

            return mstarResponsePacket.getDataPacket();
        } catch (FiscalPrinterCommunicationException e) {
            LOG.error(e.getMessage() + " (ID: {}, Command: {})", expectedPacketId, MstarCommand.getCommandID(expectedCMD));
            throw new FiscalPrinterCommunicationException(e.getMessage(), MstarErrorMsg.getErrorType(), e);
        } catch (Exception e) {
            LOG.error("Error readPacketByCommand for fiscal {}", getInn());
            LOG.error(ResBundleFiscalPrinterMstar.getString("ERROR_READ_DATA") + " (ID: {}, Command: {})", expectedPacketId, MstarCommand.getCommandID(expectedCMD));
            FiscalPrinterCommunicationException ex = new FiscalPrinterCommunicationException(ResBundleFiscalPrinterMstar.getString("ERROR_READ_DATA"), MstarErrorMsg.getErrorType(), e);
            ex.setErrorInn(getInn());
            throw ex;
        }
    }

    /**
     * Ожидание получения начала пакета (STX) в течение заданного таймаута.
     */
    private boolean waitForSTX(long readTimeOut, long pingTimeout) throws Exception {
        Timer stxResponseTimer = new Timer("Waiting for STX", readTimeOut);
        Timer enqTimer = new Timer("Sleeping before ENQ-ACK", pingTimeout);
        int read;
        while (stxResponseTimer.isNotExpired()) {
            if (getInputStream().available() > 0) {
                read = getInputStream().read();
                if (read == STX) {
                    return true;
                } else {
                    LOG.debug("unexpected response (ignored)");
                    logDebugResponce(read);
                }
            } else {
                if (enqTimer.isExpired()) {
                    // Если мы уже долгое время ждем ответа, нужно проверить, что ФР не потерялся
                    PingStatus mstarOnline = isMstarOnline();
                    if (mstarOnline.isReceivedStx()) {
                        return true;
                    } else if (mstarOnline.isOnline()) {
                        enqTimer.restart();
                    }
                }
                sleep(THREAD_SLEEP_TIME_MILLIS);
            }
        }
        return false;
    }

    /**
     * Проверка наличия связи с фискальником
     *
     * @return PingStatus
     */
    public PingStatus isMstarOnline() {
        try {
            Timer enqTimer = new Timer("Sleeping between ENQ-ACK", TIME_OUT_BETWEEN_BYTES);
            int countOfAttempts = 3;
            for (int i = 0; i < countOfAttempts; i++) {
                enqTimer.restart();
                this.sendData((byte) ENQ);
                int read;
                while (enqTimer.isNotExpired()) {
                    if (getInputStream().available() > 0) {
                        read = getInputStream().read();
                        logDebugResponce(read);
                        if (read == ACK || read == NAK) {
                            return new PingStatus(true);
                        } else if (read == STX) {
                            // Если получен STX, значит ФР уже начал отправлять ответ на исходную команду
                            return new PingStatus(true, STX);
                        }
                    } else {
                        sleep(THREAD_SLEEP_TIME_MILLIS);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Error on checking Mstar is online ({})", e.getMessage());
        }
        return new PingStatus(false);
    }

    /**
     * Вычитывание всего пакета после получения STX
     */
    private byte[] readOutAllData() throws Exception {
        Timer nextByteTimer = new Timer("Waiting for next byte", TIME_OUT_BETWEEN_BYTES);
        ByteArrayOutputStream packet = new ByteArrayOutputStream();
        packet.write(STX);
        while (nextByteTimer.isNotExpired()) {
            if (getInputStream().available() > 0) {
                packet.write(getInputStream().read());
                if (isPacketComplete(packet)) {
                    if (isRecoverPacket(packet)) {
                        LOG.debug("Recover packet: ");
                        logDebugResponse(packet);
                        // Если получен пакет-ошибка, возвращаемый в случае сбоя отправки ответа кассе после восстановления связи,
                        // вычитать новый пакет и отдать его как настоящий
                        return readOutAllData();
                    } else {
                        // Для вычитывания ACK, если он есть после пакета
                        if (getInputStream().available() > 0) {
                            logDebugResponce((char) getInputStream().read());
                        }
                        return packet.toByteArray();
                    }
                }
            } else {
                sleep(THREAD_SLEEP_TIME_MILLIS);
            }
        }
        throw new Exception("TimeOut read of data");
    }

    private boolean isRecoverPacket(ByteArrayOutputStream packet) {
        return packet.toByteArray()[1] == MIN_PACKET_ID;
    }

    /**
     * Проверка что, полученный пакет имеет минимальную длину и содержит символ конца данных ETX в правильном месте:
     * STX (1 байт) + ID пакета (1) + Код команды (2) + Код ошибки (2) + Данные (0..) + ETX (1) + CRC (2)
     */
    private boolean isPacketComplete(ByteArrayOutputStream packet) {
        return (packet.size() >= MIN_ANSWER_PACKET_LENGTH) && (packet.toByteArray()[packet.size() - 3] == ETX);

    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            // do nothing
        }
    }
}
