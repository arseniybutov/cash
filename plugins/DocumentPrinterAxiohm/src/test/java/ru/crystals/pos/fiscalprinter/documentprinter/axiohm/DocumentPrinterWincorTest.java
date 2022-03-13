package ru.crystals.pos.fiscalprinter.documentprinter.axiohm;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class DocumentPrinterWincorTest {

    DocumentPrinterWincor printerWincor = new DocumentPrinterWincor();

    @Test
    public void testReadAndTransformFile() throws Exception {
        byte[] bytes = printerWincor.readAndTransform("letter.txt", 10);
        // картинка 10x24 = 30 байт
        assertArrayEquals(new byte[] {
                0x00, 0x00, 0x00,
                0x0E, 0x3F, (byte) 0xC0,
                0x1C, 0x3F, (byte) 0xE0,
                0x18, 0x30, 0x60,
                0x30, 0x30, 0x30,
                0x30, 0x30, 0x30,
                0x18, 0x30, 0x60,
                0x1F, (byte) 0xFF, (byte) 0xE0,
                0x07, (byte) 0xFF, (byte) 0x80,
                0x00, 0x00, 0x00
        }, bytes);
    }
}