package ru.crystals.pos.bank.bpc;

import java.io.UnsupportedEncodingException;

public class DataByte {
    private final byte[] data;

    public DataByte(byte... data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        try {
            return new String(data, "cp866");
        } catch (UnsupportedEncodingException ignore) {
            return new String(data);
        }
    }
}
