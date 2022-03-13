package ru.crystals.pos.visualization.products.giftcard.product;

import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonProductForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.AbstractProductUnitPriceComponent;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductQuantityPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductSummPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductUnitPricePanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonViewProductHeaderPanel;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;

public class GiftCardViewForm extends CommonProductForm<ProductEntity,
        PositionEntity,
        CommonViewProductHeaderPanel,
        AbstractProductUnitPriceComponent,
        CommonProductSummPanel,
        CommonProductQuantityPanel> {

    public GiftCardViewForm(XListener outerListener) {
        super(outerListener);
        GiftCardViewForm.this.setName("ru.crystals.pos.visualization.products.giftcard.GiftCardViewForm");
    }

    @Override
    public void showForm(ProductEntity product, PositionEntity position) {
        super.showForm(product, position);

        headerPanel.setHeaderInfo(product);
        summPanel.setVisible(false);
        unitPanel.setVisible(false);
        footerPanel.setEnabled(false);
        footerPanel.reset();
    }

    @Override
    public CommonViewProductHeaderPanel createHeaderPanel() {
        return new CommonViewProductHeaderPanel();
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

    public BigDecimal getPrice() {
        return unitPanel.getUnitPrice();
    }

    @Override public void clear() {

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
        if (!isVisible()) return true;

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            return true;
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            return false;
        } else {
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
}