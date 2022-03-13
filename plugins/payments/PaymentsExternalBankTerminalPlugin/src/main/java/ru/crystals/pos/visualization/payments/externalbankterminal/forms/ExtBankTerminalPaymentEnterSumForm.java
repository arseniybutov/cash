package ru.crystals.pos.visualization.payments.externalbankterminal.forms;

import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductInputPanel;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentForm;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentController;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentInfo;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentHeaderPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentPaidPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentToPayPanel;
import ru.crystals.pos.visualization.payments.externalbankterminal.ResBundlePaymentExternalBankTerminal;
import ru.crystals.pos.visualization.payments.externalbankterminal.integration.ExtBankTerminalPaymentController;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;

public class ExtBankTerminalPaymentEnterSumForm extends AbstractPaymentForm<PaymentEntity, CommonPaymentHeaderPanel, CommonPaymentPaidPanel, CommonPaymentToPayPanel,
        CommonProductInputPanel, ExtBankTerminalPaymentController> {
    public ExtBankTerminalPaymentEnterSumForm(XListener outerListener) {
        super(outerListener);
    }

    @Override
    public CommonPaymentHeaderPanel createHeaderPanel() {
        return new CommonPaymentHeaderPanel(ResBundlePaymentExternalBankTerminal.getString("EXTERNAL_BANK_TERMINAL"));
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
        return new CommonProductInputPanel(CommonProductInputPanel.InputType.SUMM);
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        if (e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9 || e.getKeyCode() == KeyEvent.VK_COMMA
                || e.getKeyChar() == KeyEvent.VK_COMMA
                || e.getKeyCode() == KeyEvent.VK_BACK_SPACE || Character.isDigit(e.getKeyChar())) {
            footerPanel.keyPressed(e);
            return true;
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            /*
             * Проверим, что введеное число не больше суммы на карте или суммы к оплате
             * Проверим на ноль
             */
            BigDecimal amountFromPanel = footerPanel.getDoubleValue();
            getController().processSumToPayEntered(amountFromPanel);
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (footerPanel.isClean()) {
                return false;
            } else {
                footerPanel.clear();
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean dispatchBarcodeEvent(String barcode) {
        return false;
    }

    @Override
    protected boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        return false;
    }

    public BigDecimal getSumToPay() {
        return footerPanel.getDoubleValue();
    }

    @Override
    public void showForm(PaymentEntity payment, PaymentInfo info) {
        super.showForm(payment, info);
        if (controller.isRefund()) {
            BigDecimal value = (payment.getSumPay() != null && BigDecimalConverter.convertMoney(payment.getSumPay()).compareTo(info.getSurcharge()) < 0) ?
                    BigDecimalConverter.convertMoney(payment.getSumPay()) :
                    info.getSurcharge();
            footerPanel.setMaximumValue(value);
            footerPanel.setDoubleValue(value);
        } else {
            footerPanel.setMaximumValue(info.getSurcharge());
            footerPanel.setDoubleValue(info.getSurcharge());
        }
    }

    public void setController(PaymentController controller) {
        this.controller = (ExtBankTerminalPaymentController) controller;
    }

    public ExtBankTerminalPaymentController getController() {
        return controller;
    }
}
