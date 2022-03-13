package ru.crystals.pos.visualization.products.spirits.controller;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.pos.barcodeprocessing.BarcodeProcessor;
import ru.crystals.pos.barcodeprocessing.processors.result.BarcodeProcessResult;
import ru.crystals.pos.barcodescanner.events.ScanOutOfCheckEvent;
import ru.crystals.pos.catalog.BarcodeEntity;
import ru.crystals.pos.catalog.PriceEntity;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.catalog.ProductSpiritsController;
import ru.crystals.pos.catalog.ProductSpiritsEntity;
import ru.crystals.pos.catalog.limits.ProductLimitsService;
import ru.crystals.pos.check.InsertType;
import ru.crystals.pos.check.PositionSpiritsEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.egais.EGAISBridge;
import ru.crystals.pos.techprocess.SearchCardScenarioInterface;
import ru.crystals.pos.techprocess.TechProcessEvents;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.check.CheckContainer;
import ru.crystals.pos.visualization.check.CheckDialogContainer;
import ru.crystals.pos.visualization.components.MainWindow;
import ru.crystals.pos.visualization.products.ProductContainer.ProductState;
import ru.crystals.pos.visualization.products.spirits.integration.SpiritPluginAdapter;
import ru.crystals.pos.visualization.products.spirits.model.SpiritProductModel;

import java.lang.reflect.Field;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class SpiritProductControllerTest {

    @Spy
    private SpiritProductController controller = new SpiritProductController();

    @Mock
    private ProductSpiritsController productConfig;

    @Mock
    private SpiritPluginAdapter adapter;

    @Mock
    private SpiritProductModel model;

    @Mock
    private EGAISBridge egaisBridge;

    @Mock
    private TechProcessInterface techProcessInterface;

    @Mock
    private ProductLimitsService alcoholLimitService;

    @Mock
    private PurchaseEntity purchaseEntity;

    @Mock
    private TechProcessEvents events;

    @Mock
    private MainWindow mainWindow;

    @Mock
    private CheckContainer checkContainer;

    @Mock
    private CheckDialogContainer dialogContainer;

    @Mock
    private BarcodeProcessor barcodeProcessor;

    @Before
    public void setUp() throws Exception {
        inject(Factory.class, techProcessInterface, "techProcessImpl");

        Whitebox.setInternalState(Factory.getInstance(), "barcodeProcessor", barcodeProcessor);
        Whitebox.setInternalState(Factory.getInstance(), "mainWindow", mainWindow);
        Whitebox.setInternalState(controller, "egaisBridge", egaisBridge);
        Whitebox.setInternalState(controller, "alcoholLimitService", alcoholLimitService);
        Whitebox.setInternalState(controller, "techProcessEvents", events);
        controller.setModel(model);
        controller.setAdapter(adapter);

        Mockito.doNothing().when(techProcessInterface).startCriticalErrorBeeping(anyString());
        Mockito.doReturn(purchaseEntity).when(techProcessInterface).getCheck();
        Mockito.when(mainWindow.getCheckContainer()).thenReturn(checkContainer);
        Mockito.when(checkContainer.getCheckDialogContainer()).thenReturn(dialogContainer);
        Mockito.when(model.getState()).thenReturn(ProductState.ADD);
        Mockito.when(techProcessInterface.searchCard(any(SearchCardScenarioInterface.class), anyString(), any(InsertType.class))).thenReturn(Boolean.FALSE);
        Mockito.doReturn(false).when(controller).currentProductIsMarked();
    }

    @Test
    public void shouldSetPriceEqualMinimalPriceRestriction() {
        long price = 50000;
        long minPrice = 60000;

        Mockito.doReturn(true).when(productConfig).isSetPriceEqualMinimalPriceRestrictions();
        controller.processProductAdd(createProduct(price, minPrice));

        ArgumentCaptor<PositionSpiritsEntity> captor = ArgumentCaptor.forClass(PositionSpiritsEntity.class);
        Mockito.verify(model).setPosition(captor.capture());
        Assert.assertEquals(minPrice, captor.getValue().getPriceStart().longValue());
    }

    @Test
    public void shouldNotChangePrice() {
        long price = 60000;
        long minPrice = 50000;

        Mockito.doReturn(true).when(productConfig).isSetPriceEqualMinimalPriceRestrictions();
        controller.processProductAdd(createProduct(price, minPrice));

        ArgumentCaptor<PositionSpiritsEntity> captor = ArgumentCaptor.forClass(PositionSpiritsEntity.class);
        Mockito.verify(model).setPosition(captor.capture());
        Assert.assertEquals(price, captor.getValue().getPriceStart().longValue());
    }

    @Test
    public void shouldNotChangePriceWhenDisabled() {
        long price = 50000;
        long minPrice = 60000;

        Mockito.doReturn(false).when(productConfig).isSetPriceEqualMinimalPriceRestrictions();
        controller.processProductAdd(createProduct(price, minPrice));

        ArgumentCaptor<PositionSpiritsEntity> captor = ArgumentCaptor.forClass(PositionSpiritsEntity.class);
        Mockito.verify(model).setPosition(captor.capture());
        Assert.assertEquals(price, captor.getValue().getPriceStart().longValue());
    }

    /**
     * Проверка на добавление позиции в чек с определенным количеством и возможностью изменять это количество
     */
    @Test
    public void changeQntyPosition() {
        long newQnty = 10;

        ProductEntity productEntity = createProduct(10000, 12000, false);
        productEntity.setBarCode(createBarcode("00001", newQnty));
        controller.processProductAdd(productEntity);

        ArgumentCaptor<PositionSpiritsEntity> captor = ArgumentCaptor.forClass(PositionSpiritsEntity.class);
        Mockito.verify(model).setPosition(captor.capture());
        Assert.assertEquals(newQnty, captor.getValue().getQnty().longValue());
        Assert.assertTrue(captor.getValue().isCanChangeQnty());
    }

    /**
     * Проверка на добавление позиции в чек с определенным количеством и невозможностью изменять это количество
     */
    @Test
    public void noChangeQntyPosition() {
        long newQnty = 10;

        ProductEntity productEntity = createProduct(10000, 12000, true);
        productEntity.setBarCode(createBarcode("00001", newQnty));
        controller.processProductAdd(productEntity);

        ArgumentCaptor<PositionSpiritsEntity> captor = ArgumentCaptor.forClass(PositionSpiritsEntity.class);
        Mockito.verify(model).setPosition(captor.capture());
        Assert.assertEquals(newQnty, captor.getValue().getQnty().longValue());
        Assert.assertFalse(captor.getValue().isCanChangeQnty());
    }

    /**
     * Проверка на добавление позиции в чек с нулевым количеством и возможностью изменять это количество
     */
    @Test
    public void changeQntyPositionWithNullCount() {
        long newQnty = 0;

        ProductEntity productEntity = createProduct(10000, 12000, true);
        productEntity.setBarCode(createBarcode("00001", newQnty));
        controller.processProductAdd(productEntity);

        ArgumentCaptor<PositionSpiritsEntity> captor = ArgumentCaptor.forClass(PositionSpiritsEntity.class);
        Mockito.verify(model).setPosition(captor.capture());
        Assert.assertEquals(null, captor.getValue().getQnty());
        Assert.assertTrue(captor.getValue().isCanChangeQnty());
    }

    @Test
    public void scanBarcodeIsCard() throws Exception {
        Mockito.when(techProcessInterface.searchCard(any(SearchCardScenarioInterface.class), anyString(), any(InsertType.class))).thenReturn(Boolean.TRUE);

        boolean result = controller.barcodeScanned("00001");

        Assert.assertTrue(result);
    }

    @Test
    public void scanBarcodeNoScanExciseLabelsMode() throws Exception {
        Mockito.when(model.isScanExciseLabelsMode()).thenReturn(Boolean.FALSE);

        boolean result = controller.barcodeScanned("00001");

        Assert.assertFalse(result);
        Mockito.verify(techProcessInterface, never()).startCriticalErrorBeeping(anyString());
        Mockito.verify(events, never()).publishEvent(any(ScanOutOfCheckEvent.class));
        Mockito.verify(techProcessInterface, never()).error(anyString());
    }

    @Test
    public void scanBarcodeIsProductAndIsScanExciseLabelsMode() throws Exception {
        Mockito.when(barcodeProcessor.processWithoutBeep(anyString(), any(Set.class), eq(false))).thenReturn(BarcodeProcessResult.createOkBarcodeProcessResult(new ProductEntity()));
        Mockito.when(model.isScanExciseLabelsMode()).thenReturn(Boolean.TRUE);

        boolean result = controller.barcodeScanned("00001");

        Assert.assertTrue(result);
        Mockito.verify(events).publishEvent(any(ScanOutOfCheckEvent.class));
        Mockito.verify(techProcessInterface, times(1)).error(anyString());
    }

    private ProductSpiritsEntity createProduct(long price, long minPrice, boolean isFoundByBarcode) {
        ProductSpiritsEntity productSpiritsEntity = createProduct(price, minPrice);
        productSpiritsEntity.setFoundByBarcode(isFoundByBarcode);
        return productSpiritsEntity;
    }

    private ProductSpiritsEntity createProduct(long price, long minPrice) {
        PriceEntity priceEntity = new PriceEntity();
        priceEntity.setPrice(price);

        ProductSpiritsEntity productEntity = new ProductSpiritsEntity();
        productEntity.setPrice(priceEntity);
        productEntity.setProductConfig(productConfig);
        productEntity.setMinimalPrice(minPrice);
        return productEntity;
    }

    private BarcodeEntity createBarcode(String barcode, long count) {
        BarcodeEntity barcodeEntity = createBarcode(barcode);
        barcodeEntity.setCount(count);
        return barcodeEntity;
    }

    private BarcodeEntity createBarcode(String barcode) {
        BarcodeEntity barcodeEntity = new BarcodeEntity();
        barcodeEntity.setBarCode(barcode);
        return barcodeEntity;
    }

    private void inject(Class<?> cls, Object objectToInject, String fieldName) throws Exception {
        Field field = cls.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, objectToInject);
    }
}
