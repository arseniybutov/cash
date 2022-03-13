package ru.crystals.pos.visualization.payments.siebelgiftcard.view;

import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductInputPanel;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentForm;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentHeaderPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentPaidPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentToPayPanel;
import ru.crystals.pos.visualization.payments.siebelgiftcard.controller.SiebelGiftCardPaymentController;

import java.awt.event.KeyEvent;

public final class SiebelGiftCardPaymentEnterVerificationCodeForm extends AbstractPaymentForm<
        PaymentEntity,
        CommonPaymentHeaderPanel,
        CommonPaymentPaidPanel,
        CommonPaymentToPayPanel,
        CommonProductInputPanel,
        SiebelGiftCardPaymentController> {
    public SiebelGiftCardPaymentEnterVerificationCodeForm(XListener outerListener) {
        super(outerListener);
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (footerPanel.getTextValue() == null || footerPanel.getTextValue().isEmpty()) {
                if (controller != null) {
                    controller.beepError("SiebelPaymentsPlugin: empty verification code field");
                }
                return true;
            }
            return false;
        } else if ((e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9) || (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) ||
                Character.isDigit(e.getKeyChar())) {
            footerPanel.keyPressed(e);
            return true;
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (footerPanel.isClean()) {
                return false;
            } else {
                footerPanel.reset();
                return true;
            }
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
    public CommonPaymentHeaderPanel createHeaderPanel() {
        return null;
    }

    @Override
    public CommonPaymentPaidPanel createLeftPanel() {
        return null;
    }

    @Override
    public CommonPaymentToPayPanel createRightPanel() {
        return null;
    }

    @Override
    public CommonProductInputPanel createFooterPanel() {
        return new CommonProductInputPanel(
                CommonProductInputPanel.InputType.CARD_NUMBER,
                ResBundleVisualization.getString("INPUT_VERIFICATION_CODE"),
                ResBundleVisualization.getString("VERIFICATION_CODE"),
                8
        );
    }

    public String getEnteredNumber() {
        return footerPanel.getTextValue();
    }

}
