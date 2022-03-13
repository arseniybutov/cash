package ru.crystals.pos.visualization.products.clothing.view;

import ru.crystals.pos.catalog.ProductClothingEntity;
import ru.crystals.pos.check.PositionClothingEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.user.Right;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonProductForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDefaultProductHeaderPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductQuantityPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductSummPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductUnitPricePanel;
import ru.crystals.pos.visualization.products.ProductContainer.ProductState;
import ru.crystals.pos.visualization.products.clothing.model.ClothingProductModel;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;

/**
 * Форма "Введите количество"
 *
 * @author Tatarinov Eduard
 */
public class ClothingEnterQuantityForm extends CommonProductForm<ProductClothingEntity, PositionClothingEntity, CommonDefaultProductHeaderPanel, CommonProductUnitPricePanel, CommonProductSummPanel, CommonProductQuantityPanel> {
    private ClothingProductModel model;

    public ClothingEnterQuantityForm(XListener outerListener) {
        super(outerListener);
        this.setName(ClothingEnterQuantityForm.class.getName());
    }

    @Override
    public void showForm(ProductClothingEntity product, PositionClothingEntity position) {
        super.showForm(product, position);

        headerPanel.setHeaderInfo(product);
        unitPanel.setProduct(product);

        setScannedByBarcode(false);

        //товар найден по ШК и количество в ШК  не по умолчанию
        boolean isFoundByBarcode = product.isFoundByBarcode() && product.getBarCode() != null && product.getBarCode().getCount() != 1000L;

        setScannedByBarcode(isFoundByBarcode);

        //Флаг запрещает изменять количество
        allowUserInput &= !isFoundByBarcode;

        if (position != null) {
            if (position.getQnty() != null && !isScannedByBarcode()) {
                footerPanel.setQuantity(position.getQntyBigDecimal());
            }
            if(position.getPriceStartBigDecimal()!=null) {
                unitPanel.setUnitPrice(position.getPriceStartBigDecimal());
            }
            if(isFoundByBarcode){
                allowUserInput &= position.isCanChangeQnty();
                footerPanel.setQuantity(position.getQntyBigDecimal());
            }
        } else if(isFoundByBarcode){
            footerPanel.setQuantity(product.getBarCode().getCountBigDecimal());
        } else {
            footerPanel.setQuantity(BigDecimal.ONE);
        }
        updateSumm();
    }

    @Override
    public CommonDefaultProductHeaderPanel createHeaderPanel() {
        return new CommonDefaultProductHeaderPanel();
    }

    @Override
    public CommonProductUnitPricePanel createUnitPanel() {
        return new CommonProductUnitPricePanel(true);
    }

    @Override
    public CommonProductQuantityPanel createQuantityPanel() {
        return new CommonProductQuantityPanel();
    }

    @Override
    public CommonProductSummPanel createSummPanel() {
        return new CommonProductSummPanel();
    }

    private void updateSumm() {
        BigDecimal summ = getSumm();
        if (controller.getModel().getState()==ProductState.QUICK_EDIT) {
            controller.updateSumm(getSummDiff());
        } else {
            controller.updateSumm(summ);
        }
        summPanel.updateSumm(summ);
    }

    public BigDecimal getPrice() {
        return unitPanel.getUnitPrice();
    }

    @Override
    public BigDecimal getQuantity() {
        return footerPanel.getCurrentQuantity();
    }

    @Override
    public BigDecimal getQuantityDiff() {
        return footerPanel.getQuantityDiff();
    }

    @Override
    public boolean dispatchKeyEvent(XKeyEvent e) {
        /*
         * Enter и Escape могут пробрасываться наружу (если вернуть false)
         * остальные нажатия обрабатываются внутри формы
         */
        if (!isVisible()){
            return false;
        }

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if(getQuantity().compareTo(BigDecimal.ZERO)>0) {
                return false;
            }else{
                controller.beepError("Clothing: zero quantity not allowed!");
                return true;
            }
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (footerPanel.isReset()) {
                return false;
            } else {
                footerPanel.reset();
                updateSumm();
                e.setKeyCode(KeyEvent.VK_KANA);
                return false;
            }
        } else if (XKeyEvent.isCommaOrDigitOrBackspace(e)) {
            if (allowUserInput && Factory.getTechProcessImpl().checkUserRight(Right.CHANGE_POSITION_QUANTITY)) {
                footerPanel.keyPressedNew(e);
                updateSumm();
                e.setKeyCode(KeyEvent.VK_KANA);
                return false;
            } else if (controller != null) {
                controller.beepError("Clothing. Change quantity not allowed.");
                controller.sendEventChangeDenied(getQuantity());
            }
        }
        return false;
    }

    @Override
    protected boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        return false;
    }

    @Override
    protected boolean dispatchBarcodeEvent(String barcode) {
        return false;
    }

    @Override
    public void clear() {
    }

    public void setCurrentModel(ClothingProductModel model){
        this.model = model;
    }
}
