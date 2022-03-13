package ru.crystals.pos.visualization.products.spirits.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import ru.crystals.pos.egais.EGAISBridgeImpl;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.products.spirits.ResBundleGoodsSpirits;
import ru.crystals.test.MockInjectors;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by Dmitriy Shibaev (mailto: d.shibaev@crystals.ru) on 22.07.16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Factory.class)
public class SpiritProductControllerCheckExciseLengthTest {

    private TechProcessInterface techProcessInterface;

    private String validExcise68Barcode = "22N00001CQR8HZ384JY3TID411110630008226H2RCG4GXG6H62V4T7518GDLLZ7O3C1";
    private String validExcise40Barcode = "1234567890123456789012345678901234567890";
    private String invalidExciseBarcode = "20000004";

    private EGAISBridgeImpl egaisBridge;

    private SpiritProductController spiritProductController;

    @Before
    public void setUp() throws Exception {

        techProcessInterface = mock(TechProcessInterface.class);

        mockStatic(Factory.class);
        when(Factory.getTechProcessImpl()).thenReturn(techProcessInterface);

        spiritProductController = new SpiritProductController();

        injectEGAISBridgeImpl();

    }

    private void injectEGAISBridgeImpl() throws NoSuchFieldException, IllegalAccessException {
        Field egaisBridgeField = SpiritProductController.class.getDeclaredField("egaisBridge");
        egaisBridgeField.setAccessible(true);

        egaisBridge = new EGAISBridgeImpl();

        MockInjectors.injectField(egaisBridge, Pattern.compile("^(?=.*[a-zA-Z]+.*)([a-zA-Z0-9]{68})$"), "exciseBarcodePatternNew");

        MockInjectors.injectField(egaisBridge, Pattern.compile("\\d\\d[a-zA-Z0-9]{21}\\d[0-1]\\d[0-3]\\d{10}[a-zA-Z0-9]{31}|[a-zA-Z0-9]{40}"), "exciseBarcodePatternOld");

        egaisBridgeField.set(spiritProductController, egaisBridge);
    }

    @Test
    public void valid68ExciseBarcodeDoesNotStartBeeping() throws Exception {

        assertNull(spiritProductController.checkExciseLength(validExcise68Barcode));
        verify(techProcessInterface, never()).startCriticalErrorBeeping();

    }

    @Test
    public void valid40ExciseBarcodeDoesNotStartBeeping() throws Exception {
        MockInjectors.injectField(egaisBridge, false, "isValidateExciseMark");
        assertNull(spiritProductController.checkExciseLength(validExcise40Barcode));
        verify(techProcessInterface, never()).startCriticalErrorBeeping();

    }

    @Test
    public void invalid68ExciseBarcodeStartsBeeping() throws Exception {

        assertEquals(
                ResBundleGoodsSpirits.getString("SCAN_EXCISE_ERROR_AM"),
                spiritProductController.checkExciseLength(invalidExciseBarcode)
        );
        verify(techProcessInterface, atLeast(1)).startCriticalErrorBeeping();

    }

    @Test
    public void invalid40ExciseBarcodeStartsBeeping() throws Exception {
        MockInjectors.injectField(egaisBridge, false, "isValidateExciseMark");
        assertEquals(
                ResBundleGoodsSpirits.getString("SCAN_EXCISE_ERROR_AM"),
                spiritProductController.checkExciseLength(invalidExciseBarcode)
        );
        verify(techProcessInterface, atLeast(1)).startCriticalErrorBeeping();

    }

}