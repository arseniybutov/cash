package ru.crystals.pos.scale.massak.rp;

import org.junit.Test;
import ru.crystals.pos.scale.exception.ScaleException;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

public class ScaleMassaKRPTest {

    private static final byte[] HEADER = new byte[]{
            (byte) 0xF8, 0x55, (byte) 0xCE
    };
    private static final byte STABLE = (byte) 1;
    private static final byte DIVISION_100MG = (byte) 0;
    private static final byte DIVISION_1G = (byte) 1;
    private static final byte DIVISION_10G = (byte) 2;
    private static final byte DIVISION_100G = (byte) 3;
    private static final byte DIVISION_1000G = (byte) 4;

    private ScaleMassaKRP sut = new ScaleMassaKRP();

    @Test
    public void extractWeightTest() throws ScaleException {
        byte[] le278 = new byte[]{0x16, 0x01, 0x00, 0x00};
        byte[] le65302 = new byte[]{0x16, (byte) 0xFF, 0x00, 0x00};
        byte[] le0 = new byte[]{0x00, 0x00, 0x00, 0x00};
        byte[] leMinus294 = new byte[]{(byte) 0xDA, (byte) 0xFE, (byte) 0xFF, (byte) 0xFF};

        checkWeight("278 g stable", 278, makeAckWeight(le278));
        checkWeight("278 g not stable should be 0", 0, makeAckWeight((byte) 0, le278));
        checkWeight("0 g", 0, makeAckWeight(le0));
        checkWeight("Negative values should be converted to 0", 0, makeAckWeight(leMinus294));
        checkWeight("1 g division", 65_302, makeAckWeight(le65302, DIVISION_1G));
        checkWeight("100 mg division", 6530, makeAckWeight(le65302, DIVISION_100MG));
        checkWeight("10 g division", 2780, makeAckWeight(le278, DIVISION_10G));
        checkWeight("100 g division", 27_800, makeAckWeight(le278, DIVISION_100G));
        checkWeight("1000 g division", 278_000, makeAckWeight(le278, DIVISION_1000G));
    }

    private void checkWeight(String msg, int expected, byte[] responsePacket) throws ScaleException {
        final int actualWeight = sut.extractWeight(responsePacket);
        assertEquals(msg, expected, actualWeight);
    }

    private byte[] makeAckWeight(byte[] weightLittleEndian) {
        return makeAckWeight(STABLE, weightLittleEndian);
    }

    private byte[] makeAckWeight(byte[] weightLittleEndian, byte division) {
        return makeAckWeight(STABLE, weightLittleEndian, division);
    }

    private byte[] makeAckWeight(byte stable, byte[] weightLittleEndian) {
        return makeAckWeight(stable, weightLittleEndian, DIVISION_1G);
    }

    private byte[] makeAckWeight(byte stable, byte[] weightLittleEndian, byte division) {
        return ByteBuffer.allocate(14)
                .put(HEADER)
                .putShort((short) 7)
                .put((byte) 0x10)
                .put(weightLittleEndian)
                .put(division)
                .put(stable)
                .putShort((short) 0)
                .array();
    }

}