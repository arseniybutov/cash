package ru.crystals.pos.visualization.payments.bonuscard.view;


import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.payments.BonusCardPaymentEntity;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductInputPanel;
import ru.crystals.pos.visualization.payments.bonuscard.ResBundlePaymentBonusCard;
import ru.crystals.pos.visualization.payments.bonuscard.controller.BonusCardPaymentController;
import ru.crystals.pos.visualization.payments.bonuscard.model.BonusCardPaymentInfo;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentForm;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentInfo;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentHeaderPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentPaidPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentToPayPanel;

import java.awt.event.KeyEvent;

public class BonusCardPaymentEnterCardNumberForm extends
        AbstractPaymentForm<BonusCardPaymentEntity, CommonPaymentHeaderPanel, CommonPaymentPaidPanel, CommonPaymentToPayPanel, CommonProductInputPanel,
                BonusCardPaymentController> {

    public BonusCardPaymentEnterCardNumberForm(XListener outerListener) {
        super(outerListener);
    }

    @Override
    protected boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        if (!getController().isInformixProcessing()) {
            String value = StringUtils.trimToNull(track2);
            if (value != null) {
                getController().getModel().getInfo().setCardNumber(value);
                return false;
            } else {
                return true;
            }
        }
        return true;
    }

    @Override
    protected boolean dispatchBarcodeEvent(String barcode) {
        if (!getController().isInformixProcessing()) {
            String value = StringUtils.trimToNull(barcode);
            if (value != null) {
                getController().getModel().getInfo().setCardNumber(value);
                return false;
            } else {
                return true;
            }
        }
        return true;
    }

    @Override
    public CommonPaymentHeaderPanel createHeaderPanel() {
        return new CommonPaymentHeaderPanel(ResBundlePaymentBonusCard.getString("BONUS_PAYMENT"), ResBundlePaymentBonusCard.getString("REFUND_BONUS_PAYMENT"));
    }

    @Override
    public CommonPaymentPaidPanel createLeftPanel() {
        return new CommonPaymentPaidPanel();
    }

    @Override
    public CommonPaymentToPayPanel createRightPanel() {
        return new CommonPaymentToPayPanel();
    }

    @Override
    public CommonProductInputPanel createFooterPanel() {
        return new CommonProductInputPanel(CommonProductInputPanel.InputType.CARD_NUMBER);
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            String value = StringUtils.trimToNull(footerPanel.getValue().toString());
            if (value != null) {
                getController().getModel().getInfo().setCardNumber(value);
            } else {
                return true;
            }
        } else if (!getController().isInformixProcessing()) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE && !footerPanel.isClean()) {
                footerPanel.clear();
                return true;
            } else if (Character.isDigit(e.getKeyChar()) || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                footerPanel.keyPressed(e);
                return true;
            }
        }
        return false;
    }

    @Override
    public void showForm(BonusCardPaymentEntity payment, PaymentInfo info) {
        super.showForm(payment, info);
        String cardNumber = StringUtils.trimToNull(((BonusCardPaymentInfo) info).getCardNumber());
        if (cardNumber != null) {
            footerPanel.setTextValue(cardNumber);
        }
    }
}
