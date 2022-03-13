package ru.crystals.pos.fiscalprinter.pirit.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import org.junit.Test;
import ru.crystals.image.context.ImageConverter;
import ru.crystals.image.context.fiscal.FiscalDevice;

public class ImagePrintingTest {
    private ImageConverter pirit1Converter = new ImageConverter(FiscalDevice.PIRIT_1);
    private ImageConverter pirit2Converter = new ImageConverter(FiscalDevice.PIRIT_2);

    @Test
    public void testPirit1Dimenstions() {
        int maxWidth = 512;
        int maxHeight = 512;
        int maxRasterSize = 11 * 1024;

        BufferedImage bufferedImage = new BufferedImage(640, 400, BufferedImage.TYPE_BYTE_BINARY);
        BufferedImage converted = pirit1Converter.convertImage(bufferedImage, maxWidth, maxHeight);
        assertEquals(376, converted.getWidth());
        assertEquals(235, converted.getHeight());
        assertTrue((converted.getWidth() * converted.getHeight()) / 8 <= maxRasterSize);

        bufferedImage = new BufferedImage(288, 288, BufferedImage.TYPE_BYTE_BINARY);
        converted = pirit1Converter.convertImage(bufferedImage, maxWidth, maxHeight);
        assertEquals(288, converted.getWidth());
        assertEquals(288, converted.getHeight());
        assertTrue((converted.getWidth() * converted.getHeight()) / 8 <= maxRasterSize);

        bufferedImage = new BufferedImage(512, 30, BufferedImage.TYPE_BYTE_BINARY);
        converted = pirit1Converter.convertImage(bufferedImage, maxWidth, maxHeight);
        assertEquals(512, converted.getWidth());
        assertEquals(30, converted.getHeight());
        assertTrue((converted.getWidth() * converted.getHeight()) / 8 <= maxRasterSize);

        bufferedImage = new BufferedImage(13, 9, BufferedImage.TYPE_BYTE_BINARY);
        converted = pirit1Converter.convertImage(bufferedImage, maxWidth, maxHeight);
        assertEquals(8, converted.getWidth());
        assertEquals(6, converted.getHeight());
        assertTrue((converted.getWidth() * converted.getHeight()) / 8 <= maxRasterSize);

        bufferedImage = new BufferedImage(7, 3, BufferedImage.TYPE_BYTE_BINARY);
        converted = pirit1Converter.convertImage(bufferedImage, maxWidth, maxHeight);
        assertEquals(8, converted.getWidth());
        assertEquals(3, converted.getHeight());
        assertTrue((converted.getWidth() * converted.getHeight()) / 8 <= maxRasterSize);
    }

    @Test
    public void testPirit2Dimenstions() {
        int maxWidth = 512;
        int maxHeight = 512;
        int maxRasterSize = 11 * 1024;
        int maxAreaSize = 75000;

        BufferedImage bufferedImage = new BufferedImage(640, 400, BufferedImage.TYPE_BYTE_BINARY);
        BufferedImage converted = pirit2Converter.convertImage(bufferedImage, maxWidth, maxHeight);
        assertEquals(344, converted.getWidth());
        assertEquals(215, converted.getHeight());
        assertTrue((converted.getWidth() * converted.getHeight()) / 8 <= maxRasterSize);
        assertTrue(converted.getWidth() * converted.getHeight() <= maxAreaSize);

        bufferedImage = new BufferedImage(288, 288, BufferedImage.TYPE_BYTE_BINARY);
        converted = pirit2Converter.convertImage(bufferedImage, maxWidth, maxHeight);
        assertEquals(272, converted.getWidth());
        assertEquals(272, converted.getHeight());
        assertTrue((converted.getWidth() * converted.getHeight()) / 8 <= maxRasterSize);
        assertTrue(converted.getWidth() * converted.getHeight() <= maxAreaSize);

        bufferedImage = new BufferedImage(512, 30, BufferedImage.TYPE_BYTE_BINARY);
        converted = pirit2Converter.convertImage(bufferedImage, maxWidth, maxHeight);
        assertEquals(512, converted.getWidth());
        assertEquals(30, converted.getHeight());
        assertTrue((converted.getWidth() * converted.getHeight()) / 8 <= maxRasterSize);
        assertTrue(converted.getWidth() * converted.getHeight() <= maxAreaSize);

        bufferedImage = new BufferedImage(13, 9, BufferedImage.TYPE_BYTE_BINARY);
        converted = pirit2Converter.convertImage(bufferedImage, maxWidth, maxHeight);
        assertEquals(8, converted.getWidth());
        assertEquals(6, converted.getHeight());
        assertTrue((converted.getWidth() * converted.getHeight()) / 8 <= maxRasterSize);
        assertTrue(converted.getWidth() * converted.getHeight() <= maxAreaSize);

        bufferedImage = new BufferedImage(7, 3, BufferedImage.TYPE_BYTE_BINARY);
        converted = pirit2Converter.convertImage(bufferedImage, maxWidth, maxHeight);
        assertEquals(8, converted.getWidth());
        assertEquals(3, converted.getHeight());
        assertTrue((converted.getWidth() * converted.getHeight()) / 8 <= maxRasterSize);
        assertTrue(converted.getWidth() * converted.getHeight() <= maxAreaSize);
    }
}
