package ru.crystals.pos.visualization.products.spirits.view;

import ru.crystals.pos.catalog.ProductSpiritsEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.localization.CoreResBundle;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonProductForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.AbstractProductUnitPriceComponent;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDefaultProductHeaderPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductQuantityPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductSummPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductUnitPricePanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonViewProductHeaderPanel;
import ru.crystals.pos.visualization.products.ProductContainer;
import ru.crystals.pos.visualization.products.spirits.controller.SpiritProductController;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;

public class SpiritEditForm extends CommonProductForm<
        ProductSpiritsEntity,
        PositionEntity,
        CommonDefaultProductHeaderPanel,
        AbstractProductUnitPriceComponent,
        CommonProductSummPanel,
        CommonProductQuantityPanel> {

    public SpiritEditForm(XListener outerListener) {
        super(outerListener);
        SpiritEditForm.this.setName(SpiritEditForm.class.getName());
    }

    @Override
    public void showForm(ProductSpiritsEntity product, PositionEntity position) {
        super.showForm(product, position);
        headerPanel.setHeaderInfo(position.getProduct());
        unitPanel.setUnitPrice(position.getPriceStartBigDecimal());
        footerPanel.setQuantity(position.getQntyBigDecimal());
    }

    @Override
    public CommonViewProductHeaderPanel createHeaderPanel() {
        return new CommonViewProductHeaderPanel(CoreResBundle.getStringCommon("QUANTITY_CHANGING"));
    }

    @Override
    public AbstractProductUnitPriceComponent createUnitPanel() {
        return new CommonProductUnitPricePanel(false);
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
        summPanel.updateSumm(getSumm());
    }

    public BigDecimal getPrice() {
        return unitPanel.getUnitPrice();
    }

    @Override
    public void clear() {
        footerPanel.clear();
    }

    public BigDecimal getQuantity() {
        return footerPanel.getCurrentQuantity();
    }

    @Override
    public boolean dispatchKeyEvent(XKeyEvent e) {
        /*
         * Enter и Escape могут пробрасываться наружу (если вернуть false)
         * остальные нажатия обрабатываются внутри формы
         */
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            return processEnterKey();
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {

            if (footerPanel.isReset()) {
                return false;
            } else {
                footerPanel.reset();
                updateSumm();
                return true;
            }
        } else if (footerPanel.getDefaultQuantity() != null) {
            footerPanel.keyPressedNew(e);
            updateSumm();
            return true;
        }
        return true;
    }

    private boolean processEnterKey() {
        if (changeQuantityDenied()) {
            return !controller.isPossibleToChangePositionQuantity(footerPanel.getDefaultQuantity(), getQuantity());
        }

        if (((SpiritProductController) controller).compareExcisePositionQuantity(footerPanel.getDefaultQuantity(),
                footerPanel.getInputFieldValue(), controller.getModel().getState().equals(ProductContainer.ProductState.EDIT_OR_DELETE))) {
            return false;
        }

        setDefaultQuantity();
        controller.beepError("Spirit: zero quantity not allowed!");
        return false;
    }

    private void setDefaultQuantity() {
        footerPanel.setQuantity(footerPanel.getDefaultQuantity());
        updateSumm();
    }

    private boolean changeQuantityDenied() {
        if (getQuantity().intValue() <= 0) {
            return false;
        }
        return footerPanel.getDefaultQuantity() == null || product.getProductConfig().getChangeExciseQnty()
                || !isExciseProductSpirits(product);
    }

    private boolean isExciseProductSpirits(ProductSpiritsEntity product) {
        return (product.isExcise() || ((product.isKit() && !product.getExciseBottles().isEmpty())));
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
    public BigDecimal getQuantityDiff() {
        return footerPanel.getQuantityDiff();
    }
}