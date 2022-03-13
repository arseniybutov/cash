package ru.crystals.pos.visualization.products.giftcard.product;

import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonProductForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.AbstractProductUnitPriceComponent;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDefaultProductHeaderPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductSummPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductUnitPricePanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonViewProductHeaderPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonYesNoPanel;
import ru.crystals.pos.visualization.products.giftcard.ResBundleGoodsGiftCard;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;

public class GiftCardEditOrDeleteForm extends CommonProductForm<ProductEntity, PositionEntity,
        CommonDefaultProductHeaderPanel,
        AbstractProductUnitPriceComponent,
        CommonProductSummPanel,
        CommonYesNoPanel> {


    public GiftCardEditOrDeleteForm(XListener outerListener) {
        super(outerListener);
        GiftCardEditOrDeleteForm.this.setName("ru.crystals.pos.visualization.products.giftcard.GiftCardEditOrDeleteForm");
    }

    @Override
    public void showForm(ProductEntity product, PositionEntity position) {
        super.showForm(product, position);
        // тут идет редактирование позиции - продукт вытягиваем из позиции
        headerPanel.setHeaderInfo(position.getProduct());
        unitPanel.setVisible(false);
        summPanel.updateSumm(position.getPriceStartBigDecimal());

        footerPanel.reset();
        footerPanel.setNoEnabled(false);

    }

    @Override
    public CommonDefaultProductHeaderPanel createHeaderPanel() {
        return new CommonViewProductHeaderPanel(ResBundleGoodsGiftCard.getString("ITEM_EDIT"));
    }

    @Override
    public CommonYesNoPanel createQuantityPanel() {
        return new CommonYesNoPanel();
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
    public boolean dispatchKeyEvent(XKeyEvent e) {
        return false;
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
    public BigDecimal getQuantity() {
        return null;
    }

    @Override
    public BigDecimal getPrice() {
        return null;
    }

    @Override public void clear() {
    }
}
