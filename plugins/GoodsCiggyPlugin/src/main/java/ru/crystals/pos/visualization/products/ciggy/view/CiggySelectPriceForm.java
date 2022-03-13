package ru.crystals.pos.visualization.products.ciggy.view;

import ru.crystals.pos.catalog.ProductCiggyEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.check.PositionTableSelectionListener;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonProductForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDefaultProductHeaderPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductQuantityPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductSummPanel;
import ru.crystals.pos.visualization.products.ciggy.ResBundleGoodsCiggy;
import ru.crystals.pos.visualization.products.ciggy.controller.CiggyPluginController;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Форма "Выберите цену" в сигаретных товарах.
 */
@SuppressWarnings("serial")
public class CiggySelectPriceForm extends CommonProductForm<ProductCiggyEntity, PositionEntity,
        CommonDefaultProductHeaderPanel, CiggySelectUnitPricePanel, CommonProductSummPanel, CommonProductQuantityPanel> implements PositionTableSelectionListener {
    public CiggySelectPriceForm(XListener outerListener) {
        super(outerListener);
        this.setName(CiggySelectPriceForm.class.getName());
    }

    @Override
    public void showForm(ProductCiggyEntity product, PositionEntity position) {
        super.showForm(product, position);

        headerPanel.setHeaderInfo(product);
        unitPanel.setProduct(product);

        this.add(headerPanel, BorderLayout.NORTH);
        this.add(unitPanel, BorderLayout.CENTER);
    }

    @Override
    public CommonDefaultProductHeaderPanel createHeaderPanel() {
        return new CommonDefaultProductHeaderPanel();
    }

    @Override
    public CiggySelectUnitPricePanel createUnitPanel() {
        unitPanel = new CiggySelectUnitPricePanel(ResBundleGoodsCiggy.getString("SELECT_PRODUCT_PRICE"), false, this);
        return unitPanel;
    }

    @Override
    public CommonProductQuantityPanel createQuantityPanel() {
        return null;
    }

    @Override
    public CommonProductSummPanel createSummPanel() {
        return null;
    }

    private void updateSumm() {
        summPanel.updateSumm((unitPanel.getUnitPrice().multiply(footerPanel.getCurrentQuantity())).setScale(2, RoundingMode.HALF_EVEN));
    }

    public BigDecimal getPrice() {
        return unitPanel.getUnitPrice();
    }

    public BigDecimal getMRP() {
        return unitPanel.getSelectedMRP();
    }

    public BigDecimal getQuantity() {
        return BigDecimal.ONE;
    }

    @Override
    public boolean dispatchKeyEvent(XKeyEvent e) {
        /*
         * Enter и Escape могут пробрасываться наружу (если вернуть false)
         * остальные нажатия обрабатываются внутри формы
         */
        if (!isVisible()){
            return true;
        }

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            return false;
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            return false;
        } else if ((e.getKeyCode() == KeyEvent.VK_UP) || (e.getKeyCode() == KeyEvent.VK_DOWN)) {
            unitPanel.keyPressed(e);
            e.setKeyCode(KeyEvent.VK_KANA);
            return false;
        } else {
            return false;
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
        ((CiggyPluginController)controller).selectionPriceChanged(unitPanel.getSelectedPrice());
    }
}
