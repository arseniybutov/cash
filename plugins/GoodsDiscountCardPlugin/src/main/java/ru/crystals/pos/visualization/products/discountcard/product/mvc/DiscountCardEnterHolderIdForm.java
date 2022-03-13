package ru.crystals.pos.visualization.products.discountcard.product.mvc;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;

import javax.swing.JPanel;

import org.apache.commons.lang.StringUtils;

import ru.crystals.pos.catalog.ProductDiscountCardEntity;
import ru.crystals.pos.check.PositionDiscountCardEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonProductForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDefaultProductHeaderPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductSummPanel;
import ru.crystals.pos.visualization.forms.input.EnterValueForm;
import ru.crystals.pos.visualization.input.AbstractInputPanel;
import ru.crystals.pos.visualization.products.discountcard.ResBundleGoodsDiscountCard;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualizationtouch.components.inputfield.KeyClass;

/**
 * Форма ввода идентификатора картоносца (например, номера анкеты) при продаже ДК (Дисконтной карты).
 * 
 * @author aperevozchikov
 */
// @formatter:off
public class DiscountCardEnterHolderIdForm extends CommonProductForm<ProductDiscountCardEntity, 
                                                                    PositionDiscountCardEntity, 
                                                                    CommonDefaultProductHeaderPanel, 
                                                                    JPanel, 
                                                                    CommonProductSummPanel, 
                                                                    EnterValueForm<String>> {
 // @formatter:on
    private static final long serialVersionUID = 1L;

    private DiscountCardController controller;

    public DiscountCardEnterHolderIdForm(XListener outerListener) {
        super(outerListener);
        DiscountCardEnterHolderIdForm.this.setName("ru.crystals.pos.visualization.products.discountcard.product.mvc.DiscountCardEnterHolderIdForm");
    }

    @Override
    public void showForm(ProductDiscountCardEntity product, PositionDiscountCardEntity position) {
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
    public EnterValueForm<String> createQuantityPanel() {
        return new EnterValueForm<>(null, new AbstractInputPanel<String>(null,
                ResBundleGoodsDiscountCard.getString("ENTER_HOLDER_ID_INPUT_FIELD_HEADER"),
                ResBundleGoodsDiscountCard.getString("ENTER_HOLDER_ID_WELCOME_TEXT"), false));
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
            if (StringUtils.isEmpty(footerPanel.getValue())) {
                if (getController() != null) {
                    getController().beepError(e.getSource().toString());
                }
                return true;
            }
            return false;
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (StringUtils.isEmpty(footerPanel.getValue())) {
                return false;
            } else {
                footerPanel.setValue("");
                return true;
            }
        } else if ((e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9) || (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) ||
            Character.isDigit(e.getKeyChar())) {
            // это ввод
            if (allowUserInput) {
                footerPanel.getInputPanel().press(new KeyClass(e.getKeyChar(), e.getKeyCode()));
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
        return false;
    }

    @Override
    protected boolean dispatchBarcodeEvent(String barcode) {
        footerPanel.setValue(barcode);
        return false;
    }

    @Override
    public BigDecimal getQuantity() {
        return BigDecimal.ONE;
    }

    @Override
    public void clear() {
        footerPanel.setValue("");
    }

    public String getEnteredNumber() {
        return footerPanel.getValue();
    }
    
    public void setInputPanelHeader(String header) {
        footerPanel.getAbstractInputPanel().setLabel(header);
    }
    
    public void setInputPanelWelcomeText(String hint) {
        footerPanel.getAbstractInputPanel().setWelcomeText(hint);
    }
}