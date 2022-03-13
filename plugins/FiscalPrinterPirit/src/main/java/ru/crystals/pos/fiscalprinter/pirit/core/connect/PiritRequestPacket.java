package ru.crystals.pos.fiscalprinter.pirit.core.connect;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.comportemulator.interfaces.RequestPacketUtils;
import ru.crystals.comportemulator.pirit.PiritCommand;
import ru.crystals.pos.fiscalprinter.pirit.core.ResBundleFiscalPrinterPirit;
import ru.crystals.pos.fiscalprinter.transport.DataPacket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static java.lang.String.format;

/**
 * @author dalex
 */
public class PiritRequestPacket {

    private static final Logger log = LoggerFactory.getLogger(PiritRequestPacket.class);

    /**
     * ID пакета.
     */
    private final PacketId packetId;

    private final DataPacket data;

    /**
     * Признак заголовочного пакета
     * (с него надо начинать поиск для тестирования)
     */
    private boolean startPacket;

    private long threadID;

    private boolean lost;

    public PiritRequestPacket(int command, byte[] data) {
        this.packetId = new PacketId(0, PiritCommand.getCommandID(command));
        this.data = new DataPacket(data);
    }

    public PiritRequestPacket(PiritCommand commandID, String... params) {
        this(0, commandID, params);
    }

    public PiritRequestPacket(PiritCommand commandID, boolean startPacket, String... params) {
        this(commandID, params);
        this.startPacket = startPacket;
    }

    public PiritRequestPacket(int packetId, PiritCommand commandID, String... params) {
        this.packetId = new PacketId(packetId, commandID);
        this.data = new DataPacket(params);
    }

    public PiritRequestPacket(int packetId, PiritCommand commandID, DataPacket data) {
        this.packetId = new PacketId(packetId, commandID);
        this.data = data == null ? new DataPacket() : data;
    }

    public PiritRequestPacket(int packetId, ExtendedCommand commandID, DataPacket data) {
        this.packetId = new PacketId(packetId, commandID);
        this.data = data == null ? new DataPacket() : data;
        this.data.putIntValue(commandID.getSubCmd(), 0);
    }

    public PiritRequestPacket(byte[] packetBytes) throws IOException {
        String packet = new String(packetBytes, PiritConnector.ENCODING);

        int id = packet.charAt(5);
        PiritCommand command = PiritCommand.getCommandID(Integer.parseInt(packet.substring(6, 8), 16));

        int realCrc = Integer.parseInt(packet.substring(packet.length() - 2), 16);
        final String crcCalculationBase = StringUtils.substring(packet, 0, packet.length() - 2);
        int calcCrc = createCrc(crcCalculationBase.getBytes(PiritConnector.ENCODING));
        if (packet.length() > PiritConnector.MIN_ANSWER_PACKET_LENGTH) {
            data = new DataPacket(StringUtils.substring(packet, 8, packet.length() - 3));
            final ExtendedCommand extCommand = ExtendedCommand.getByCommandAndSubCommand(command, data.getStringValueNull(0));
            this.packetId = new PacketId(id, command, extCommand);
        } else {
            data = new DataPacket();
            this.packetId = new PacketId(id, command);
        }
        if (calcCrc != realCrc) {
            log.error("CRC mismatch (request): calculated ({}), received ({}). Calculated for '{}'",
                    String.format("%02X", calcCrc),
                    String.format("%02X", realCrc),
                    crcCalculationBase);
            throw new IOException(ResBundleFiscalPrinterPirit.getString("ERROR_RESPONSE_CRC"));
        }
    }

    public PacketId getPacketId() {
        return packetId;
    }

    public byte[] toByteArray() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(PiritConnector.STX);
            baos.write(PiritConnector.PASSWORD_BYTES);
            baos.write(packetId.getPacketId());
            baos.write(format("%02X", packetId.getCommand().getCode()).getBytes());
            if (data != null && data.getCountValue() > 0) {
                baos.write(data.getDataBuffer().getBytes(PiritConnector.ENCODING));
            }
            baos.write((byte) PiritConnector.ETX);
            baos.write(format("%02X", createCrc(baos.toByteArray())).getBytes());
            return baos.toByteArray();
        } catch (Exception ex) {
            log.error("", ex);
            throw new RuntimeException(ex);
        }
    }

    public static int createCrc(byte[] dataArray) {
        int newCrc = 0;
        for (int i = 1; i < dataArray.length; i++) {
            newCrc = (newCrc ^ dataArray[i]) & 0xFF;
        }
        return newCrc;
    }

    public boolean isStartPacket() {
        return startPacket;
    }

    public long getThreadID() {
        return threadID;
    }

    public void setThreadID(long threadID) {
        this.threadID = threadID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PiritRequestPacket that = (PiritRequestPacket) o;

        if (packetId.getCommand() != that.packetId.getCommand()) {
            return false;
        }

        final List<String> thisParams = this.data.getParams();
        final List<String> thatParams = that.data.getParams();

        if (thisParams.size() != thatParams.size()) {
            return false;
        }
        for (int i = 0; i < thisParams.size(); i++) {
            if (!RequestPacketUtils.testEqualsDataWithTags(thisParams.get(i), thatParams.get(i))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        return packetId.getCommand().hashCode();
    }

    public void setLost() {
        this.lost = true;
    }

    public boolean isLost() {
        return lost;
    }

    @Override
    public String toString() {
        return String.valueOf(packetId) + data;
    }

}
