package ru.crystals.pos.fiscalprinter.pirit.core;

import java.awt.image.BufferedImage;
import org.junit.Before;
import org.junit.Test;
import ru.crystals.image.context.fiscal.FiscalDevice;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

import static org.mockito.Mockito.*;

public class ImagePrintingRoutineTest {

    private static final long ALIGN_VALUE = 22L;

    private ImagePrintingRoutine routine;

    private AbstractPirit pirit;

    @Before
    public void before() {
        pirit = mock(AbstractPirit.class);
        routine = new ImagePrintingRoutine(pirit);
    }

    @Test
    public void printImageTest() throws FiscalPrinterException {
        // given
        BufferedImage image = new BufferedImage(640, 400, BufferedImage.TYPE_BYTE_BINARY);
        when(pirit.getImagePrintingType()).thenReturn(null);
        // when
        routine.printImage(image, ALIGN_VALUE);
        // then
        verify(pirit).getImagePrintingType();
        verifyNoMoreInteractions(pirit);

        // given
        when(pirit.getImagePrintingType()).thenReturn(FiscalDevice.PIRIT_1);
        // when
        routine.printImage(image, ALIGN_VALUE);
        // then
        verify(pirit).printImageBase(anyLong(), anyLong(), eq(ALIGN_VALUE), any());

        // given
        when(pirit.getImagePrintingType()).thenReturn(FiscalDevice.PIRIT_2);
        // when
        routine.printImage(image, ALIGN_VALUE);
        // then
        verify(pirit).printImagePNGBase(anyLong(), anyLong(), eq(ALIGN_VALUE), any());
    }
}
