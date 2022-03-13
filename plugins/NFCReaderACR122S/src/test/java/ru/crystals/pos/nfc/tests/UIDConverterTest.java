package ru.crystals.pos.nfc.tests;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import ru.crystals.utils.NfcUIDConverter;

public class UIDConverterTest {

    @Test
    public void testConverter() {
        assertEquals("102042012055", NfcUIDConverter.convert(new byte[] {0x66, 0x2A, 0x0C, 0x37}));
        assertEquals("052153073177", NfcUIDConverter.convert(new byte[] {0x34, (byte) 0x99, 0x49, (byte) 0xB1}));
        assertEquals("001102042184", NfcUIDConverter.convert(new byte[] {0x01, 0x66, 0x2A, (byte) 0xB8}));
        assertEquals("255255255255", NfcUIDConverter.convert(new byte[] {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF}));
        assertEquals("255255255001", NfcUIDConverter.convert(new byte[] {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x01}));
        assertEquals("001002008006", NfcUIDConverter.convert(new byte[] {0x01, 0x02, 0x08, 0x06}));
        assertEquals("000000103158", NfcUIDConverter.convert(new byte[] {0x00, 0x00, 0x67, (byte) 0x9E}));
        assertEquals("110000000000", NfcUIDConverter.convert(new byte[] {0x6E, 0x00, 0x00, 0x00}));
        assertNull(NfcUIDConverter.convert(new byte[] {0, 0, 0, 0}));
        assertEquals("000000005013", NfcUIDConverter.convert(new byte[] {0, 0, 5, 13}));
        assertEquals("000000015013", NfcUIDConverter.convert(new byte[] {0, 0, 15, 13}));
        assertEquals("000000000013", NfcUIDConverter.convert(new byte[] {0, 0, 0, 13}));
        assertEquals("128000005013", NfcUIDConverter.convert(new byte[] {(byte) 128, 0, 5, 13}));
        assertEquals("001000000000", NfcUIDConverter.convert(new byte[] {1, 0, 0, 0}));
        assertEquals("001000000125", NfcUIDConverter.convert(new byte[] {1, 0, 0, 125}));
        assertEquals("051226043238", NfcUIDConverter.convert(new byte[] {51, (byte) 226, 43, (byte) 238}));
    }
}
