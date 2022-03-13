package ru.crystals.pos.fiscalprinter.atol3.transport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.StringJoiner;
import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.actions.Action;

public class Packet {
    private final int len;
    private final int id;
    private final byte[] data;
    private final int crc;

    static Packet create(int id, Action action) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        action.write(buffer);

        byte[] data = buffer.toByteArray();
        CRC8 crc8 = new CRC8();
        crc8.add(id);
        crc8.add(data);

        return new Packet(data.length, id, data, crc8.get());
    }

    int getLen() {
        return len;
    }

    int getId() {
        return id;
    }

    byte[] getData() {
        return data;
    }

    int getCrc() {
        return crc;
    }

    Packet(int len, int id, byte[] data, int crc) {
        this.len = len;
        this.id = id;
        this.data = data;
        this.crc = crc;
    }

    public static String bufferToString(byte[] buffer) {
        StringJoiner joiner = new StringJoiner(" ", "[", "]");

        for (byte data : buffer) {
            joiner.add(byteToString(data));
        }

        return joiner.toString();
    }

    static String byteToString(byte b) {
        return String.format("%02X", b);
    }
}
