package ru.crystals.pos.visualization.payments.consumercredit.view.forms;

import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.payments.ConsumerCreditPaymentEntity;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductInputPanel;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentForm;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentInfo;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentHeaderPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentPaidPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentToPayPanel;
import ru.crystals.pos.visualization.payments.consumercredit.ResBundlePaymentConsumerCredit;
import ru.crystals.pos.visualization.payments.consumercredit.controller.ConsumerCreditController;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;

/**
 * Created by myaichnikov on 19.11.2014.
 */
public class ConsumerCreditPaymentEnterSum extends AbstractPaymentForm<ConsumerCreditPaymentEntity,
        CommonPaymentHeaderPanel,
        CommonPaymentPaidPanel,
        CommonPaymentToPayPanel,
        CommonProductInputPanel,
        ConsumerCreditController> {

    public ConsumerCreditPaymentEnterSum(XListener outerListener) {
        super(outerListener);
    }

    @Override
    protected boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        return false;
    }

    @Override
    protected boolean dispatchBarcodeEvent(String barcode) {
        getController().processDataFromBarcode(barcode);
        return true;
    }

    @Override
    public CommonPaymentHeaderPanel createHeaderPanel() {
        return new CommonPaymentHeaderPanel(ResBundlePaymentConsumerCredit.getString("CONSUMER_CREDIT_PAYMENT"));
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
            getController().processEnterSum(footerPanel.getDoubleValue());
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
    public void showForm(ConsumerCreditPaymentEntity payment, PaymentInfo info) {
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
}
