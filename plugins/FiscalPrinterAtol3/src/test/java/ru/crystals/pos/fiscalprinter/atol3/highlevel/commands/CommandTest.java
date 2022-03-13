package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import org.junit.Assert;
import org.junit.Test;

public class CommandTest {
    @Test
    public void testEncode() {
        Assert.assertArrayEquals(new byte[] { 0x00, 0x00, 0x00 }, Command.encode(0L, 3));
        Assert.assertArrayEquals(new byte[] { 0x18, 0x00}, Command.encode(1800, 2));
        Assert.assertArrayEquals(new byte[] { 0x10, 0x00 }, Command.encode(1000, 2));
        Assert.assertArrayEquals(new byte[] { 0x00, 0x06, (byte)0x81, 0x35, (byte)0x94 }, Command.encode(6813594, 5));
    }
}
