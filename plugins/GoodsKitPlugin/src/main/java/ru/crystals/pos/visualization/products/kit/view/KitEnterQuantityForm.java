package ru.crystals.pos.visualization.products.kit.view;

import ru.crystals.pos.catalog.ProductKitEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.user.Right;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductQuantityPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductSummPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductUnitPricePanel;
import ru.crystals.pos.visualization.products.ProductContainer;
import ru.crystals.pos.visualization.products.kit.model.KitPluginModel;
import ru.crystals.pos.visualization.utils.ScaleDimension;

import javax.swing.Box;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;

public class KitEnterQuantityForm extends CommonForm<ProductKitEntity, PositionEntity> {

    private KitProductHeaderPanel headerPanel;
    private ComponentsPanel components;
    private CommonProductQuantityPanel quantityPanel;
    private CommonProductSummPanel summPanel;
    private CommonProductUnitPricePanel unitPanel;
    private KitPluginModel kitPluginModel;

    public KitEnterQuantityForm(XListener outerListener) {
        super(outerListener);
        setLayout(new BorderLayout());

        headerPanel = new KitProductHeaderPanel();
        unitPanel = new CommonProductUnitPricePanel(true);
        components = new ComponentsPanel();
        summPanel = new CommonProductSummPanel();
        summPanel.setPreferredSize(new ScaleDimension(280, 80));
        summPanel.getjSummaLabel().setPreferredSize(new ScaleDimension(280, 80));
        quantityPanel = new CommonProductQuantityPanel();

        Box headerBox = Box.createHorizontalBox();
        headerBox.setPreferredSize(new ScaleDimension(530, 63));
        headerBox.add(headerPanel);
        headerBox.add(unitPanel);

        add(headerBox, BorderLayout.NORTH);
        add(components, BorderLayout.WEST);
        add(summPanel, BorderLayout.EAST);
        add(quantityPanel, BorderLayout.SOUTH);
    }

    @Override
    public void showForm(ProductKitEntity product, PositionEntity position) {
        super.showForm(product, position);
        headerPanel.setHeaderInfo(product);
        kitPluginModel = (KitPluginModel) getModel();
        components.setModel(kitPluginModel);
        components.showComponents();
        unitPanel.setProduct(product);
        summPanel.updateSumm(kitPluginModel.getPrice());
        unitPanel.setUnitPrice(kitPluginModel.getPrice());

        //товар найден по ШК и количество в ШК  не по умолчанию
        boolean isFoundByBarcode = product.isFoundByBarcode() && product.getBarCode() != null && product.getBarCode().getCount() != 1000L;

        controller.setScannedByBarcode(isFoundByBarcode);

        if (position != null) {
            quantityPanel.setQuantity(position.getQntyBigDecimal());
        } else if (isFoundByBarcode) {
            quantityPanel.setQuantity(product.getBarCode().getCountBigDecimal());
        } else {
            quantityPanel.setQuantity(BigDecimal.ONE);
        }

        updateSumm();
    }

    @Override
    protected boolean dispatchKeyEvent(XKeyEvent e) {
        if ((e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9)
                || (e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
                || Character.isDigit(e.getKeyChar())) {

            if (Factory.getTechProcessImpl().checkUserRight(Right.CHANGE_POSITION_QUANTITY)) {
                quantityPanel.keyPressedNew(e);
                changeQuantity();
                updateSumm();
                return true;
            } else {
                controller.beepError("Kit. Change quantity not allowed.");
                controller.sendEventChangeDenied(getQuantity());
            }
        }

        return false;
    }

    private void changeQuantity() {
        components.changeQuantity(quantityPanel.getCurrentQuantity());
    }

    private void updateSumm() {
        BigDecimal summ = getSumm();
        if (controller.getModel().getState() == ProductContainer.ProductState.QUICK_EDIT) {
            controller.updateSumm(getSummDiff());
        } else {
            controller.updateSumm(summ);
        }

        summPanel.updateSumm(summ);
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
    public BigDecimal getSumm() {
        return getPrice() != null ? CurrencyUtil.getPositionSum(getPrice(), getQuantity()) : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getQuantity() {
        return quantityPanel.getCurrentQuantity();
    }

    @Override
    public BigDecimal getQuantityDiff() {
        return quantityPanel.getQuantityDiff();
    }

    @Override
    public BigDecimal getPrice() {
        return unitPanel.getUnitPrice();
    }

    @Override
    public void clear() {
    }
}
