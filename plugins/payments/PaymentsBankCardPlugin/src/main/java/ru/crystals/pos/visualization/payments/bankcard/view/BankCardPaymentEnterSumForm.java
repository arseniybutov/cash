package ru.crystals.pos.visualization.payments.bankcard.view;

import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.payments.BankCardPaymentEntity;
import ru.crystals.pos.payments.ChildrenCardPaymentController;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductInputPanel;
import ru.crystals.pos.visualization.notification.NotificationType;
import ru.crystals.pos.visualization.payments.bankcard.ResBundlePaymentBankCard;
import ru.crystals.pos.visualization.payments.bankcard.controller.BankCardPaymentController;
import ru.crystals.pos.visualization.payments.bankcard.model.BankCardPaymentInfo;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentForm;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentInfo;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentHeaderPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentPaidPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentToPayPanel;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;

/**
 * Created by agaydenger on 18.11.16.
 */
public class BankCardPaymentEnterSumForm extends AbstractPaymentForm<BankCardPaymentEntity, CommonPaymentHeaderPanel, CommonPaymentPaidPanel, CommonPaymentToPayPanel,
        CommonProductInputPanel, BankCardPaymentController> {

    public BankCardPaymentEnterSumForm(XListener outerListener) {
        super(outerListener);
    }

    @Override
    public CommonPaymentHeaderPanel createHeaderPanel() {
        return new CommonPaymentHeaderPanel(ResBundlePaymentBankCard.getString("PAYMENT_BY_CARD"));
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
            return processEnterKey();
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            return processEscKey();
        }
        Factory.getInstance().hideNotification(NotificationType.RETURN_CASH_OUT);
        return false;
    }

    private boolean processEnterKey() {
        Factory.getInstance().hideNotification(NotificationType.RETURN_CASH_OUT);
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
    }

    private boolean processEscKey() {
        Factory.getInstance().hideNotification(NotificationType.RETURN_CASH_OUT);
        if (footerPanel.isClean()) {
            return false;
        } else {
            footerPanel.clear();
            return true;
        }
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
    public void showForm(BankCardPaymentEntity payment, PaymentInfo info) {
        headerPanel.setSaleText(getController().getAdapter().getTitlePaymentType());
        headerPanel.setRefundText(getController().getAdapter().getReturnPaymentString());
        super.showForm(payment, info);
        if (getController().isRefund()) {
            BigDecimal value = calcReturnSum(payment, info);

            footerPanel.setMaximumValue(value);
            footerPanel.setDoubleValue(value);
            summPanel.setPaymentSumm(value);
        } else {
            footerPanel.setMaximumValue(info.getSurcharge());
            footerPanel.setDoubleValue(info.getSurcharge());
            summPanel.setPaymentSumm(info.getSurcharge());
        }

    }

    private BigDecimal calcReturnSum(BankCardPaymentEntity payment, PaymentInfo info) {
        BigDecimal value = (payment.getSumPay() != null && BigDecimalConverter.convertMoney(payment.getSumPay()).compareTo(info.getSurcharge()) < 0) ?
                BigDecimalConverter.convertMoney(payment.getSumPay()) : info.getSurcharge();

        Long returnCashOutAmount = ((BankCardPaymentInfo) info).getReturnCashOutAmount() != null ? ((BankCardPaymentInfo) info).getReturnCashOutAmount() : 0L;
        if (returnCashOutAmount > 0) {
            value = value.add(BigDecimalConverter.convertMoney(returnCashOutAmount));
            Factory.getInstance().showNotification(NotificationType.RETURN_CASH_OUT,
                    String.format(ResBundlePaymentBankCard.getString("AMOUNT_RETURN_CASHOUT"), BigDecimalConverter.convertMoneyToText(returnCashOutAmount)));
        }

        return value;
    }
}
