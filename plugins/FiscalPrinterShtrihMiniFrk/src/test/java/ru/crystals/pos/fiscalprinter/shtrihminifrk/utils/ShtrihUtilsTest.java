package ru.crystals.pos.fiscalprinter.shtrihminifrk.utils;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ShtrihUtilsTest {

    @Test
    public void testParseTlv() {
        byte[] tlv = new byte[] {0x42, 0x08, 0x04, 0x00, (byte) 0x9E, (byte) 0xF0, (byte) 0xD9, 0x60, 0x39, 0x08, 0x01, 0x00, 0x00,
                (byte) 0xD5, 0x07, 0x01, 0x00, 0x05, 0x3D, 0x08, 0x01, 0x00, 0x01, 0x34, 0x08, 0x01, 0x00, 0x02};
        Map<Integer, Long> parsed = ShtrihUtils.parseTlv(tlv);
        assertEquals(5, parsed.size());
        assertEquals(new Long(1624895646), parsed.get(2114));
        assertEquals(0, parsed.get(2105).intValue());
        assertEquals(5, parsed.get(2005).intValue());
        assertEquals(1, parsed.get(2109).intValue());
        assertEquals(2, parsed.get(2100).intValue());
    }
}