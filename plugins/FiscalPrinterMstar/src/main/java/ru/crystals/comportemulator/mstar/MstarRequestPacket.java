package ru.crystals.comportemulator.mstar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.fiscalprinter.transport.mstar.DataPacket;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class MstarRequestPacket {

    public static final String ENCODING = "cp866";
    /**
     * Байт начала пакета (0x02).
     */
    public static final byte STX = 2;
    /**
     * Байт окончания пакета (0x03).
     */
    public static final byte ETX = 3;
    /**
     * Код разделения (под)комманд
     */
    public static final byte FS = 0x1C;
    public static final String SPLITTER = new String(new byte[]{FS});
    private static final Logger log = LoggerFactory.getLogger(MstarRequestPacket.class);
    /**
     * ID пакета.
     */
    private byte packetId;
    /**
     * Пароль связи.
     */
    private String password = "PIRI";
    /**
     * ID команды
     */
    private MstarCommand commandID;
    /**
     * Пакет данных
     */
    private MstarDataPacket data;

    private DataPacket paramsData;

    private Long threadID;

    private boolean lost = false;

    public MstarRequestPacket(int command, byte[] data) {
        commandID = MstarCommand.getCommandID(command);
        this.data = new MstarDataPacket(data);
    }

    public MstarRequestPacket(Byte packetId, MstarCommand commandID, DataPacket paramsData) {
        this.packetId = packetId == null ? 0 : packetId;
        this.commandID = commandID;
        this.paramsData = paramsData == null ? new DataPacket() : paramsData;
    }

    public static int getPacketCRC(byte[] packet) {
        byte[] dataArray = new byte[2];
        dataArray[0] = packet[packet.length - 2];
        dataArray[1] = packet[packet.length - 1];
        String readCrcString = new String(dataArray);
        return Integer.valueOf(readCrcString, 16);
    }

    public static int createCrc(byte[] dataArray) {
        int newCrc = 0;
        for (int i = 1; i < dataArray.length; i++) {
            newCrc = (newCrc ^ dataArray[i]) & 0xFF;
        }
        return newCrc;
    }

    @Override
    public String toString() {
        if (paramsData != null) {
            return String.format("MstarRequestPacket{Id=%d, Cmd=%s, data=%s}", packetId, commandID, paramsData.getDataBuffer());
        } else if (data != null) {
            return String.format("MstarRequestPacket{Id=%d, Cmd=%s, data=%s}", packetId, commandID, data.paramsToString());
        }
        return String.format("MstarRequestPacket{Cmd=%s}", commandID);
    }

    public byte getPacketId() {
        return packetId;
    }

    public MstarCommand getCommandID() {
        return commandID;
    }

    public ByteBuffer getData() {
        try {
            return ByteBuffer.wrap(this.data.paramsAsByteArray());
        } catch (Exception ex) {
            log.error("", ex);
        }
        return null;
    }

    public void setData(ByteBuffer data) {
        this.data = new MstarDataPacket(data);
    }

    public byte[] toByteArray() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(STX);
            baos.write(password.getBytes(ENCODING));
            baos.write(packetId);
            baos.write(String.format("%02X", commandID.getCode()).getBytes());
            if (paramsData != null) {
                baos.write(paramsData.getDataBuffer().getBytes(ENCODING));
            } else {
                baos.write(getData().array());
            }
            baos.write(ETX);
            baos.write(String.format("%02X", createCrc(baos.toByteArray())).getBytes());
            return baos.toByteArray();
        } catch (Exception ex) {
            log.error("", ex);
        }
        return null;
    }

    public Long getThreadID() {
        return threadID;
    }

    public void setThreadID(Long threadID) {
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

        MstarRequestPacket that = (MstarRequestPacket) o;

        return commandID == that.commandID;
    }

    @Override
    public int hashCode() {
        int result = commandID.hashCode();
        result = 31 * result + (data.getData() != null ? data.getData().hashCode() : 0);
        return result;
    }

    public void setLost() {
        this.lost = true;
    }

    public boolean isLost() {
        return lost;
    }
}
