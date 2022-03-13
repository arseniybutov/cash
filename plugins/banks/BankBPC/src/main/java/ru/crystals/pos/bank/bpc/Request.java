package ru.crystals.pos.bank.bpc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Request {
    private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public byte[] toBytes() {
        ByteArrayOutputStream requestBytes = new ByteArrayOutputStream();
        byte[] bytes = buffer.toByteArray();
        requestBytes.write(0x00);
        requestBytes.write(bytes.length);
        try {
            requestBytes.write(bytes);
        } catch (IOException ignore) {
        }
        return requestBytes.toByteArray();
    }

    public Request addField(int tag, String pur) {
        buffer.write(tag);
        buffer.write(pur.length());
        try {
            buffer.write(pur.getBytes());
        } catch (IOException ignore) {
        }
        return this;
    }

    public Request addField(int tag, int i) {
        buffer.write(tag);
        buffer.write(0x01);
        buffer.write(i);
        return this;
    }
}
