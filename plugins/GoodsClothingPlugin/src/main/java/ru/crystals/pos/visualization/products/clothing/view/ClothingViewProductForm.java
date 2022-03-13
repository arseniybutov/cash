package ru.crystals.pos.visualization.products.clothing.view;

import ru.crystals.pos.catalog.ProductClothingEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonProductForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductQuantityPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductSummPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductUnitPricePanel;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonViewProductHeaderPanel;

public class ClothingViewProductForm extends CommonProductForm<ProductClothingEntity, PositionEntity, CommonViewProductHeaderPanel, CommonProductUnitPricePanel, CommonProductSummPanel, CommonProductQuantityPanel> {

    public ClothingViewProductForm(XListener outerListener) {
        super(outerListener);
    }

    @Override
    public void showForm(ProductClothingEntity product, PositionEntity position) {
        super.showForm(product, position);

        headerPanel.setHeaderInfo(product);
        if(unitPanel!=null) {
            unitPanel.setProduct(product);
        }
        if(footerPanel!=null) {
            footerPanel.setQuantity(BigDecimal.valueOf(1));
            footerPanel.reset();
        }
        if(summPanel!=null) {
            updateSumm();
        }
    }

    @Override
    public CommonViewProductHeaderPanel createHeaderPanel() {
        return new CommonViewProductHeaderPanel();
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
        summPanel.updateSumm((unitPanel.getUnitPrice().multiply(footerPanel.getCurrentQuantity())).setScale(2, RoundingMode.HALF_EVEN));
    }

    @Override
    public BigDecimal getPrice() {
        return unitPanel.getUnitPrice();
    }

    @Override
    public BigDecimal getQuantity() {
        return footerPanel.getCurrentQuantity();
    }

    @Override
    public boolean dispatchKeyEvent(XKeyEvent e) {
        /*
         * Enter и Escape могут пробрасываться наружу (если вернуть false)
         * остальные нажатия обрабатываются внутри формы
         */
        if(footerPanel == null){
            return false;
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                return false;
            case KeyEvent.VK_ESCAPE:
                if (footerPanel.isReset()) {
                    return false;
                } else {
                    footerPanel.reset();
                    updateSumm();
                    return true;
                }
            default:
                footerPanel.keyPressedNew(e);
                updateSumm();
                return true;
        }
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
}