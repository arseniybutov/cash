package ru.crystals.pos.visualization.payments.externalbankterminal.forms;

import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductInputPanel;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentForm;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentController;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentHeaderPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentToPayPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentUnitPanel;
import ru.crystals.pos.visualization.payments.externalbankterminal.ResBundlePaymentExternalBankTerminal;
import ru.crystals.pos.visualization.payments.externalbankterminal.integration.ExtBankTerminalPaymentController;

import java.awt.event.KeyEvent;

public class ExtBankTerminalPaymentEnterAuthCodeForm extends AbstractPaymentForm<PaymentEntity, CommonPaymentHeaderPanel, ExtBankPayment2UnitPanel,
        CommonPaymentToPayPanel, CommonProductInputPanel, ExtBankTerminalPaymentController> {
    public ExtBankTerminalPaymentEnterAuthCodeForm(XListener outerListener) {
        super(outerListener);
    }

    @Override
    public CommonPaymentHeaderPanel createHeaderPanel() {
        return new CommonPaymentHeaderPanel(ResBundlePaymentExternalBankTerminal.getString("EXTERNAL_BANK_TERMINAL"),
                ResBundlePaymentExternalBankTerminal.getString("EXTERNAL_BANK_TERMINAL"));
    }

    @Override
    public CommonProductInputPanel createFooterPanel() {
        return new CommonProductInputPanel(CommonProductInputPanel.InputType.INTEGER, ResBundlePaymentExternalBankTerminal.getString("ENTER_AUTHORIZATION_CODE"), "", 10);
    }

    @Override
    public ExtBankPayment2UnitPanel createLeftPanel() {
        return new ExtBankPayment2UnitPanel(new CommonPaymentUnitPanel(ResBundlePaymentExternalBankTerminal.getString("CARD_NUMBER"), ""), null);
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
                getController().processAuthCodeCanceled();
                return true;
            } else {
                footerPanel.reset();
                return true;
            }
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            controller.processAuthCodeEntered(footerPanel.getTextValue());
        }
        return false;
    }

    public void setController(PaymentController controller) {
        this.controller = (ExtBankTerminalPaymentController) controller;
    }

    public ExtBankTerminalPaymentController getController() {
        return controller;
    }
}
