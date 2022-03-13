package ru.crystals.pos.visualization.products.giftcard.product;

import ru.crystals.pos.catalog.ProductGiftCardEntity;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonProductForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDefaultProductHeaderPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductCardNumberPanel;
import ru.crystals.pos.visualization.products.giftcard.product.controller.GiftCardPluginController;
import ru.crystals.pos.visualization.styles.Color;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;

public class GiftCardEnterNumberForm extends CommonProductForm<ProductGiftCardEntity,
        PositionEntity,
        CommonDefaultProductHeaderPanel,
        JPanel,
        JPanel,
        CommonProductCardNumberPanel> {
    private GiftCardPluginController controller;

    public GiftCardEnterNumberForm(XListener outerListener) {
        super(outerListener);
        GiftCardEnterNumberForm.this.setName("ru.crystals.pos.visualization.products.giftcard.GiftCardEnterNumberForm");
    }

    @Override
    public void showForm(ProductGiftCardEntity product, PositionEntity position) {
        super.showForm(product, position);
        if (product != null) {
            this.allowUserInput = product.getProductConfig().isAllowUserInput();
        }
        headerPanel.setHeaderInfo(product);
    }

    @Override
    public CommonDefaultProductHeaderPanel createHeaderPanel() {
        return new CommonDefaultProductHeaderPanel();
    }

    @Override
    public CommonProductCardNumberPanel createQuantityPanel() {
        return new CommonProductCardNumberPanel();
    }

    @Override
    public JPanel createSummPanel() {
        JPanel p = new JPanel();
        p.setBackground(Color.greyBackground);
        return p;
    }

    @Override
    public JPanel createUnitPanel() {
        JPanel p = new JPanel();
        p.setBackground(Color.greyBackground);
        return p;
    }

    @Override
    public boolean dispatchKeyEvent(XKeyEvent e) {
        /*
         * Enter и Escape могут пробрасываться наружу (если вернуть false)
         * остальные нажатия обрабатываются внутри формы
         */
        if (!isVisible()) return true;

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (footerPanel.getCurrentNumber() == null || footerPanel.getCurrentNumber().isEmpty()) {
                if (getController() != null) {
                    getController().beepError(e.getSource().toString());
                }
                return true;
            }
            return false;
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (footerPanel.isReset()) {
                return false;
            } else {
                footerPanel.reset();
                return true;
            }
        } else if ((e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9) || (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) || Character.isDigit(e.getKeyChar())) {
            //TODO помнить про конфиг :)
            if (allowUserInput) {
                footerPanel.keyPressedNew(e);
            } else if (getController() != null) {
                getController().beepError(e.getSource().toString());
            }
            return true;
        } else {
            return false;
        }
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
        boolean returnValue = false;
        footerPanel.barcodeScanned(barcode);
        return returnValue;
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
        footerPanel.reset();
    }

    public String getEnteredNumber() {
        return footerPanel.getCurrentNumber();
    }
}
