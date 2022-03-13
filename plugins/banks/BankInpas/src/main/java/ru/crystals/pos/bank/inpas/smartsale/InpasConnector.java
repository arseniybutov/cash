package ru.crystals.pos.bank.inpas.smartsale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.InpasConstants;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.inpas.smartsale.serial.SimpleSerialPortConnector;
import ru.crystals.utils.time.Timer;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class InpasConnector implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(InpasConnector.class);

    public static final int WAIT_OPERATION_CODE = 21;
    private static final byte SOH = 0x01;
    private static final byte STX = 0x02;
    private static final byte EOT = 0x04;
    private static final byte ACK = 0x06;
    private static final long TO1 = TimeUnit.MINUTES.toMillis(2);
    private static final long TO2 = TimeUnit.SECONDS.toMillis(15);
    private long overallTimeOut = TO1;
    private long readByteTimeOut = TO2;
    private SimpleSerialPortConnector port;

    public InpasConnector(SimpleSerialPortConnector port) {
        this.port = port;
    }

    public void setOverallTimeOut(long overallTimeOut) {
        this.overallTimeOut = overallTimeOut;
    }

    public void setReadByteTimeOut(long readByteTimeOut) {
        this.readByteTimeOut = readByteTimeOut;
    }

    public void open(String portName, String baudRate, String dataBits, String stopBits, String parity) throws Exception {
        port.setPort(portName)
                .setBaudRate(baudRate)
                .setDataBits(Integer.parseInt(dataBits))
                .setStopBits(stopBits)
                .setParity(parity)
                .openPort();
    }

    public void close() {
        port.close();
    }

    /**
     * Отправка пакета
     *
     * @param fc - данные для отправки
     */
    public void sendPacket(FieldCollection fc) throws BankException {
        ByteArrayOutputStream garbageData = new ByteArrayOutputStream();
        try {
            ByteArrayOutputStream packet = new ByteArrayOutputStream();
            byte[] data = fc.toArray();
            int len = data.length;

            packet.write(STX);
            packet.write((byte) len);
            packet.write((byte) (len >> 8));
            packet.write(data);

            short crc = calcCRC(packet.toByteArray(), packet.size());

            packet.write((byte) (crc));
            packet.write((byte) (crc >> 8));
            packet.flush();

            byte[] dataToWrite = packet.toByteArray();
            port.write(dataToWrite);

            if (LOG.isDebugEnabled()) {
                LOG.debug("--> {}", fc.toString());
            }
            logWrite(dataToWrite);

            final Timer readByteTimer = Timer.of(Duration.ofMillis(readByteTimeOut));
            while (!Thread.interrupted()) {
                if (port.getInputStreamBufferSize() <= 0) {
                    if (readByteTimer.isExpired()) {
                        throw new BankException(ResBundleBankInpas.getString("ERROR_TIMEOUT_READ_ACK"));
                    }
                    Thread.sleep(100);
                    continue;
                }
                byte c = (byte) port.read();
                if (c == ACK) {
                    logRead(c);
                    logGarbageData(garbageData);
                    return;
                } else {
                    garbageData.write(c);
                }
            }
            throw new IllegalStateException("Unexpectedly interrupted");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Interrupted", e);
            throw new BankException(ResBundleBankInpas.getString("ERROR_SEND_DATA"), e);
        } catch (Exception e) {
            LOG.error("", e);
            logGarbageData(garbageData);
            throw new BankException(ResBundleBankInpas.getString("ERROR_SEND_DATA"), e);
        }
    }

    private void logGarbageData(ByteArrayOutputStream garbageData) {
        if (garbageData.size() > 0) {
            logRead("garbage data ", garbageData.toByteArray());
        }
    }

    /**
     * Чтение пакета
     *
     * @return VO с полученными данными
     */
    public FieldCollection readPacket() throws BankException {
        ByteArrayOutputStream bodyPacket = new ByteArrayOutputStream();
        ByteArrayOutputStream packet = new ByteArrayOutputStream();

        final Timer overAllTimer = Timer.of(Duration.ofMillis(overallTimeOut));
        int len = 0;
        ReadState stateRead = ReadState.STX;
        byte packetType = 0;
        int numHeader = 0;
        short crc = 0;

        try {
            while (!Thread.interrupted()) {
                if (port.getInputStreamBufferSize() <= 0) {
                    if (overAllTimer.isExpired()) {
                        throw new BankException(ResBundleBankInpas.getString("ERROR_TIMEOUT_READ_DATA"));
                    }
                    Thread.sleep(100);
                    continue;
                }
                byte c = (byte) port.read();
                logRead(c);
                packet.write(c);

                switch (stateRead) {
                    case STX: {
                        if (c == STX || c == SOH) {
                            packetType = c;
                            stateRead = ReadState.LENGTH;
                        }
                        break;
                    }
                    case LENGTH: {
                        len = (c & 0xFF);
                        stateRead = ReadState.PAYLOAD;
                        break;
                    }
                    case PAYLOAD: {
                        len |= ((c << 8) & 0xFF00);
                        LOG.debug("Packet length {}", len);

                        if (packetType == SOH) {
                            byte[] header = readHeaderSOHPacket().toByteArray();
                            numHeader = getNumSOHPacket(header);
                            packet.write(header);
                            len = (len - header.length);
                            LOG.debug("Packet length recalculated (SOH) {}", len);
                        }

                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        final Timer readByteTimer = Timer.of(Duration.ofMillis(readByteTimeOut));
                        while (buffer.size() < len && !Thread.interrupted()) {
                            // если данные не успели придти, ждем их readByteTimeOut
                            int read = port.read();
                            if (read == -1) {
                                if (readByteTimer.isExpired()) {
                                    if (buffer.size() > 0) {
                                        LOG.warn("Read only {} of expected {}", buffer.size(), len);
                                        logRead(buffer.toByteArray());
                                    }
                                    // если ожидаемые данные так и не пришли
                                    throw new BankException(ResBundleBankInpas.getString("ERROR_TIMEOUT_READ_DATA"));
                                }
                                Thread.sleep(100);
                            } else {
                                buffer.write(read);
                                readByteTimer.restart();
                            }
                        }
                        byte[] readBodyBytes = buffer.toByteArray();
                        logRead(readBodyBytes);
                        packet.write(readBodyBytes);
                        bodyPacket.write(readBodyBytes);

                        stateRead = ReadState.CRC_1;
                        break;
                    }
                    case CRC_1: {
                        crc = (short) (c & 0xFF);
                        stateRead = ReadState.CRC_2;
                        break;
                    }
                    case CRC_2: {
                        crc |= (short) ((c << 8) & 0xFF00);

                        short calcCRC = calcCRC(packet.toByteArray(),
                                packet.size() - 2);
                        if (calcCRC != crc) {
                            throw new Exception("Error CRC: readCRC="
                                    + String.format("%04X", crc) + " calcCRC="
                                    + String.format("%04X", calcCRC));
                        }

                        // если это SOH пакет и он не последний
                        if (packetType == SOH && numHeader > 0) {
                            port.write(ACK);
                            packet.reset();
                            overAllTimer.restart();

                            stateRead = ReadState.STX;

                            LOG.debug("Read {} SOH packet and waiting for the next.", numHeader);
                        } else {

                            // тут уже хранится содержимое пакета, без служебных
                            // символов и без header, если это SOH пакет
                            bodyPacket.flush();

                            FieldCollection result = new FieldCollection(bodyPacket.toByteArray(), 0, bodyPacket.size());
                            LOG.debug(result.toString());

                            if (result.getOperationCode() == WAIT_OPERATION_CODE) {
                                overAllTimer.restart();

                                stateRead = ReadState.STX;
                                bodyPacket.reset();
                                packet.reset();

                                LOG.debug("Return packet with OperationCode == {}", WAIT_OPERATION_CODE);
                            } else {
                                port.write(ACK);
                                if (result.getOperationCode() != InpasConstants.EXECUTE_USER_COMMAND) {
                                    port.write(EOT);
                                }
                                LOG.debug("Finish read {} packet.", ((packetType == SOH) ? "SOH" : "STX"));
                                return result;
                            }
                        }
                        break;
                    }
                    default:
                        throw new IllegalStateException("Unexpected state: " + stateRead);
                }
            }
            throw new IllegalStateException("Unexpectedly interrupted");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BankException(ResBundleBankInpas.getString("ERROR_SEND_DATA"), e);
        } catch (Exception e) {
            LOG.error("", e);
            throw new BankException(ResBundleBankInpas.getString("ERROR_SEND_DATA"), e);
        }
    }

    /**
     * Чтение заголовка SOH пакета
     *
     * @return Заголовок SOH пакета в формате: <b>[LL LH] [N LL LH D] ... [N LL
     * LH D]</b>, где </br> <b>[LL LH]</b> - длина заголовка (общая
     * длина всех тегов заголовка) </br> <b>[N LL LH D]</b> - тег
     * заголовка, где </br> <b>[N]</b> - номер поля заголовка (1 байт)
     * </br> <b>[LL LH]</b> - длина данных поля заголовка (2 байта: LL -
     * младший байт, LH - старший байт) </br> <b>[D]</b> - данные поля
     * заголовка переменной длины
     * @throws BankException
     */
    private ByteArrayOutputStream readHeaderSOHPacket() throws BankException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        int stateRead = 1;
        int len = 0;
        int ptr = 0;
        try {
            while (!Thread.interrupted()) {
                if (port.getInputStreamBufferSize() > 0) {
                    byte c = (byte) port.read();
                    result.write(c);

                    switch (stateRead) {
                        case 1: {
                            len = (c & 0xFF);
                            stateRead = 2;
                            break;
                        }
                        case 2: {
                            len |= ((c << 8) & 0xFF00);
                            stateRead = 3;
                            break;
                        }
                        case 3: {
                            if (++ptr == len) {
                                return result;
                            }
                            break;
                        }
                        default:
                            throw new IllegalStateException("Unknown state \"" + stateRead + "\"");
                    }
                }
            }
            throw new IllegalStateException("Unexpectedly interrupted");
        } catch (Exception e) {
            throw new BankException(ResBundleBankInpas.getString("ERROR_TIMEOUT_READ_DATA"));
        }
    }

    /**
     * Получаем номер SOH пакета
     *
     * @param header заголовок пакета
     * @return номер пакета
     * @throws BankException
     */
    private int getNumSOHPacket(byte[] header) throws BankException {

        int i = 0;
        int ptr = 0;
        int length;
        int stateRead = 0;
        int len = 0;
        boolean isNumPacket = false;
        byte[] result = null;

        length = (header[ptr++] & 0xFF);
        length |= ((header[ptr++] << 8) & 0xFF00);
        length = length + 2;

        while (true) {
            if (ptr < length) {
                byte c = header[ptr++];

                switch (stateRead) {
                    case 0: {
                        if (c == 2) {
                            isNumPacket = true;
                        }
                        stateRead = 1;
                        break;
                    }
                    case 1: {
                        len = (c & 0xFF);
                        stateRead = 2;
                        break;
                    }
                    case 2: {
                        len |= ((c << 8) & 0xFF00);
                        stateRead = 3;
                        result = new byte[len];
                        i = 0;
                        break;
                    }
                    case 3: {
                        if (isNumPacket && result != null) {
                            result[i] = c;
                        }

                        if (++i == len) {
                            if (isNumPacket) {
                                return Integer.parseInt(new String(result));
                            }
                            stateRead = 0;
                        }
                        break;
                    }
                    default:
                        throw new IllegalStateException("Unknown state \"" + stateRead + "\"");
                }
            } else {
                throw new BankException(ResBundleBankInpas.getString("ERROR_TIMEOUT_READ_DATA"));
            }
        }
    }

    private short calcCRC(byte[] data, int length) {
        short s = 0;

        for (int i = 0; i < Math.min(data.length, length); i++) {
            byte b = data[i];
            for (int j = 0; j < 8; j++) {
                int x16 = ((((b & 0x80) != 0) && ((s & 0x8000) != 0)) || (((b & 0x80) == 0) && ((s & 0x8000) == 0))) ? 0 : 1;
                int x15 = (((x16 != 0) && ((s & 0x4000) != 0)) || ((x16 == 0) && ((s & 0x4000) == 0))) ? 0 : 1;
                int x2 = (((x16 != 0) && ((s & 0x0002) != 0)) || ((x16 == 0) && ((s & 0x0002) == 0))) ? 0 : 1;
                s = (short) (s << 1);
                b = (byte) (b << 1);
                s |= (x16 != 0) ? 0x0001 : 0;
                s = (short) ((x2 != 0) ? s | 0x0004 : s & 0xfffb);
                s = (short) ((x15 != 0) ? s | 0x8000 : s & 0x7fff);
            }
        }

        s = Short.reverseBytes(s);
        return s;
    }

    private void logWrite(byte[] data) {
        logData("-->", "sent", "", data);
    }

    private void logRead(String message, byte[] data) {
        logData("<--", "read", message, data);
    }

    private void logRead(byte[] readData) {
        logRead("", readData);
    }

    private void logData(String directionArrow, String directionMessage, String extraMessage, byte[] data) {
        if (data.length == 0) {
            return;
        }
        if (LOG.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            for (byte b : data) {
                sb.append(String.format("%02X", b & 0xFF));
            }
            LOG.debug("{} {}({} {} bytes):\n{}", directionArrow, extraMessage, directionMessage, data.length, sb.toString());
        }
    }

    private void logRead(byte b) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("<-- {}", String.format("%02X", b & 0xFF));
        }
    }

    private enum ReadState {
        STX, LENGTH, PAYLOAD, CRC_1, CRC_2
    }
}
