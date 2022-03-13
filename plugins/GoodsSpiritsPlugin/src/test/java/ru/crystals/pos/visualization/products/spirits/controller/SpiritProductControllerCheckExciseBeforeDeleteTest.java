package ru.crystals.pos.visualization.products.spirits.controller;

import edu.emory.mathcs.backport.java.util.Arrays;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import ru.crystals.pos.check.PurchaseExciseBottleEntity;
import ru.crystals.pos.egais.EGAISBridgeImpl;
import ru.crystals.pos.egais.excise.validation.ExciseValidationImpl;
import ru.crystals.pos.egais.excise.validation.ExciseValidationProvider;
import ru.crystals.pos.egais.excise.validation.ds.ProductType;
import ru.crystals.pos.egais.excise.validation.external.ExternalValidationProviderConfiguration;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.test.MockInjectors;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by Dmitriy Shibaev (mailto: d.shibaev@crystals.ru) on 22.07.16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Factory.class)
public class SpiritProductControllerCheckExciseBeforeDeleteTest {

    private TechProcessInterface techProcessInterface;

    private String validExcise68Barcode = "22N00001CQR8HZ384JY3TID411110630008226H2RCG4GXG6H62V4T7518GDLLZ7O3C1";
    private String validExcise40Barcode = "1234567890123456789012345678901234567890";
    private String invalidExciseBarcode = "20000004";

    private EGAISBridgeImpl egaisBridge;


    private SpiritProductController spiritProductController;

    @Rule
    public ExpectedException exception = ExpectedException.none();

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

        Field egaisExciseCheckValidationField = SpiritProductController.class.getDeclaredField("egaisExciseCheckValidation");
        egaisExciseCheckValidationField.setAccessible(true);
        ExciseValidationImpl egaisExciseValidation = new ExciseValidationImpl();

        ExciseValidationProvider provider = mock(ExciseValidationProvider.class);
        ExternalValidationProviderConfiguration settings = new ExternalValidationProviderConfiguration();
        settings.setEnabled(true);
        settings.init();

        doReturn(Collections.singleton(ProductType.SPIRITS)).when(provider).getValidatedProductTypes();

        doReturn(settings).when(provider).getSettings();

        Map<ProductType, ExciseValidationProvider> providers = new HashMap<>();
        providers.put(ProductType.SPIRITS, provider);

        MockInjectors.injectField(egaisExciseValidation, providers, "providers");

        egaisExciseCheckValidationField.set(spiritProductController, egaisExciseValidation);
    }

    @Test
    public void scanningInvalidBarcodeLeadsToException() throws Exception {

        exception.expect(Exception.class);
        exception.expectMessage(ResBundleVisualization.getString("WRONG_SPIRIT_STAMP_LENGTH"));

        spiritProductController.checkExciseBeforeDelete(invalidExciseBarcode, null, null);

    }

    @Test
    public void scanningSameValid68BarcodeTwiceLeadsToException() throws Exception {

        PurchaseExciseBottleEntity bottle = mock(PurchaseExciseBottleEntity.class);
        when(bottle.getExciseBarcode()).thenReturn(validExcise68Barcode);

        List<PurchaseExciseBottleEntity> bottles = Arrays.asList(new PurchaseExciseBottleEntity[] {bottle});

        exception.expect(Exception.class);
        exception.expectMessage(ResBundleVisualization.getString("SPIRIT_STAMP_ALREADY_SCANNED"));

        spiritProductController.checkExciseBeforeDelete(validExcise68Barcode, null, bottles);

    }

    @Test
    public void scanningSameValid40BarcodeTwiceLeadsToException() throws Exception {

        PurchaseExciseBottleEntity bottle = mock(PurchaseExciseBottleEntity.class);
        when(bottle.getExciseBarcode()).thenReturn(validExcise40Barcode);

        MockInjectors.injectField(egaisBridge, false, "isValidateExciseMark");

        List<PurchaseExciseBottleEntity> bottles = Collections.singletonList(bottle);

        exception.expect(Exception.class);
        exception.expectMessage(ResBundleVisualization.getString("SPIRIT_STAMP_ALREADY_SCANNED"));

        spiritProductController.checkExciseBeforeDelete(validExcise40Barcode, null, bottles);

    }
}