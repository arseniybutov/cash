package ru.crystals.pos.fiscalprinter.atol3.highlevel.types;

import org.junit.Assert;
import org.junit.Test;

public class ValueDecoderTest {
    @Test
    public void testLongDecoder() {
        LongDecoder longDecoder = new LongDecoder();
        Assert.assertEquals(6813594L, (long)longDecoder.decode(new byte[] { 0x00, 0x06, (byte)0x81, 0x35, (byte)0x94 }, 0, 5));
        Assert.assertEquals(8135L, (long)longDecoder.decode(new byte[] { 0x00, 0x06, (byte)0x81, 0x35, (byte)0x94 }, 2, 2));
    }
}
