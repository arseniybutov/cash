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
import ru.crystals.pos.catalog.ProductSpiritsController;
import ru.crystals.pos.catalog.ProductSpiritsEntity;
import ru.crystals.pos.check.PositionSpiritsEntity;
import ru.crystals.pos.check.PurchaseExciseBottleEntity;
import ru.crystals.pos.egais.EGAISBridge;
import ru.crystals.pos.visualization.products.spirits.integration.SpiritPluginAdapter;
import ru.crystals.pos.visualization.products.spirits.model.SpiritProductModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.doReturn;

/**
 * Проверяет корректность работы костыля, обеспечивающего совместимость между оптовой продажей в МЧ с марками и древней визуалкой pos.
 * - Пришлось вмешиваться в количество прямо при добавлении. Другие варианты есть, но они не сильно лучше
 * - Количество проставляется равным числу бутылок
 * - Это должно работать только для акцизных позиций, которые не кит и пришли с мягкого чека
 */
@RunWith(MockitoJUnitRunner.class)
public class SpiritProductControllerSoftCheckQuantityTest extends SpiritProductControllerTestBase {

    private static final String SOFT_CHECK_NUMBER = "TEST_SOFTCHECK";

    @Spy
    private final SpiritProductController controller = new SpiritProductController();

    @Mock
    private ProductSpiritsController productConfig;

    @Mock
    private SpiritPluginAdapter adapter;

    @Mock
    private SpiritProductModel model;

    @Mock
    private EGAISBridge egaisBridge;

    private final PositionSpiritsEntity adapterPosition = new PositionSpiritsEntity();
    private final List<PurchaseExciseBottleEntity> bottles = createBottles();

    @Before
    public void setUp() {
        Whitebox.setInternalState(controller, "egaisBridge", egaisBridge);
        controller.setModel(model);
        controller.setAdapter(adapter);
        doReturn(adapterPosition).when(adapter).getPosition();
    }

    @Test
    public void shouldSetQuantityByBottlesCountForSoftCheckPositionIfNotKit() {
        // given
        doReturn(Mockito.mock(PositionSpiritsEntity.class)).when(model).getPosition();
        doReturn(Optional.empty()).when(productConfig).getAmrc(any(), anyBoolean());
        adapterPosition.setSoftCheckNumber(SOFT_CHECK_NUMBER);

        ProductSpiritsEntity product = getProduct(false, productConfig);
        Assert.assertFalse(product.isKit());

        // when
        controller.addSpiritPosition(product, BigDecimal.ONE, BigDecimal.valueOf(100), false, createBottles());

        // then
        ArgumentCaptor<PositionSpiritsEntity> captor = ArgumentCaptor.forClass(PositionSpiritsEntity.class);
        Mockito.verify(adapter).doPositionAdd(captor.capture());
        PositionSpiritsEntity actualPosition = captor.getValue();
        Assert.assertEquals(Long.valueOf(bottles.size() * 1000L), actualPosition.getQnty());
        Assert.assertEquals(2, bottles.size());
    }

    @Test
    public void shouldNotSetQuantityByBottlesCountForSoftCheckPositionIfKit() {
        // given
        doReturn(Mockito.mock(PositionSpiritsEntity.class)).when(model).getPosition();
        doReturn(Optional.empty()).when(productConfig).getAmrc(any(), anyBoolean());
        adapterPosition.setSoftCheckNumber(SOFT_CHECK_NUMBER);

        ProductSpiritsEntity product = getProduct(true, productConfig);
        Assert.assertTrue(product.isKit());

        // when
        controller.addSpiritPosition(product, BigDecimal.ONE, BigDecimal.valueOf(100), false, createBottles());

        // then
        ArgumentCaptor<PositionSpiritsEntity> captor = ArgumentCaptor.forClass(PositionSpiritsEntity.class);
        Mockito.verify(adapter).doPositionAdd(captor.capture());
        PositionSpiritsEntity actualPosition = captor.getValue();
        Assert.assertEquals(Long.valueOf(1000L), actualPosition.getQnty());
    }

    @Test
    public void shouldNotSetQuantityByBottlesCountForNonSoftCheckPositionIfNotKit() {
        // given
        doReturn(Mockito.mock(PositionSpiritsEntity.class)).when(model).getPosition();
        doReturn(Optional.empty()).when(productConfig).getAmrc(any(), anyBoolean());

        ProductSpiritsEntity product = getProduct(false, productConfig);
        Assert.assertFalse(product.isKit());

        // when
        controller.addSpiritPosition(product, BigDecimal.ONE, BigDecimal.valueOf(100), false, createBottles());

        // then
        ArgumentCaptor<PositionSpiritsEntity> captor = ArgumentCaptor.forClass(PositionSpiritsEntity.class);
        Mockito.verify(adapter).doPositionAdd(captor.capture());
        PositionSpiritsEntity actualPosition = captor.getValue();
        Assert.assertEquals(Long.valueOf(1000L), actualPosition.getQnty());
    }

    @Test
    public void shouldNotSetQuantityByBottlesCountForNonSoftCheckPositionIfKit() {
        // given
        doReturn(Mockito.mock(PositionSpiritsEntity.class)).when(model).getPosition();
        doReturn(Optional.empty()).when(productConfig).getAmrc(any(), anyBoolean());

        ProductSpiritsEntity product = getProduct(true, productConfig);
        Assert.assertTrue(product.isKit());

        // when
        controller.addSpiritPosition(product, BigDecimal.ONE, BigDecimal.valueOf(100), false, createBottles());

        // then
        ArgumentCaptor<PositionSpiritsEntity> captor = ArgumentCaptor.forClass(PositionSpiritsEntity.class);
        Mockito.verify(adapter).doPositionAdd(captor.capture());
        PositionSpiritsEntity actualPosition = captor.getValue();
        Assert.assertEquals(Long.valueOf(1000L), actualPosition.getQnty());
    }

    private List<PurchaseExciseBottleEntity> createBottles() {
        List<PurchaseExciseBottleEntity> excises = new ArrayList<>();
        excises.add(createPurchaseBottle(0L, EXCISE));
        excises.add(createPurchaseBottle(0L, EXCISE));
        return excises;
    }
}
