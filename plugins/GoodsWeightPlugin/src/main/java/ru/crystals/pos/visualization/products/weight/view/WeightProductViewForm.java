package ru.crystals.pos.visualization.products.weight.view;

import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.localization.CoreResBundle;
import ru.crystals.pos.visualization.commonplugin.controller.ProductPluginController;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonProductForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.AbstractProductUnitPriceComponent;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductSummPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductUnitPricePanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductWeightPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonViewProductHeaderPanel;
import ru.crystals.pos.visualization.eventlisteners.WeightChangeEventListener;
import ru.crystals.pos.visualization.products.weight.controller.WeightProductController;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;

/**
 * Форма "Информация о товаре" для весового товара Никогда не запрещаем ввод с клавиатуры веса + одновременно слушаются данные с прикассовых весов
 */
public class WeightProductViewForm extends
        CommonProductForm<ProductEntity, PositionEntity, CommonViewProductHeaderPanel, AbstractProductUnitPriceComponent, CommonProductSummPanel,
                CommonProductWeightPanel> implements
        WeightChangeEventListener {

    public WeightProductViewForm(XListener outerListener) {
        super(outerListener);
        WeightProductViewForm.this.setName("ru.crystals.pos.visualization.products.weight.WeightProductViewForm");
    }

    @Override
    public void showForm(ProductEntity product, PositionEntity position) {
        super.showForm(product, position);

        headerPanel.setHeaderInfo(product);
        unitPanel.setProduct(product);
        footerPanel.reset();
        updateSumm();
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
    public CommonProductWeightPanel createQuantityPanel() {
        return new CommonProductWeightPanel(CoreResBundle.getStringCommon("ENTER_WEIGHT"), true);
    }

    @Override
    public CommonProductSummPanel createSummPanel() {
        return new CommonProductSummPanel();
    }

    private void updateSumm() {
        summPanel.updateSumm(CurrencyUtil.getPositionSum(getPrice(), getQuantity()));
    }

    @Override
    public BigDecimal getPrice() {
        return unitPanel.getUnitPrice();
    }

    @Override
    public void clear() {
        if (footerPanel != null) {
            footerPanel.clear();
        }
    }

    @Override
    public BigDecimal getQuantity() {
        return footerPanel.getCurrentWeight();
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
        } else {
            footerPanel.keyPressed(e);
            updateSumm();
            return true;
        }
    }

    @Override
    public void setController(ProductPluginController controller) {
        super.setController(controller);
        //подписываемся на внешнее событие изменение веса - это событие присылают прикассовые весы
        ((WeightProductController) controller).addWeightChangeListener(this);
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
    public void weightChange(BigDecimal weight) {
        if (isVisible()) {
            footerPanel.setWeight(weight);
            updateSumm();
        }
    }
}