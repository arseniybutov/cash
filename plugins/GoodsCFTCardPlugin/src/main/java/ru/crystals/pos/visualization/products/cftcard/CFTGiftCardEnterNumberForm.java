package ru.crystals.pos.visualization.products.cftcard;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import javax.swing.JPanel;
import ru.crystals.pos.catalog.ProductCFTGiftCardEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonProductForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDefaultProductHeaderPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductCardNumberPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductUnitPricePanel;
import ru.crystals.pos.visualization.styles.Color;

public class CFTGiftCardEnterNumberForm extends CommonProductForm<ProductCFTGiftCardEntity,
        PositionEntity,
        CommonDefaultProductHeaderPanel,
        CommonProductUnitPricePanel,
        JPanel,
        CommonProductCardNumberPanel> {

    public CFTGiftCardEnterNumberForm(XListener outerListener) {
        super(outerListener);
        CFTGiftCardEnterNumberForm.this.setName("ru.crystals.pos.visualization.products.cftcard.CFTGiftCardEnterNumberForm");
    }

    @Override
    public void showForm(ProductCFTGiftCardEntity product, PositionEntity position) {
        super.showForm(product, position);
        
        if (product != null) {
            this.allowUserInput = product.getProductConfig().isAllowUserInput();
        }

        headerPanel.setHeaderInfo(product);
        unitPanel.setProduct(product);
        footerPanel.reset();
        if (position != null) {
            unitPanel.setUnitPrice(position.getPriceStartBigDecimal());
        }
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
    public CommonProductUnitPricePanel createUnitPanel() {
        return new CommonProductUnitPricePanel(ResBundleGoodsCFTGiftCard.getString("NOMINAL"));
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
                if (controller != null) {
                    controller.beepError(e.getSource().toString());
                }
                return true;
            }
            return false;
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (footerPanel.isReset()) {
                return false;
            } else {
                footerPanel.reset();
                controller.updateSumm(getSumm());
                return true;
            }
        } else if ((e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9) || (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) || Character.isDigit(e.getKeyChar())) {
            if (allowUserInput) {
                footerPanel.keyPressedNew(e);
                controller.updateSumm(getSumm());
            } else if (controller != null) {
                controller.beepError(e.getSource().toString());
            }
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
        footerPanel.barcodeScanned(barcode);
        return false;
    }

    @Override
    public BigDecimal getQuantity() {
        return BigDecimal.ONE;
    }

    @Override
    public BigDecimal getPrice() {
        if (product != null) {
            return product.getPrice().getPriceBigDecimal();
        }
        return BigDecimal.ONE;
    }

    @Override public void clear() {

    }

    public String getEnteredNumber() {
        return footerPanel.getCurrentNumber();
    }
}
