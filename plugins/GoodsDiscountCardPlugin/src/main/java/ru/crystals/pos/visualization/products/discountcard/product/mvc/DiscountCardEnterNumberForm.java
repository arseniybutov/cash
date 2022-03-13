package ru.crystals.pos.visualization.products.discountcard.product.mvc;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;

import javax.swing.JPanel;

import ru.crystals.pos.catalog.ProductDiscountCardEntity;
import ru.crystals.pos.check.PositionDiscountCardEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonProductForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDefaultProductHeaderPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductCardNumberPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductSummPanel;
import ru.crystals.pos.visualization.products.discountcard.ResBundleGoodsDiscountCard;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualizationtouch.components.inputfield.CardNumberFormatter;

/**
 * Форма ввода номера ДК (Дисконтной карты) при продаже.
 * 
 * @author aperevozchikov
 */
// @formatter:off
public class DiscountCardEnterNumberForm extends CommonProductForm<ProductDiscountCardEntity, 
                                                                    PositionDiscountCardEntity, 
                                                                    CommonDefaultProductHeaderPanel, 
                                                                    JPanel, 
                                                                    CommonProductSummPanel, 
                                                                    CommonProductCardNumberPanel> {
 // @formatter:on
    private static final long serialVersionUID = 1L;
    
    private DiscountCardController controller;

    public DiscountCardEnterNumberForm(XListener outerListener) {
        super(outerListener);
        DiscountCardEnterNumberForm.this.setName("ru.crystals.pos.visualization.products.discountcard.product.mvc.DiscountCardEnterNumberForm");
    }

    @Override
    public void showForm(ProductDiscountCardEntity product, PositionDiscountCardEntity position) {
        super.showForm(product, position);
        if (product != null) {
            this.allowUserInput = product.getProductConfig().isAllowUserInput();
        }
        headerPanel.setHeaderInfo(product);
        summPanel.updateSumm(getPositionSum());
    }

    @Override
    public BigDecimal getPrice() {
        return getPositionSum();
    }

    /**
     * Вернет суммарную стоимость добавляемой позиции (ДК)
     * 
     * @return {@code null}, если у товара нету цены
     */
    private BigDecimal getPositionSum() {
        return product == null || product.getPrice() == null ? null : product.getPrice().getPriceBigDecimal();
    }
    
    @Override
    public CommonDefaultProductHeaderPanel createHeaderPanel() {
        return new CommonDefaultProductHeaderPanel();
    }

    @Override
    public CommonProductCardNumberPanel createQuantityPanel() {
        CommonProductCardNumberPanel result = new CommonProductCardNumberPanel(true, new CardNumberFormatter(false, 0, 50));
        result.setInputFieldHeader(ResBundleGoodsDiscountCard.getString("CARD_TO_ACTIVATE_HEADER"));
        result.setWelcomeText(ResBundleGoodsDiscountCard.getString("CARD_TO_ACTIVATE_HINT"));
        return result;
    }

    @Override
    public CommonProductSummPanel createSummPanel() {
        return new CommonProductSummPanel();
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
         * Enter и Escape могут пробрасываться наружу (если вернуть false) остальные нажатия обрабатываются внутри формы
         */
        if (!isVisible())
            return true;

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
        } else if ((e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9) || (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) ||
            Character.isDigit(e.getKeyChar())) {
            if (allowUserInput) {
                footerPanel.keyPressedNew(e);
            } else if (getController() != null) {
                getController().beepError(e.getSource().toString());
            }
            return true;
        } else {
            return true;
        }
    }

    public DiscountCardController getController() {
        return controller;
    }

    public void setController(DiscountCardController controller) {
        this.controller = controller;
    }

    @Override
    protected boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        footerPanel.eventMSR(track1, track2, track3, track4);
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
    public void clear() {
        footerPanel.reset();
    }

    public String getEnteredNumber() {
        return footerPanel.getCurrentNumber();
    }

    /**
     * Требуется смена надписи в зависимости от того, ввод номера карты нужен при продаже или возврате.
     *
     * @param forReturn <tt>true</tt> если ввод номера нужен при возврате карты, <tt>false</tt> при покупке
     */
    void changeInputFieldHeader(boolean forReturn) {
        String header = forReturn ?
                ResBundleGoodsDiscountCard.getString("CARD_TO_DEACTIVATE_HEADER") :
                ResBundleGoodsDiscountCard.getString("CARD_TO_ACTIVATE_HEADER");
        footerPanel.setInputFieldHeader(header);
    }

}
