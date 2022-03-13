package ru.crystals.pos.visualization.products.spirits.controller;

import org.junit.Test;
import ru.crystals.pos.catalog.ProductSpiritsController;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Tatarinov Eduard
 */
public class SpiritProductControllerCheckAmrc extends SpiritProductControllerTestBase{

    private static final long AMRC = 5000L;
    private static final long PRICE_GT_KIT = 10000L;
    private static final long PRICE_GT = 6000L;
    private static final long PRICE_LE = 4000L;

    private ProductSpiritsController controller = new ProductSpiritsController();


    @Test
    public void checkAmrc() {
        boolean result = controller.checkAmrc(getPosition(PRICE_GT, true), AMRC);
        assertTrue(result);
    }

    @Test
    public void checkAmrcPriceLessAlcoMinPrice() {
        boolean result = controller.checkAmrc(getPosition(PRICE_LE, true), AMRC);
        assertFalse(result);
    }

    @Test
    public void checkAmrcAlcoMinPriceIsNull() {
        boolean result = controller.checkAmrc(getPosition(), null);
        assertTrue(result);
    }

    @Test
    public void checkAmrcPositionIsNull() {
        boolean result = controller.checkAmrc(null, AMRC);
        assertTrue(result);
    }

    @Test
    public void checkAmrcPositionIsKit() {
        boolean result = controller.checkAmrc(getKitPosition(PRICE_GT_KIT, getExciseBottles(PRICE)), AMRC);
        assertTrue(result);
    }

    @Test
    public void checkAmrcPositionIsKitPriceLessAlcoMinPrice() {
        boolean result = controller.checkAmrc(getKitPosition(PRICE_GT, getExciseBottles(PRICE)), AMRC);
        assertFalse(result);
    }

    @Test
    public void checkAmrcPositionIsKitInMemoryBottleIsNull() {
        boolean result = controller.checkAmrc(getKitPosition(PRICE_GT_KIT, null), AMRC);
        assertTrue(result);
    }

    @Test
    public void checkAmrcPositionIsKitInMemoryBottleIsEmpty() {
        boolean result = controller.checkAmrc(getKitPosition(PRICE_GT_KIT, Collections.EMPTY_LIST), AMRC);
        assertTrue(result);
    }

    @Test
    public void checkAmrcPositionIsKitInMemoryBottleAmrcNull() {
        boolean result = controller.checkAmrc(getKitPosition(PRICE_GT_KIT, getExciseBottles(null)), AMRC);
        assertTrue(result);
    }

    @Test
    public void checkAmrcPositionIsKitInMemoryBottleAmrcAndExciseBarcodeIsNull() {
        boolean result = controller.checkAmrc(getKitPosition(PRICE_GT_KIT, getExciseBottlesEcxiseIsNull(PRICE)), AMRC);
        assertTrue(result);
    }

    @Test
    public void checkAmrcPositionIsKitInMemoryBottleAmrcIsNullAndExciseBarcodeIsEmpty() {
        boolean result = controller.checkAmrc(getKitPosition(PRICE_GT_KIT, getExciseBottlesEcxiseIsEmpty(PRICE)), AMRC);
        assertTrue(result);
    }

}
