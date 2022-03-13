package ru.crystals.pos.visualization.products.kit.view;

import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.currency.CurrencyUtil;
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

import java.awt.event.KeyEvent;
import java.math.BigDecimal;

public class KitEditForm extends CommonProductForm<ProductEntity, PositionEntity, CommonDefaultProductHeaderPanel,
        AbstractProductUnitPriceComponent, CommonProductSummPanel, CommonProductQuantityPanel> {

    public KitEditForm(XListener outerListener) {
        super(outerListener);
        KitEditForm.this.setName("ru.crystals.pos.visualization.products.kit.view.KitEditForm");
    }

    @Override
    public void showForm(ProductEntity product, PositionEntity position) {
        super.showForm(product, position);

        headerPanel.setHeaderInfo(position.getProduct(), position);
        unitPanel.setPosition(position);
        footerPanel.setQuantity(position.getQntyBigDecimal());
        updateSumm();
    }

    private void updateSumm() {
        summPanel.updateSumm(getSumm());
    }

    @Override
    public BigDecimal getSumm() {
        return getPrice() != null ? CurrencyUtil.getPositionSum(getPrice(), getQuantity()) : BigDecimal.ZERO;
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
    public CommonDefaultProductHeaderPanel createHeaderPanel() {
        return new CommonViewProductHeaderPanel(CoreResBundle.getStringCommon("QUANTITY_CHANGING"));
    }

    @Override
    public CommonProductQuantityPanel createQuantityPanel() {
        return new CommonProductQuantityPanel();
    }

    @Override
    public CommonProductSummPanel createSummPanel() {
        return new CommonProductSummPanel();
    }

    @Override
    public AbstractProductUnitPriceComponent createUnitPanel() {
        return new CommonProductUnitPricePanel(false);
    }

    @Override
    protected boolean dispatchKeyEvent(XKeyEvent e) {

        //Нажатия клавиши Enter и Esc(в случае если ненадо отменить изменение кол-ва) прокидываем выше
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
        } else {
            if (allowUserInput) {
                footerPanel.keyPressedNew(e);
                updateSumm();
            }
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
        footerPanel.clear();
    }
}
