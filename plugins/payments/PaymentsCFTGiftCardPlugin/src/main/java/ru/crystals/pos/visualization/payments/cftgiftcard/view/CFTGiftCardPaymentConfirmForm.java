package ru.crystals.pos.visualization.payments.cftgiftcard.view;

import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductInputPanel;
import ru.crystals.pos.visualization.payments.cftgiftcard.ResBundlePaymentCftGiftCard;
import ru.crystals.pos.visualization.payments.cftgiftcard.controller.CFTGiftCardPaymentController;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentForm;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentInfo;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentHeaderPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentPaidPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentToPayPanel;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;

public class CFTGiftCardPaymentConfirmForm extends AbstractPaymentForm<PaymentEntity, CommonPaymentHeaderPanel, CommonPaymentPaidPanel, CommonPaymentToPayPanel,
        CommonProductInputPanel, CFTGiftCardPaymentController> {
    public CFTGiftCardPaymentConfirmForm(XListener outerListener) {
        super(outerListener);
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
        return new CommonPaymentHeaderPanel(ResBundlePaymentCftGiftCard.getString("CFT_GIFTCARD_PAYMENT"), ResBundlePaymentCftGiftCard.getString("CFT_GIFTCARD_REFUND"));
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
        if (e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9 || e.getKeyCode() == KeyEvent.VK_COMMA || e.getKeyCode() == KeyEvent.VK_BACK_SPACE || Character.isDigit(e.getKeyChar())) {
            footerPanel.keyPressed(e);
            return true;
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            /*
             * Проверим, что введеное число не больше суммы на карте или суммы к оплате
             * Проверим на ноль
             */
            BigDecimal amountFromPanel = footerPanel.getDoubleValue();
            if (amountFromPanel.compareTo(payment.getSumPayBigDecimal()) > 0 ||
                    amountFromPanel.compareTo(info.getSurcharge()) > 0 ||
                    amountFromPanel.compareTo(BigDecimal.ZERO) <= 0) {
                e.setSource("CFTPaymentsPlugin: empty or zero summ field, cannot process card");
                e.setKeyCode(KeyEvent.VK_KANA);
            }
            return false;
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
    public void showForm(PaymentEntity payment, PaymentInfo info) {
        super.showForm(payment, info);
        headerPanel.setHeaderInfo(payment);
        summPanel.setPaymentSumm(info.getSurcharge());
        unitPanel.setPaid(info);
        if (info.getSurcharge().compareTo(payment.getSumPayBigDecimal()) > 0) {
            footerPanel.setMaximumValue(payment.getSumPayBigDecimal());
            footerPanel.setDoubleValue(payment.getSumPayBigDecimal());
        } else {
            footerPanel.setMaximumValue(info.getSurcharge());
            footerPanel.setDoubleValue(info.getSurcharge());
        }
    }


    public BigDecimal getSumToPay() {
        return footerPanel.getDoubleValue();
    }
}
