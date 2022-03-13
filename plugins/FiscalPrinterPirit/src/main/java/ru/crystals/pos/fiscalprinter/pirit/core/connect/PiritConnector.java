package ru.crystals.pos.fiscalprinter.pirit.core.connect;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.comportemulator.pirit.PiritCommand;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterCommunicationException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterExceptionType;
import ru.crystals.pos.fiscalprinter.pirit.core.ResBundleFiscalPrinterPirit;
import ru.crystals.pos.fiscalprinter.transport.DataPacket;
import ru.crystals.utils.time.Timer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;

public class PiritConnector extends BaseConnector implements AutoCloseable {

    protected static final Logger log = LoggerFactory.getLogger(PiritConnector.class);
    static final int STX = 0x02;
    static final int ETX = 0x03;
    private static final int ENQ = 0x05;
    private static final int ACK = 0x06;
    private static final int NAK = 0x15;
    static final String FS_STR = "\u001C";
    static final Charset ENCODING = Charset.forName("cp866");
    static final byte[] PASSWORD_BYTES = "PIRI".getBytes();

    public static final int MIN_ANSWER_PACKET_LENGTH = 9;
    public static final long READ_TIME_OUT = 10000L;
    private static final Duration READ_ACK_TIME_OUT =  Duration.ofSeconds(2);
    public static final long PING_TIME_OUT = 5000L;
    private static final Duration TIME_OUT_BETWEEN_BYTES = Duration.ofMillis(200);
    public static final int MAX_PING_ATTEMPTS = 3;

    private static final int MIN_PACKET_ID = 0x20;
    private static final int MAX_PACKET_ID = 0x3F;
    private int packetId = MIN_PACKET_ID;

    /**
     * Текущий счетчика отправленных аиснхронных команд (команды в пакетном режиме, ответ на которые не требуется,
     * например, печать позиции или текста)
     */
    private long sentAsyncCommandCounter;

    private static final long THREAD_SLEEP_TIME_MILLIS = 10;

    /**
     * Количество отправленных асинхронных команд, после которого нужно дождаться завершения обработки
     */
    private long maxAsyncCommandBuffer = 20;
    private CommandStack commandStack = new CommandStack();

    private String portName;
    private String baudeRate;

    public PiritConnector() {
    }

    public PiritConnector(String portName, String baudeRate) {
        this.portName = portName;
        this.baudeRate = baudeRate;
    }

    public void setParams(String portName, String baudeRate) {
        this.portName = portName;
        this.baudeRate = baudeRate;
    }

    public synchronized void reconnect() throws FiscalPrinterException {
        open(portName, baudeRate);
    }

    /**
     * Отправляет в устройство двухэтапные команды по загрузке картинок или других бинарных данных.
     * Обший алгоритм
     * 1. Отправляем команду с метаданными (например, размер изображения, размер данных)
     * 2. Ожидаем в ответ ACK о готовности принимать сами данные или стандартный ответ с кодом ошибки
     * 3. Отправляем сырые данные и ждем итоговый стандартный ответ с кодом ошибки
     *
     * @param command    Команда, работающая с бинарными данными
     * @param metaData   пакет с метаданными
     * @param binaryData сырые бинарные данные
     * @return пакет с ответом
     * @throws FiscalPrinterException в случае неполучения ответа за таймаут или других проблем
     */
    public DataPacket sendBinaryRequest(PiritCommand command, DataPacket metaData, byte[] binaryData) throws FiscalPrinterException {
        waitForPrintingEnd();
        PacketId expectedPacketId = sendPacket(command, metaData, false);
        readAckByCommand(expectedPacketId);
        sendData(binaryData);
        return readPacketByCommand(expectedPacketId, command.getReadTimeOut(), command.getPingTimeOut());
    }

    public DataPacket sendRequest(PiritCommand command) throws FiscalPrinterException {
        return sendRequest(command, null);
    }

    public DataPacket sendRequest(PiritCommand command, boolean isAsyncMode) throws FiscalPrinterException {
        return sendRequest(command, null, isAsyncMode);
    }

    public DataPacket sendRequest(PiritCommand command, DataPacket dataPacket) throws FiscalPrinterException {
        return sendRequest(command, dataPacket, false);
    }

    public DataPacket sendRequest(ExtendedCommand command) throws FiscalPrinterException {
        return sendRequest(command, null);
    }

    public DataPacket sendRequest(ExtendedCommand command, DataPacket dataPacket) throws FiscalPrinterException {
        return sendRequest(command, dataPacket, false);
    }

    public synchronized DataPacket sendRequest(PiritCommand command, DataPacket dp, boolean isAsyncMode) throws FiscalPrinterException {
        PacketId expectedPacketId = sendPacket(command, dp, isAsyncMode);
        if (!isAsyncMode) {
            return readPacketByCommand(expectedPacketId, command.getReadTimeOut(), command.getPingTimeOut());
        }
        return DataPacket.EMPTY;
    }

    public synchronized DataPacket sendRequest(ExtendedCommand command, DataPacket dp, boolean isAsyncMode) throws FiscalPrinterException {
        PacketId expectedPacketId = sendPacket(command, dp, isAsyncMode);
        if (!isAsyncMode) {
            return readPacketByCommand(expectedPacketId, command.getCmd().getReadTimeOut(), command.getCmd().getPingTimeOut());
        }
        return DataPacket.EMPTY;
    }

    public synchronized byte[] sendRequestForData(PiritCommand command, DataPacket dp) throws FiscalPrinterException {
        sendPacket(command, dp, false);
        return readPacketByCommandForData(command.getReadTimeOut());
    }

    private PacketId sendPacket(PiritCommand cmd, DataPacket dataPacket, boolean isAsyncMode) throws FiscalPrinterException {
        if (cmd.isShouldUseAsExtended()) {
            throw new IllegalArgumentException(String.format("You have to use command %s (with sub command) through %s", cmd, ExtendedCommand.class.getSimpleName()));
        }
        checkAsyncCommandBuffer(isAsyncMode);
        int expectedPacketId = packetIdIncrementAndGet();
        PiritRequestPacket packet = new PiritRequestPacket(expectedPacketId, cmd, dataPacket);
        sendPacket(packet);
        return packet.getPacketId();
    }

    private PacketId sendPacket(ExtendedCommand cmd, DataPacket dataPacket, boolean isAsyncMode) throws FiscalPrinterException {
        checkAsyncCommandBuffer(isAsyncMode);
        int expectedPacketId = packetIdIncrementAndGet();
        PiritRequestPacket packet = new PiritRequestPacket(expectedPacketId, cmd, dataPacket);
        sendPacket(packet);
        return packet.getPacketId();
    }

    private void sendPacket(PiritRequestPacket packet) throws FiscalPrinterException {
        try {
            byte[] packetByteArray = packet.toByteArray();
            getOutputStream().write(packetByteArray);
            flushStreams();
            logDebugCommand(packet.toString());
            commandStack.addRequestPacket(packet);
        } catch (IOException e) {
            log.warn("Error sendPacket for fiscal {}", inn, e);
            FiscalPrinterCommunicationException ex = new FiscalPrinterCommunicationException(ResBundleFiscalPrinterPirit.getString("ERROR_SEND_DATA"),
                    PiritErrorMsg.getErrorType());
            ex.setErrorInn(inn);
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

    /**
     * Проверяем перед отправкой команды, не пора ли защититься от переполнения буфера принтера при большом количестве переданных асинхронных команд,
     * и защищаемся, если пора.
     *
     * @param isAsyncMode режим, в котором планируем отправить следующую команду
     */
    private void checkAsyncCommandBuffer(boolean isAsyncMode) throws FiscalPrinterException {
        if (!isAsyncMode) {
            sentAsyncCommandCounter = 0;
            return;
        }
        sentAsyncCommandCounter++;
        if (sentAsyncCommandCounter >= maxAsyncCommandBuffer) {
            waitForPrintingEnd();
        }
    }

    /**
     * Дождаться окончания печати команд, переданных в пакетном режиме
     * <p>
     * Актуально вызывать
     * - перед командой с передачей бинарных данных (печать/загрузка картинок и т. п.)
     * - после отправки определенного количества команд в пакетном режим (для защиты от переполнения буфера принтера)
     */
    private void waitForPrintingEnd() throws FiscalPrinterException {
        sentAsyncCommandCounter = 0;
        sendRequest(PiritCommand.GET_STATUS_FOR_NORMALIZE);
    }

    private DataPacket readPacketByCommand(PacketId expectedPacketId, long readTimeOut, long pingTimeOut) throws FiscalPrinterException {
        try {
            PiritResponsePacket piritResponsePacket = commandStack.getResponsePacketBeforeRead(expectedPacketId);
            while (piritResponsePacket == null) {
                if (waitForSTX(readTimeOut, pingTimeOut)) {
                    byte[] packet = readOutAllData();
                    piritResponsePacket = new PiritResponsePacket(packet);
                    logDebugResponse(piritResponsePacket);
                    piritResponsePacket = commandStack.getResponsePacket(piritResponsePacket, expectedPacketId);
                } else {
                    commandStack.markRequestAsLost(expectedPacketId);
                    FiscalPrinterException exception = new FiscalPrinterException("TimeOut read of STX");
                    exception.setExceptionType(FiscalPrinterExceptionType.TIMEOUT_EXPIRED);
                    throw exception;
                }
            }

            return piritResponsePacket.getData();
        } catch (FiscalPrinterCommunicationException e) {
            log.error("Error: {} (ID: {})", e.getMessage(), expectedPacketId);
            throw new FiscalPrinterCommunicationException(e.getMessage(), PiritErrorMsg.getErrorType(), e);
        } catch (Exception e) {
            throw logAndMakeFPCException(expectedPacketId, e);
        } finally {
            sentAsyncCommandCounter = 0;
        }
    }

    private FiscalPrinterCommunicationException logAndMakeFPCException(PacketId expectedPacketId, Exception e) {
        log.error("Error readPacketByCommand for fiscal {}", inn);
        String readDataErrorMessage = ResBundleFiscalPrinterPirit.getString("ERROR_READ_DATA");
        log.error("Error: {} (ID: {})", readDataErrorMessage, expectedPacketId);
        FiscalPrinterCommunicationException ex = new FiscalPrinterCommunicationException(readDataErrorMessage, PiritErrorMsg.getErrorType(), e);
        ex.setErrorInn(inn);
        return ex;
    }

    /**
     * Ожидание получения начала пакета (STX) в течение заданного таймаута.
     */
    private boolean waitForSTX(long readTimeOut, long pingTimeout) throws Exception {
        Timer stxResponseTimer = Timer.ofMillis(readTimeOut);
        Timer enqTimer = Timer.ofMillis(pingTimeout);
        int read;
        while (stxResponseTimer.isNotExpired()) {
            if (isInputDataAvailable()) {
                read = getInputStream().read();
                if (read == STX) {
                    return true;
                } else {
                    log.debug("unexpected response (ignored)");
                    logDebugResponse(read);
                }
            } else {
                if (enqTimer.isExpired()) {
                    // Если мы уже долгое время ждем ответа, нужно проверить, что Пирит не потерялся
                    PingStatus piritOnline = isPiritStillOnline();
                    if (piritOnline.isReceivedStx()) {
                        return true;
                    } else if (piritOnline.isOnline()) {
                        enqTimer.restart();
                    }
                }
                sleep(THREAD_SLEEP_TIME_MILLIS);
            }
        }
        return false;
    }

    /**
     * Ожидаем в ответ ACK о готовности принимать данные для двухэтапной команды или стандартный ответ с кодом ошибки (отличной от 0)
     * <p>
     * Ситуация, когда нам вернули стандартный ответ с кодом ошибки 0 (нет ошибки) считается некорректной
     */
    private void readAckByCommand(PacketId expectedPacketId) throws FiscalPrinterException {
        try {
            PiritResponsePacket piritResponsePacket = commandStack.getResponsePacketBeforeRead(expectedPacketId);
            while (piritResponsePacket == null) {
                int ackOrSTX = waitForAckOrSTX();
                if (ackOrSTX == ACK) {
                    log.debug("ACK received");
                    return;
                }
                if (ackOrSTX == STX) {
                    byte[] packet = readOutAllData();
                    piritResponsePacket = new PiritResponsePacket(packet);
                    logDebugResponse(piritResponsePacket);
                    piritResponsePacket = commandStack.getResponsePacket(piritResponsePacket, expectedPacketId);
                } else {
                    commandStack.markRequestAsLost(expectedPacketId);
                    FiscalPrinterException exception = new FiscalPrinterException("TimeOut read of STX or ACK");
                    exception.setExceptionType(FiscalPrinterExceptionType.TIMEOUT_EXPIRED);
                    throw exception;
                }
            }
            log.error("Unexpected response packet without error");
            throw new IllegalStateException("Unexpected response packet without error");
        } catch (FiscalPrinterCommunicationException e) {
            log.error("{} (ID: {})", e.getMessage(), expectedPacketId);
            throw new FiscalPrinterCommunicationException(e.getMessage(), PiritErrorMsg.getErrorType(), e);
        } catch (Exception e) {
            throw logAndMakeFPCException(expectedPacketId, e);
        } finally {
            sentAsyncCommandCounter = 0;
        }
    }

    /**
     * Ожидание получения ACK или начала пакета (STX)
     *
     * @return полученный байт (ACK, STX) или -1, если таймаут истек
     */
    private int waitForAckOrSTX() throws IOException {
        Timer stxResponseTimer = Timer.of(READ_ACK_TIME_OUT);
        int read;
        while (stxResponseTimer.isNotExpired()) {
            if (isInputDataAvailable()) {
                read = getInputStream().read();
                if (read == ACK || read == STX) {
                    return read;
                } else if (read == NAK) {
                    log.error("Unexpected response (NAK)");
                    throw new IllegalStateException("Unexpected response (NAK)");
                } else {
                    log.debug("unexpected response (ignored)");
                    logDebugResponse(read);
                }
            } else {
                Thread.yield();
            }
        }
        return -1;
    }

    /**
     * Проверка наличия связи с Пиритом при первичном обнаружении/подключении
     *
     * @return - информация о статусе пинга
     */
    public PingStatus isPiritOnline() {
        return isPiritOnline(true);
    }

    /**
     * Проверяем, что Пирит все еще доступен (для использования приожидании ответа на отправленные команды)
     */
    private PingStatus isPiritStillOnline() {
        return isPiritOnline(false);
    }

    /**
     * Проверка наличия связи с Пиритом
     *
     * @param needClearStream признак, нужно ли почистить буфер перед отправкой (для пинга в процессе ожидания ответа команды это делать не надо)
     * @return - информация о статусе пинга
     */
    private PingStatus isPiritOnline(boolean needClearStream) {
        try {
            if (needClearStream) {
                readOutGarbageIfExist();
            }
            for (int i = 0; i < MAX_PING_ATTEMPTS; i++) {
                sendData((byte) ENQ);
                Integer foundValue = readValues(ACK, NAK, STX);
                if (foundValue != null) {
                    return new PingStatus(true, foundValue);
                }
            }
        } catch (Exception e) {
            log.error("Error on checking Pirit is online ({})", e.getMessage());
        }
        return new PingStatus(false);
    }

    /**
     * Читаем из вх. потока до нахождения ожидаемого значения
     *
     * @param listCmd - набор ожидаемых значений
     * @return - значение которое нашли
     * @throws IOException - в случае проблем с потоком из которого читаем
     */
    private Integer readValues(int... listCmd)
            throws IOException {
        if (ArrayUtils.isEmpty(listCmd)) {
            return null;
        }

        Timer enqTimer = Timer.of(TIME_OUT_BETWEEN_BYTES);
        while (enqTimer.isNotExpired()) {
            if (isInputDataAvailable()) {
                int read = getInputStream().read();
                logDebugResponse(read);
                for (int cmd : listCmd) {
                    if (read == cmd) {
                        return cmd;
                    }
                }
            } else {
                sleep(THREAD_SLEEP_TIME_MILLIS);
            }
        }
        return null;
    }

    private boolean isInputDataAvailable()
            throws IOException {
        return getInputStream().available() > 0;
    }

    private void readOutGarbageIfExist() throws IOException {
        if (getInputStream().available() > 0) {
            Timer readOutGarbageTimer = Timer.of(TIME_OUT_BETWEEN_BYTES);
            ByteArrayOutputStream garbage = new ByteArrayOutputStream();
            while (getInputStream().available() > 0 && readOutGarbageTimer.isNotExpired()) {
                garbage.write(getInputStream().read());
            }
            if (garbage.size() > 0) {
                log.debug("unexpected response (ignored):");
                logDebugResponse(garbage.toByteArray());
            }
        }
    }

    /**
     * Вычитывание всего пакета после получения STX
     */
    protected byte[] readOutAllData() throws Exception {
        Timer nextByteTimer = Timer.of(TIME_OUT_BETWEEN_BYTES);
        ByteArrayOutputStream packet = new ByteArrayOutputStream();
        packet.write(STX);
        while (nextByteTimer.isNotExpired()) {
            if (isInputDataAvailable()) {
                packet.write(getInputStream().read());
                if (isPacketComplete(packet)) {
                    if (isRecoverPacket(packet)) {
                        log.debug("Recover packet: ");
                        logDebugResponse(packet);
                        // Если получен пакет-ошибка, возвращаемый Пиритом в случае сбоя отправки ответа кассе после восстановления связи,
                        // вычитать новый пакет и отдать его как настоящий
                        return readOutAllData();
                    } else {
                        // Для вычитывания ACK, если он есть после пакета
                        if (isInputDataAvailable()) {
                            logDebugResponse((char) getInputStream().read());
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

    private byte[] readPacketByCommandForData(long readTimeOut) throws FiscalPrinterException {
        ReadableByteArrayOutputStream packet = null;
        long startTime = System.currentTimeMillis();
        boolean timeOut = false;
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (isInputDataAvailable()) {
                    int c = getInputStream().read();
                    if (packet != null) {
                        packet.write(c);
                    }
                    if ((packet == null) && ((c == 1) || (c == 2))) {
                        packet = new ReadableByteArrayOutputStream();
                        packet.write(c);
                    } else if (packet != null && (packet.size() >= 9) &&
                            (packet.valueAt(packet.size() - 3) == 3) &&
                            (packet.valueAt(packet.size() - 9) == 2) &&
                            (((packet.valueAt(packet.size() - 2) >= 0x30) && (packet.valueAt(packet.size() - 2) <= 0x39)) ||
                                    ((packet.valueAt(packet.size() - 2) >= 0x41) && (packet.valueAt(packet.size() - 2) <= 0x46))) &&
                            (((packet.valueAt(packet.size() - 1) >= 0x30) && (packet.valueAt(packet.size() - 1) <= 0x39)) ||
                                    ((packet.valueAt(packet.size() - 1) >= 0x41) && (packet.valueAt(packet.size() - 1) <= 0x46)))) {
                        break;
                    }
                } else if (System.currentTimeMillis() - startTime > readTimeOut) {
                    if (timeOut) {
                        throw new Exception("TimeOut read of data");
                    } else {
                        logDebugRawResponce(packet.toByteArray());
                        startTime = System.currentTimeMillis();
                        timeOut = true;
                    }
                } else {
                    Thread.sleep(THREAD_SLEEP_TIME_MILLIS);
                }
            }
        } catch (Exception e) {
            log.warn("", e);
            throw new FiscalPrinterCommunicationException(ResBundleFiscalPrinterPirit.getString("ERROR_READ_DATA"), PiritErrorMsg.getErrorType());
        } finally {
            sentAsyncCommandCounter = 0;
            logDebugRawResponce(packet.toByteArray());
        }
        return packet.toByteArray();
    }

    public void setMaxAsyncCommandBuffer(long maxAsyncCommandBuffer) {
        this.maxAsyncCommandBuffer = maxAsyncCommandBuffer;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }
}
