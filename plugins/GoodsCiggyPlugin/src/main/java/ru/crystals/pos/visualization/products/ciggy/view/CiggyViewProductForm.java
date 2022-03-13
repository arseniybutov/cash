package ru.crystals.pos.visualization.products.ciggy.view;

import ru.crystals.pos.catalog.ProductCiggyEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.localization.CoreResBundle;
import ru.crystals.pos.visualization.check.PositionTableSelectionListener;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonProductForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductQuantityPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductSummPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonViewProductHeaderPanel;
import ru.crystals.pos.visualization.products.ciggy.controller.CiggyPluginController;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class CiggyViewProductForm extends
    CommonProductForm<ProductCiggyEntity, PositionEntity, CommonViewProductHeaderPanel, CiggySelectUnitPricePanel, CommonProductSummPanel, CommonProductQuantityPanel>
    implements PositionTableSelectionListener {

    public CiggyViewProductForm(XListener outerListener) {
        super(outerListener);
    }

    @Override
    public void showForm(ProductCiggyEntity product, PositionEntity position) {
        super.showForm(product, position);

        headerPanel.setHeaderInfo(product);
        unitPanel.setProduct(product);
        footerPanel.setQuantity(BigDecimal.valueOf(1));
        updateSumm();
        footerPanel.reset();

    }

    @Override
    public CommonViewProductHeaderPanel createHeaderPanel() {
        return new CommonViewProductHeaderPanel();
    }

    @Override
    public CiggySelectUnitPricePanel createUnitPanel() {
        return new CiggySelectUnitPricePanel(CoreResBundle.getStringCommon("SELECT_PRICE"), true, this);
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

    public BigDecimal getPrice() {
        return unitPanel.getUnitPrice();
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
            return false;
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (footerPanel.isReset()) {
                return false;
            } else {
                footerPanel.reset();
                updateSumm();
                return true;
            }
        } else if ((e.getKeyCode() == KeyEvent.VK_UP) || (e.getKeyCode() == KeyEvent.VK_DOWN)) {
            unitPanel.keyPressed(e);
            updateSumm();
            return true;
        } else {
            boolean result = footerPanel.keyPressedNew(e);
            updateSumm();
            return result;
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

    @Override
    public void rowSelected(int row) {
        ((CiggyPluginController)controller).selectionPriceChangedInViewInfo(unitPanel.getSelectedPrice());
    }
    
}