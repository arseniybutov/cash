package ru.crystals.pos.visualization.products.spirits.controller;

import edu.emory.mathcs.backport.java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import ru.crystals.pos.check.PurchaseExciseBottleEntity;
import ru.crystals.pos.egais.EGAISBridgeImpl;
import ru.crystals.pos.egais.excise.validation.ExciseValidationImpl;
import ru.crystals.pos.egais.excise.validation.ExciseValidationProvider;
import ru.crystals.pos.egais.excise.validation.ds.ProductType;
import ru.crystals.pos.egais.excise.validation.external.ExternalValidationProviderConfiguration;
import ru.crystals.pos.techprocess.TechProcessEvents;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.products.spirits.ResBundleGoodsSpirits;
import ru.crystals.test.MockInjectors;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by Dmitriy Shibaev (mailto: d.shibaev@crystals.ru) on 22.07.16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Factory.class)
public class SpiritProductControllerCheckExciseBeforeAddTest {

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
        when(techProcessInterface.getTechProcessEvents()).thenReturn(mock(TechProcessEvents.class));

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

        ExciseValidationImpl egaisExciseValidation = new ExciseValidationImpl();

        ExciseValidationProvider provider = mock(ExciseValidationProvider.class);
        ExternalValidationProviderConfiguration settings = new ExternalValidationProviderConfiguration();
        settings.setEnabled(true);
        settings.init();

        doReturn(settings).when(provider).getSettings();
        doReturn(Collections.singleton(ProductType.SPIRITS)).when(provider).getValidatedProductTypes();

        Map<ProductType, ExciseValidationProvider> providers = new HashMap<>();
        providers.put(ProductType.SPIRITS, provider);

        MockInjectors.injectField(egaisExciseValidation, providers, "providers");

        Field egaisExciseCheckValidationField = SpiritProductController.class.getDeclaredField("egaisExciseCheckValidation");
        egaisExciseCheckValidationField.setAccessible(true);
        egaisExciseCheckValidationField.set(spiritProductController, egaisExciseValidation);

        Field techProcessField = ExciseValidationImpl.class.getDeclaredField("techProcess");
        techProcessField.setAccessible(true);
        techProcessField.set(egaisExciseValidation, techProcessInterface);
    }

    @Test
    public void scanningNullBarcodeLeadsToSCAN_EXCISE_ERROR() throws Exception {
        String result = spiritProductController.checkExciseBeforeAdd(null, null);
        assertEquals(result, ResBundleGoodsSpirits.getString("SCAN_EXCISE_ERROR_AM"));
    }

    @Test
    public void scanningValid68BarcodeWithNoBottlesStopsBeeping() throws Exception {
        String result = spiritProductController.checkExciseBeforeAdd(validExcise68Barcode, null);
        assertNull(result);
        verify(techProcessInterface, atLeastOnce()).stopCriticalErrorBeeping();
    }

    @Test
    public void scanningValid40BarcodeWithNoBottlesStopsBeeping() throws Exception {
        MockInjectors.injectField(egaisBridge, false, "isValidateExciseMark");
        String result = spiritProductController.checkExciseBeforeAdd(validExcise40Barcode, null);
        assertNull(result);
        verify(techProcessInterface, atLeastOnce()).stopCriticalErrorBeeping();
    }

    @Test
    public void scanningValid68BarcodeTwiceStartsBeeping() throws Exception {
        PurchaseExciseBottleEntity bottle = mock(PurchaseExciseBottleEntity.class);
        when(bottle.getExciseBarcode()).thenReturn(validExcise68Barcode);

        List<PurchaseExciseBottleEntity> bottles = Arrays.asList(new PurchaseExciseBottleEntity[]{bottle});
        String result = spiritProductController.checkExciseBeforeAdd(validExcise68Barcode, bottles);
        assertEquals(ResBundleGoodsSpirits.getString("SCAN_EXCISE_EXISTS"), result);
        verify(techProcessInterface, atLeastOnce()).startCriticalErrorBeeping();
    }

    @Test
    public void scanningValid40BarcodeTwiceStartsBeeping() throws Exception {
        PurchaseExciseBottleEntity bottle = mock(PurchaseExciseBottleEntity.class);
        MockInjectors.injectField(egaisBridge, false, "isValidateExciseMark");
        when(bottle.getExciseBarcode()).thenReturn(validExcise40Barcode);

        List<PurchaseExciseBottleEntity> bottles = Arrays.asList(new PurchaseExciseBottleEntity[]{bottle});
        String result = spiritProductController.checkExciseBeforeAdd(validExcise40Barcode, bottles);
        assertEquals(ResBundleGoodsSpirits.getString("SCAN_EXCISE_EXISTS"), result);
        verify(techProcessInterface, atLeastOnce()).startCriticalErrorBeeping();
    }

}