package ru.crystals.pos.scale.massak.protocol100.request;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SetTare extends Request {

    public SetTare(int tareWeight) {
        super(0xA3, ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(tareWeight).array());
    }

    public SetTare() {
        this(0);
    }

}
