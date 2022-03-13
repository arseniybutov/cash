package ru.crystals.pos.visualization.products.spirits.controller;

import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.catalog.PriceEntity;
import ru.crystals.pos.catalog.ProductConfig;
import ru.crystals.pos.catalog.ProductSpiritsBottleEntity;
import ru.crystals.pos.catalog.ProductSpiritsEntity;
import ru.crystals.pos.check.PositionSpiritsEntity;
import ru.crystals.pos.check.PurchaseExciseBottleEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tatarinov Eduard
 */
public class SpiritProductControllerTestBase {

    protected static final String EXCISE = "1234567890QWERTYUIOP";
    protected static final String BARCODE = "123456789";
    protected static final String PROMO_BARCODE = "987654321";
    protected static final String ITEM = "70000";
    protected static final String ALCOCODE = "12345";
    protected static final double VOLUME_BOTTLE = 0.5;
    protected static final long PRICE = 5000;

    protected PositionSpiritsEntity getPosition() {
        return getPosition(false);
    }

    protected PositionSpiritsEntity getPosition(boolean withProduct) {
        return getPosition(PRICE, withProduct);
    }

    protected PositionSpiritsEntity getPosition(Long price, boolean withProduct) {
        PositionSpiritsEntity result = new PositionSpiritsEntity();
        if (withProduct) {
            result.setProduct(getProduct(false));
        }
        result.setBarCode(BARCODE);
        result.setPriceEnd(price);
        return result;
    }

    protected PositionSpiritsEntity getKitPosition(Long price, List<PurchaseExciseBottleEntity> bottles) {
        PositionSpiritsEntity result = new PositionSpiritsEntity();
        result.setInMemoryBottles(bottles);
        result.setProduct(getProduct(true));
        result.setBarCode(PROMO_BARCODE);
        result.setPriceEnd(price);
        return result;
    }

    protected PositionSpiritsEntity getKitPosition() {
        PositionSpiritsEntity result = getKitPosition(PRICE, getExciseBottlesEcxiseIsNull());
        return result;
    }

    protected ProductSpiritsEntity getProduct(boolean isKit, ProductConfig productConfig) {
        ProductSpiritsEntity product = getProduct(isKit);
        product.setProductConfig(productConfig);
        return product;
    }

    protected ProductSpiritsEntity getProduct(boolean isKit) {
        PriceEntity priceEntity = new PriceEntity();
        priceEntity.setPrice(PRICE);

        ProductSpiritsEntity product = new ProductSpiritsEntity();
        if (isKit) {
            ProductSpiritsBottleEntity bottle = new ProductSpiritsBottleEntity();
            product.getBottles().add(bottle);
        }
        product.setPrice(priceEntity);
        product.setExcise(true);
        return product;
    }

    protected List<PurchaseExciseBottleEntity> getExciseBottles() {
        return getExciseBottles(null);
    }

    protected List<PurchaseExciseBottleEntity> getExciseBottles(Long alcoMinPrice) {
        return getExciseBottlesInner(alcoMinPrice, EXCISE);
    }

    protected List<PurchaseExciseBottleEntity> getExciseBottlesEcxiseIsNull() {
        return getExciseBottlesEcxiseIsNull(null);
    }

    protected List<PurchaseExciseBottleEntity> getExciseBottlesEcxiseIsNull(Long alcoMinPrice) {
        return getExciseBottlesInner(alcoMinPrice, null);
    }

    protected List<PurchaseExciseBottleEntity> getExciseBottlesEcxiseIsEmpty(Long alcoMinPrice) {
        return getExciseBottlesInner(alcoMinPrice, StringUtils.EMPTY);
    }

    private List<PurchaseExciseBottleEntity> getExciseBottlesInner(Long alcoMinPrice, String excise) {
        List<PurchaseExciseBottleEntity> excises = new ArrayList<>();
        excises.add(createPurchaseBottle(alcoMinPrice, excise));
        return excises;
    }

    protected PurchaseExciseBottleEntity createPurchaseBottle(Long alcoMinPrice, String excise) {
        return new PurchaseExciseBottleEntity(ITEM, BARCODE, excise, PRICE, VOLUME_BOTTLE, ALCOCODE, alcoMinPrice);
    }

}
