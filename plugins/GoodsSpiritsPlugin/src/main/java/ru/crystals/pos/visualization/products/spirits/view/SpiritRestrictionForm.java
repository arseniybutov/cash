package ru.crystals.pos.visualization.products.spirits.view;

import ru.crystals.pos.catalog.ProductSpiritsEntity;
import ru.crystals.pos.check.PositionSpiritsEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonProductForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDefaultProductHeaderPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductSummPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductUnitPricePanel;

import java.awt.event.KeyEvent;

/**
 * Форма показа алкогольных ограничений
 * С нее можно только выйти назад
 *
 * @author nbogdanov
 */
public class SpiritRestrictionForm extends CommonProductForm<
        ProductSpiritsEntity, PositionSpiritsEntity,
        CommonDefaultProductHeaderPanel,
        CommonProductUnitPricePanel,
        CommonProductSummPanel,
        SpiritWarnPanel>{

    public SpiritRestrictionForm(XListener outerListener) {
        super(outerListener);
    }

    @Override
    public void showForm(ProductSpiritsEntity product, PositionSpiritsEntity position) {
        super.showForm(product, position);
        headerPanel.setHeaderInfo(product);
    }

    @Override
    public CommonDefaultProductHeaderPanel createHeaderPanel() {
        return new CommonDefaultProductHeaderPanel();
    }

    @Override
    public SpiritWarnPanel createQuantityPanel() {
        return new SpiritWarnPanel();
    }

    @Override
    public CommonProductSummPanel createSummPanel() {
        return null;
    }

    @Override
    public CommonProductUnitPricePanel createUnitPanel() {
        return null;
    }

    @Override
    protected boolean dispatchKeyEvent(XKeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_ENTER){
            Factory.getTechProcessImpl().stopCriticalErrorBeeping();
        }
        return false;
    }

    @Override
    protected boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        return false;
    }

    @Override
    protected boolean dispatchBarcodeEvent(String barcode) {
        controller.beepError("Barcode not allowed in spirits restriction form.");
        return true;
    }

    @Override
    public void clear() {
        footerPanel.setWarningText("");
    }

    public void setWarnMessage(String message){
        footerPanel.setWarningText(message);
    }
}
