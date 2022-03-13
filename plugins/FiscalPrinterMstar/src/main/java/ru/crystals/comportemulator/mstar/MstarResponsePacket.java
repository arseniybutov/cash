package ru.crystals.comportemulator.mstar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.fiscalprinter.mstar.core.ResBundleFiscalPrinterMstar;
import ru.crystals.pos.fiscalprinter.transport.mstar.DataPacket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class MstarResponsePacket {
    private static final Logger LOG = LoggerFactory.getLogger(MstarResponsePacket.class);

    private byte packetId = -1;

    private MstarDataPacket data;
    private DataPacket dataPacket;

    private int errorCode;
    private MstarCommand commandID;


    public MstarResponsePacket(byte[] packet) throws IOException {
        if (packet.length == 0) {
            this.data = new MstarDataPacket(ByteBuffer.allocate(0));
        }
        if (packet.length == 1) {
            this.commandID = MstarCommand.getCommandID(packet[0]);
        } else if (packet.length > 1) {
            this.packetId = packet[1];
            this.errorCode = Integer.parseInt(new String(new byte[]{packet[4], packet[5]}), 16);
            this.commandID = MstarCommand.getCommandID(Integer.parseInt(new String(new byte[]{packet[2], packet[3]}), 16));
            int etxIndex = 0;
            while (packet[etxIndex] != MstarRequestPacket.ETX && etxIndex < packet.length) {
                etxIndex++;
            }
            if (packet[etxIndex] != MstarRequestPacket.ETX) {
                throw new IOException("EOF ERROR");
            }
            byte[] dataArray = new byte[etxIndex - 2];
            System.arraycopy(packet, 2, dataArray, 0, etxIndex - 2);
            this.data = new MstarDataPacket(dataArray);

            String packetStr = new String(packet, MstarRequestPacket.ENCODING);
            packetStr = packetStr.substring(6, packetStr.length() - 3);
            this.dataPacket = new DataPacket(packetStr);

            dataArray = new byte[packet.length - 2];
            System.arraycopy(packet, 0, dataArray, 0, packet.length - 2);

            int calcCrc = MstarRequestPacket.createCrc(dataArray);
            int realCrc = MstarRequestPacket.getPacketCRC(packet);
            if (calcCrc != realCrc) {
                LOG.error("CRC ERROR - calc crc: " + String.format("%02X", calcCrc) + "       real: " + String.format("%02X", packet[packet.length - 1]) + "       STR '" + Arrays.toString(dataArray) + "'" + "       full '" + Arrays.toString(packet) + "'");
                throw new IOException(ResBundleFiscalPrinterMstar.getString("ERROR_RESPONSE_CRC"));
            }
        }
    }

    @Override
    public String toString() {
        if (packetId == -1) {
            return String.format("MstarResponsePacket{Cmd=%s}", commandID);
        } else {
            return String.format("MstarResponsePacket{Id=%d, Cmd=%s, data=%s}", packetId, commandID, data.paramsToString());
        }
    }

    public DataPacket getDataPacket() {
        return this.dataPacket;
    }


    public byte getPacketId() {
        return packetId;
    }

    public ByteBuffer getData() {
        return data.getData();
    }

    public void setData(ByteBuffer data) {
        this.data = new MstarDataPacket(data);
    }

    public int getErrorCode() {
        return errorCode;
    }

    public MstarCommand getCommandID() {
        return commandID;
    }
}
