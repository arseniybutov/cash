package ru.crystals.pos.visualization.products.giftcard.product;

import ru.crystals.pos.catalog.ProductGiftCardEntity;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonProductForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDefaultProductHeaderPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductInputPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductUnitPricePanel;
import ru.crystals.pos.visualization.products.giftcard.ResBundleGoodsGiftCard;
import ru.crystals.pos.visualization.products.giftcard.product.controller.GiftCardPluginController;
import ru.crystals.pos.visualization.styles.Color;
import javax.swing.*;
import java.math.BigDecimal;

public class GiftCardFixedAmountForm extends CommonProductForm<ProductGiftCardEntity,
        PositionEntity,
        CommonDefaultProductHeaderPanel,
        CommonProductUnitPricePanel,
        JPanel,
        CommonProductInputPanel> {

    private GiftCardPluginController controller;

    public GiftCardFixedAmountForm(XListener outerListener) {
        super(outerListener);
        GiftCardFixedAmountForm.this.setName("ru.crystals.pos.visualization.products.giftcard.GiftCardFixedAmountForm");
    }

    @Override
    public void showForm(ProductGiftCardEntity product, PositionEntity position) {
        super.showForm(product, position);

        headerPanel.setHeaderInfo(product);
        unitPanel.setVisible(false);

        footerPanel.setEnabled(false);
    }

    @Override
    public CommonDefaultProductHeaderPanel createHeaderPanel() {
        return new CommonDefaultProductHeaderPanel();
    }

    @Override
    public CommonProductInputPanel createQuantityPanel() {
        return new CommonProductInputPanel(CommonProductInputPanel.InputType.SUMM,
                                           ResBundleGoodsGiftCard.getString("NOMINAL"),
                                           ResBundleGoodsGiftCard.getString("NOMINAL"));
    }

    @Override
    public JPanel createSummPanel() {
        JPanel p = new JPanel();
        p.setBackground(Color.greyBackground);
        return p;
    }

    @Override
    public CommonProductUnitPricePanel createUnitPanel() {
        return new CommonProductUnitPricePanel(ResBundleGoodsGiftCard.getString("NOMINAL"));
    }

    @Override
    public boolean dispatchKeyEvent(XKeyEvent e) {
        /*
         * Enter и Escape могут пробрасываться наружу (если вернуть false)
         * остальные нажатия обрабатываются внутри формы
         */
        return !isVisible();
    }

    public GiftCardPluginController getController() {
        return controller;
    }

    public void setController(GiftCardPluginController controller) {
        this.controller = controller;
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
        return BigDecimal.ONE;
    }

    @Override
    public BigDecimal getPrice() {
        if (product != null && product.getAmount() != null) {
            return BigDecimalConverter.convertMoney(product.getAmount());
        }
        return BigDecimal.ZERO;
    }

    @Override public void clear() {
    }

    public void setAmount(Long amount) {
        footerPanel.setDoubleValue(BigDecimalConverter.convertMoney(amount));
    }
}
