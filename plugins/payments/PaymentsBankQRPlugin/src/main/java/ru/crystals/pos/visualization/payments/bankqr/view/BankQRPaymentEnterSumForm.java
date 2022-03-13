package ru.crystals.pos.visualization.payments.bankqr.view;

import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.payments.BankQRPaymentEntity;
import ru.crystals.pos.payments.ChildrenCardPaymentController;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductInputPanel;
import ru.crystals.pos.visualization.payments.bankqr.ResBundlePaymentBankQR;
import ru.crystals.pos.visualization.payments.bankqr.controller.BankQRPaymentController;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentForm;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentInfo;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentHeaderPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentPaidPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentToPayPanel;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;

public class BankQRPaymentEnterSumForm extends AbstractPaymentForm<BankQRPaymentEntity, CommonPaymentHeaderPanel, CommonPaymentPaidPanel, CommonPaymentToPayPanel,
        CommonProductInputPanel, BankQRPaymentController> {

    public BankQRPaymentEnterSumForm(XListener outerListener) {
        super(outerListener);
    }

    @Override
    public CommonPaymentHeaderPanel createHeaderPanel() {
        return new CommonPaymentHeaderPanel(ResBundlePaymentBankQR.getString("QR_PAYMENT"));
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
        if (e.getKeyCode() == KeyEvent.VK_COMMA || e.getKeyCode() == KeyEvent.VK_BACK_SPACE || Character.isDigit(e.getKeyChar()) || e.getKeyChar() == KeyEvent.VK_COMMA) {
            footerPanel.keyPressed(e);
            return true;
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            BigDecimal amountFromPanel = footerPanel.getDoubleValue();
            Long amountFromPanelConverted = BigDecimalConverter.convertMoney(amountFromPanel);
            if ((amountFromPanel.compareTo(BigDecimal.ZERO) > 0) && CurrencyUtil.checkPaymentRatio(amountFromPanelConverted)) {
                if (isProhibitedWithMixedPayment(amountFromPanelConverted) || isProhibitedForChildPurchase(amountFromPanelConverted)) {
                    Factory.getTechProcessImpl().error("Payment sum can not be less than check sum, " +
                            "because mixedPaymentProhibited is true. See cash-config.xml");
                    Factory.getInstance().showMessage(ResBundleVisualization.getString("PAYMENT_SUM_IS_LESS_THAN_CHECK_SUM"));
                } else {
                    getController().processOperation(amountFromPanel);
                }
            } else if (info.getSurcharge().compareTo(BigDecimal.ZERO) == 0 && amountFromPanel.compareTo(BigDecimal.ZERO) == 0) {
                Factory.getInstance().getMainWindow().getCheckContainer().paymentComplete(payment, Factory.getInstance().getMainWindow().getCheckContainer().getChange());
            }
            return true;
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

    private boolean isProhibitedWithMixedPayment(Long amountFromPanelConverted) {
        return Factory.getInstance().getProperties().isMixedPaymentProhibited()
                && amountFromPanelConverted < Factory.getTechProcessImpl().getSurchargeValue();
    }

    private boolean isProhibitedForChildPurchase(Long amountFromPanelConverted) {
        return controller.getAdapter().getPaymentId().equals(ChildrenCardPaymentController.TYPE_NAME)
                && Factory.getInstance().getProperties().isSplitCheckByChildrenCard()
                && amountFromPanelConverted < BigDecimalConverter.convertMoney(info.getSurcharge());
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
    public void showForm(BankQRPaymentEntity payment, PaymentInfo info) {
        headerPanel.setSaleText(getController().getAdapter().getTitlePaymentType());
        headerPanel.setRefundText(getController().getAdapter().getReturnPaymentString());
        super.showForm(payment, info);
        if (getController().isRefund()) {
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
}
