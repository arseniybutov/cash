package ru.crystals.pos.visualization.products.clothing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.crystals.pos.annotation.ConditionalOnProductTypeConfig;
import ru.crystals.pos.catalog.ProductClothingEntity;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.PositionClothingEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.exception.PositionalCouponException;
import ru.crystals.pos.cis.validation.CisValidation;
import ru.crystals.pos.cis.validation.CisValidationState;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.commonplugin.controller.AbstractProductController;
import ru.crystals.pos.visualization.products.ProductContainer;
import ru.crystals.pos.visualization.products.clothing.integration.ClothingPluginAdapter;
import ru.crystals.pos.visualization.products.clothing.model.ClothingProductModel;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;

/**
 * Контроллер товаров "Одежда"
 *
 * @author Tatarinov Eduard
 */
@Component
@ConditionalOnProductTypeConfig(typeName = ClothingPluginAdapter.PRODUCT_TYPE)
public class ClothingProductController extends AbstractProductController<ClothingProductModel> {
    private CisValidation cisValidation;

    @Autowired
    void setCisValidation(CisValidation cisValidation) {
        this.cisValidation = cisValidation;
    }

    public CisValidationState validation(String barcode, boolean isReturn) {
        if (isReturn) {
            return cisValidation.isCanReturn(barcode, null);
        }
        return cisValidation.isCanSale(barcode, getModel().getProduct());
    }

    @Override
    public void processProductAdd(ProductEntity product) {
        getModel().setScanExciseLabelsMode(isNeedToScanExciseLabels(product));

        //товар найден по ШК и количество в ШК  не по умолчанию
        boolean isFoundByBarcode = product.isFoundByBarcode()
                && product.getBarCode() != null
                && product.getBarCode().getCount() != 1000L;

        setScannedByBarcode(isFoundByBarcode);
        PositionClothingEntity posQuick = new PositionClothingEntity();
        posQuick.setProduct(product);
        posQuick.setPriceStart(product.getPrice().getPrice());
        posQuick.setPriceEnd(product.getPrice().getPrice());

        if (isFoundByBarcode) {
            if (product.getBarCode().getCount() == 0) {
                posQuick.setQnty(null);
                posQuick.setCanChangeQnty(true);
            } else {
                posQuick.setQnty(product.getBarCode().getCount());
                posQuick.setCanChangeQnty(false);
            }
        } else {
            posQuick.setQnty(1000L);
        }
        getModel().setPosition(posQuick);
        getModel().setProduct(product);
        getModel().setState(ProductContainer.ProductState.ADD);
        getModel().changed();
    }

    @Override
    public void processPositionDelete(PositionEntity position, ProductContainer.ProductState state) {
        getModel().setScanExciseLabelsMode(isNeedToScanExciseLabels(position.getProduct()));
        super.processPositionDelete(position, state);
    }

    @Override
    public void processPositionEdit(PositionEntity position) {
        getModel().setScanExciseLabelsMode(isNeedToScanExciseLabels(position.getProduct()));
        super.processPositionEdit(position);
    }

    @Override
    public void processPositionEditOrReturn(PositionEntity position) {
        getModel().setScanExciseLabelsMode(isNeedToScanExciseLabels(position.getProduct()));
        super.processPositionEditOrReturn(position);
    }

    /**
     * Необходимости сканирования КиЗ
     *
     * @param product
     * @return
     */
    private boolean isNeedToScanExciseLabels(ProductEntity product) {
        return ((ProductClothingEntity) product).isCisable();
    }

    /**
     * Переопределен метод добавления позиций
     *
     * @param product
     * @param quantity
     * @param price
     * @param addedManually
     * @return
     */
    public boolean addClothesPosition(ProductEntity product, BigDecimal quantity, BigDecimal price, boolean addedManually) {
        PositionClothingEntity posEntity = new PositionClothingEntity();
        try {
            posEntity.setAddedToCheckManually(addedManually);
            checkInputDataBeforeAdd(product, quantity, price);
            fillDefaultPosition(quantity, price, product, posEntity);
            if (getCis() != null) {
                posEntity.setCis(getCis());
                getModel().setCis(null);
            }
            if ((product.isFoundByBarcode() && product.getBarCode() != null && (product.getBarCode().getCount() != 0 && product.getBarCode().getCount() != 1000))
                    || isScannedByBarcode()) {
                posEntity.setCanChangeQnty(false);
            }
            if (getAdapter().doPositionAdd(posEntity)) {
                // если все ОК - корректно выйдем из плагина
                getAdapter().dispatchCloseEvent(true);
                return true;
            }
            return false;
        } catch (PositionalCouponException ex) {
            getAdapter().beepError("Cannot add position: " + posEntity.toString() + " error msg:" + ex.getMessage());
            Factory.getInstance().showMessage(ex.getMessage());
            return false;
        } catch (Exception ex) {
            getAdapter().beepError("Cannot add position: " + posEntity.toString() + " error msg:" + ex.getMessage());
            return false;
        }
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        if (e.getGoodsCode() != null) {
            return true;
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_ENTER) {
            processEscPressEvent();
            return true;
        }
        return false;
    }

    @Override
    public boolean barcodeScanned(String barcode) {
        boolean foundByBarcode = super.barcodeScanned(barcode);
        if (!foundByBarcode) {
            if (getModel().isScanExciseLabelsMode()) {
                beepError("Try scan barcode on clothing plugin with cis");
                return true;
            }
            return false;
        } else {
            return true;
        }
    }

    public void setCis(String cisCode) {
        getModel().setCis(cisCode);
    }

    public String getCis() {
        return getModel().getCis();
    }
}
