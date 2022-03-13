package ru.crystals.pos.visualization.products.weight.view;

import ru.crystals.pos.catalog.ProductWeightEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.commonplugin.controller.ProductPluginController;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonProductForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductQuantityPanel;
import ru.crystals.pos.visualization.products.weight.ResBundleGoodsWeight;
import ru.crystals.pos.visualization.products.weight.controller.WeightProductController;
import ru.crystals.pos.visualization.products.weight.tare.Tare;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;

public class WeightProductSelectTareForm extends
        CommonProductForm<ProductWeightEntity, PositionEntity, WeightProductSelectTareHeaderPanel, WeightProductSelectTarePanel, WeightProductSumAndPricePanel,
                CommonProductQuantityPanel> {
    public WeightProductSelectTareForm(XListener outerListener) {
        super(outerListener);
        setName(WeightProductSelectTareForm.class.getName());
    }

    @Override
    public void showForm(ProductWeightEntity product, PositionEntity position) {
        super.showForm(product, position);
        refreshTare(product);
        summPanel.updateSum(product.getPrice().getPriceBigDecimal().multiply(product.getWeightBigDecimal()));

    }

    @Override
    public WeightProductSelectTareHeaderPanel createHeaderPanel() {
        return new WeightProductSelectTareHeaderPanel(ResBundleGoodsWeight.getString("MANUAL_TARING"));
    }

    public void refreshTare(ProductWeightEntity product) {
        unitPanel.refreshTare(product);
    }

    @Override
    public WeightProductSelectTarePanel createUnitPanel() {
        unitPanel = new WeightProductSelectTarePanel();
        return unitPanel;
    }

    @Override
    public CommonProductQuantityPanel createQuantityPanel() {
        return null;
    }

    @Override
    public WeightProductSumAndPricePanel createSummPanel() {
        return new WeightProductSumAndPricePanel();
    }

    @Override
    public BigDecimal getPrice() {
        return product.getPrice().getPriceBigDecimal();
    }

    public Tare getTare() {
        return unitPanel.getSelectedTare();
    }

    public boolean isTareSelected() {
        return getTare() != null && getTare().getWeight().compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public BigDecimal getQuantity() {
        return product.getWeightBigDecimal().subtract(getTare().getWeight());
    }

    @Override
    protected boolean dispatchKeyEvent(XKeyEvent e) {
        /*
         * Enter и Escape могут пробрасываться наружу (если вернуть false)
         * остальные нажатия обрабатываются внутри формы
         */
        if (!isVisible()) {
            return true;
        }
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            position.setQntyBigDecimal(product.getWeightBigDecimal().subtract(unitPanel.getSelectedTare().getWeight()));
            return false;
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            product.setWeightBigDecimal(BigDecimal.ZERO);
            return false;
        }
        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
            unitPanel.keyPressed(e);
            updateWeight(product.getWeightBigDecimal().subtract(unitPanel.getSelectedTare().getWeight()));
            e.setKeyCode(KeyEvent.VK_KANA);
        }
        return false;
    }

    private void updateWeight(BigDecimal weight) {
        if (product != null && weight.compareTo(BigDecimal.ZERO) > 0) {
            summPanel.updateWeight(weight);
            summPanel.updateSum(getSumm());
            controller.updateSumm(getSumm());
            ((WeightProductController) controller).sendChangeWeightEvent(weight);
        }
    }

    @Override
    public BigDecimal getSumm() {
        return getPrice() != null ? CurrencyUtil.getPositionSum(getPrice(), getQuantity()) : BigDecimal.ZERO;
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
        unitPanel.clear();
    }

    public void setProduct(ProductWeightEntity product) {
        this.product = product;
        summPanel.updateWeight(product.getWeightBigDecimal());
        summPanel.updateSum(product.getPrice().getPriceBigDecimal());
    }

    @Override
    public void setController(ProductPluginController controller) {
        super.setController(controller);
    }

    public boolean isTareAvailable() {
        return unitPanel.isTareAvailable();
    }
}
