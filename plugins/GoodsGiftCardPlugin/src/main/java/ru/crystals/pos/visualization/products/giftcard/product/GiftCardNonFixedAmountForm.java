package ru.crystals.pos.visualization.products.giftcard.product;

import ru.crystals.cards.presentcards.PresentCardInformationVO;
import ru.crystals.pos.catalog.ProductGiftCardEntity;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonProductForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDefaultProductHeaderPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductInputPanel;
import ru.crystals.pos.visualization.products.giftcard.ResBundleGoodsGiftCard;
import ru.crystals.pos.visualization.products.giftcard.product.panel.NonFixedGiftCardMaxAmountPanel;
import ru.crystals.pos.visualization.products.giftcard.product.panel.NonFixedGiftCardMultiplicityPanel;
import ru.crystals.pos.visualizationtouch.components.inputfield.InputFieldListener;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;

public class GiftCardNonFixedAmountForm extends CommonProductForm<ProductGiftCardEntity,
        PositionEntity,
        CommonDefaultProductHeaderPanel,
        NonFixedGiftCardMultiplicityPanel,
        NonFixedGiftCardMaxAmountPanel,
        CommonProductInputPanel> {
    
    private BigDecimal maxValue = null;
    private TechProcessInterface tp;

    public GiftCardNonFixedAmountForm(XListener outerListener) {
        super(outerListener);
        GiftCardNonFixedAmountForm.this.setName("ru.crystals.pos.visualization.products.giftcard.GiftCardNonFixedAmountForm");
        footerPanel.getInputField().addInputFieldListener((InputFieldListener<BigDecimal>) (target, data) -> {
            if (maxValue != null && data.compareTo(maxValue) > 0) {
                Factory.getTechProcessImpl().startCriticalErrorBeeping();
            } else {
                Factory.getTechProcessImpl().stopCriticalErrorBeeping();
            }
        });
    }

    @Override
    public void showForm(ProductGiftCardEntity product, PositionEntity position) {
        super.showForm(product, position);
        allowUserInput = true;
        headerPanel.setHeaderInfo(product);
    }

    @Override
    public CommonDefaultProductHeaderPanel createHeaderPanel() {
        return new CommonDefaultProductHeaderPanel();
    }

    @Override
    public CommonProductInputPanel createQuantityPanel() {
        return new CommonProductInputPanel(CommonProductInputPanel.InputType.SUMM,
                                           ResBundleGoodsGiftCard.getString("SUMMA_LABEL"),
                                           ResBundleGoodsGiftCard.getString("SUMMA_LABEL"));
    }

    @Override
    public NonFixedGiftCardMaxAmountPanel createSummPanel() {
        return new NonFixedGiftCardMaxAmountPanel();
    }

    @Override
    public NonFixedGiftCardMultiplicityPanel createUnitPanel() {
        return new NonFixedGiftCardMultiplicityPanel();
    }

    @Override
    public boolean dispatchKeyEvent(XKeyEvent e) {
        /*
         * Enter и Escape могут пробрасываться наружу (если вернуть false)
         * остальные нажатия обрабатываются внутри формы
         */
        if (!isVisible()) return true;

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (getPriceLong() == 0) {
                if (getController() != null) {
                    getController().beepError(e.getSource().toString());
                }
                return true;
            }
            return false;
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (footerPanel.isClean()) {
                return false;
            } else {
                footerPanel.clear();
                return true;
            }
        } else if ((e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9) || (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) || Character.isDigit(e.getKeyChar())) {
            //TODO помнить про конфиг :)
            if (allowUserInput) {
                footerPanel.keyPressed(e);
            } else if (getController() != null) {
                getController().beepError(e.getSource().toString());
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
        return false;
    }

    @Override
    public BigDecimal getQuantity() {
        return BigDecimal.ONE;
    }

    @Override
    public BigDecimal getPrice() {
        return footerPanel.getDoubleValue();
    }

    public Long getPriceLong() {
        return BigDecimalConverter.convertMoney(getPrice());
    }

    @Override public void clear() {
    }

    public void setCardInfo(PresentCardInformationVO presentCard) {
        unitPanel.setMultiplicity(presentCard.getMultiplicity());
        summPanel.setMaxAmount(presentCard.getMaxAmount());
        
        // SRL-459 При превышениии суммы теперь издаем звуковой сигнал и не сбрасываем сумму
        //footerPanel.setMaximumValue(BigDecimalConverter.convertMoney(presentCard.getMaxAmount()));
        if (presentCard.getMaxAmount() != null) {
            maxValue = BigDecimalConverter.convertMoney(presentCard.getMaxAmount());
        } else {
            maxValue = null;
        }        
        footerPanel.clear();
    }
}
