package ru.crystals.pos.fiscalprinter.pirit.core.connect;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.comportemulator.pirit.PiritCommand;
import ru.crystals.pos.fiscalprinter.pirit.core.ResBundleFiscalPrinterPirit;
import ru.crystals.pos.fiscalprinter.transport.DataPacket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static ru.crystals.pos.fiscalprinter.pirit.core.connect.PiritRequestPacket.createCrc;

/**
 * @author dalex
 */
public class PiritResponsePacket {
    private static final Logger log = LoggerFactory.getLogger(PiritResponsePacket.class);

    private PacketId packetId;
    private final DataPacket data;

    private int errorCode;

    public PiritResponsePacket(int packetId, PiritCommand command, byte[] data) {
        this.packetId = new PacketId(packetId, command);
        this.data = new DataPacket(data);
    }

    public PiritResponsePacket(PacketId packetId, DataPacket data) {
        this.packetId = packetId;
        this.data = data;
    }

    public PiritResponsePacket(String param, String... params) {
        data = new DataPacket();
        errorCode = Integer.parseInt(param.substring(2, 4), 16);
        PiritCommand command = PiritCommand.getCommandID(Integer.parseInt(param.substring(0, 2), 16));
        if (param.length() > 4) {
            final ExtendedCommand extCommand = ExtendedCommand.getByCommandAndSubCommand(command, param.substring(4));
            packetId = new PacketId(0, command, extCommand);
            data.putStringValue(param.substring(4));
        } else {
            packetId = new PacketId(0, command);
        }
        if (params.length > 0) {
            for (String s : params) {
                data.putStringValue(s);
            }
        }
    }

    public PiritResponsePacket(byte[] packetBytes) throws IOException {
        String packet = new String(packetBytes, PiritConnector.ENCODING);

        int id = packet.charAt(1);
        PiritCommand command = PiritCommand.getCommandID(Integer.parseInt(packet.substring(2, 4), 16));
        int realCrc = Integer.parseInt(packet.substring(packet.length() - 2), 16);
        final String crcCalculationBase = StringUtils.substring(packet, 0, packet.length() - 2);
        int calcCrc = createCrc(crcCalculationBase.getBytes(PiritConnector.ENCODING));
        errorCode = Integer.parseInt(packet.substring(4, 6), 16);

        if (packet.length() > PiritConnector.MIN_ANSWER_PACKET_LENGTH) {
            data = new DataPacket(StringUtils.stripEnd(StringUtils.substring(packet, 6, packet.length() - 3), PiritConnector.FS_STR));
            final ExtendedCommand extCommand = ExtendedCommand.getByCommandAndSubCommand(command, data.getStringValueNull(0));
            this.packetId = new PacketId(id, command, extCommand);
        } else {
            data = new DataPacket();
            this.packetId = new PacketId(id, command);
        }

        if (calcCrc != realCrc) {
            log.error("CRC mismatch: calculated ({}), received ({}). Calculated for '{}'",
                    String.format("%02X", calcCrc),
                    String.format("%02X", realCrc),
                    crcCalculationBase);
            throw new IOException(ResBundleFiscalPrinterPirit.getString("ERROR_RESPONSE_CRC"));
        }
    }

    public DataPacket getData() {
        return data;
    }

    public PacketId getPacketId() {
        return packetId;
    }

    public void setPacketId(PacketId packetId) {
        this.packetId = packetId;
    }

    public byte[] toByteArray() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(PiritConnector.STX);
            baos.write(packetId.getPacketId());
            baos.write(String.format("%02X", packetId.getCommand().getCode()).getBytes());
            baos.write(String.format("%02X", errorCode).getBytes());
            baos.write(data.getDataBuffer().getBytes(PiritConnector.ENCODING));
            baos.write(PiritConnector.ETX);
            baos.write(String.format("%02X", createCrc(baos.toByteArray())).getBytes());
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        if (errorCode != 0) {
            return String.format("%s%s (error 0x%02X)", packetId, data, errorCode);
        }
        return String.valueOf(packetId) + data;
    }


}
