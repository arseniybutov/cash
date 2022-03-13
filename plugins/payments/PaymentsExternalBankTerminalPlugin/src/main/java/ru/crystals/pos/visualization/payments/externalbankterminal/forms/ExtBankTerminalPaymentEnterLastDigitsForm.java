package ru.crystals.pos.visualization.payments.externalbankterminal.forms;

import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductInputPanel;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentForm;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentHeaderPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentPaidPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentToPayPanel;
import ru.crystals.pos.visualization.payments.externalbankterminal.ResBundlePaymentExternalBankTerminal;
import ru.crystals.pos.visualization.payments.externalbankterminal.integration.ExtBankTerminalPaymentController;

import java.awt.event.KeyEvent;


public class ExtBankTerminalPaymentEnterLastDigitsForm extends AbstractPaymentForm<PaymentEntity, CommonPaymentHeaderPanel, CommonPaymentPaidPanel,
        CommonPaymentToPayPanel, CommonProductInputPanel, ExtBankTerminalPaymentController> {
    public ExtBankTerminalPaymentEnterLastDigitsForm(XListener outerListener) {
        super(outerListener);
    }

    @Override
    public CommonPaymentHeaderPanel createHeaderPanel() {
        return new CommonPaymentHeaderPanel(ResBundlePaymentExternalBankTerminal.getString("EXTERNAL_BANK_TERMINAL"));
    }

    @Override
    public CommonProductInputPanel createFooterPanel() {
        return new CommonProductInputPanel(CommonProductInputPanel.InputType.INTEGER, ResBundlePaymentExternalBankTerminal.getString("ENTER_CARD_NUMBER"), "", 4);
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
    protected boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        return false;
    }

    @Override
    protected boolean dispatchBarcodeEvent(String barcode) {
        return false;
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        if ((e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9) || e.getKeyCode() == KeyEvent.VK_BACK_SPACE || Character.isDigit(e.getKeyChar())) {
            footerPanel.keyPressed(e);
            return true;
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (footerPanel.isClean()) {
                getController().processLastDigitsCanceled();
                return true;
            } else {
                footerPanel.reset();
                return true;
            }
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            String digits = footerPanel.getTextValue();

            if (controller.validateDigits(digits)) {
                controller.processLastDigitsEntered(footerPanel.getTextValue());
            } else {
                controller.beepError("Bad card number (4 last digits)");
            }
        }
        return false;
    }
}
