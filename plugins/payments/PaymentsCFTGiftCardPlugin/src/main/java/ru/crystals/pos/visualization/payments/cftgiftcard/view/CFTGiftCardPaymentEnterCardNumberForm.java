package ru.crystals.pos.visualization.payments.cftgiftcard.view;

import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductInputPanel;
import ru.crystals.pos.visualization.payments.cftgiftcard.ResBundlePaymentCftGiftCard;
import ru.crystals.pos.visualization.payments.cftgiftcard.controller.CFTGiftCardPaymentController;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentForm;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentHeaderPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentPaidPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentToPayPanel;

import java.awt.event.KeyEvent;

public class CFTGiftCardPaymentEnterCardNumberForm extends AbstractPaymentForm<PaymentEntity, CommonPaymentHeaderPanel, CommonPaymentPaidPanel, CommonPaymentToPayPanel
        , CommonProductInputPanel, CFTGiftCardPaymentController> {
    public CFTGiftCardPaymentEnterCardNumberForm(XListener outerListener) {
        super(outerListener);
    }

    @Override
    public CommonPaymentHeaderPanel createHeaderPanel() {
        return new CommonPaymentHeaderPanel(ResBundlePaymentCftGiftCard.getString("CFT_GIFTCARD_PAYMENT"),
                ResBundlePaymentCftGiftCard.getString("CFT_GIFTCARD_REFUND"));
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
        /*
         * Enter и Escape могут пробрасываться наружу (если вернуть false)
         * остальные нажатия обрабатываются внутри формы
         */
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (footerPanel.getTextValue() == null || footerPanel.getTextValue().isEmpty()) {
                if (controller != null) {
                    controller.beepError("CFTPaymentsPlugin: empty card number field, cannot process info");
                }
                return true;
            }
            return false;
        } else if ((e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9) || (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) || Character.isDigit(e.getKeyChar())) {
            if (allowUserInput) {
                footerPanel.keyPressed(e);
            } else {
                // случай, когда ввод с клавиатуры запрещен - нужно вызвать ошибку техпроцесса и попищать
                if (controller != null) {
                    controller.beepError("CFTPaymentsPlugin: empty card number field, cannot process info");
                }
                return true;
            }

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
    protected boolean dispatchBarcodeEvent(String barcode) {
        return false;
    }

    @Override
    protected boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        return false;
    }

    public String getEnteredNumber() {
        return footerPanel.getTextValue();
    }
}
