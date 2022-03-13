package ru.crystals.pos.visualization.payments.consumercredit.view.forms;

import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.payments.ConsumerCreditPaymentEntity;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonYesNoPanel;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentForm;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentToPayPanel;
import ru.crystals.pos.visualization.payments.consumercredit.ResBundlePaymentConsumerCredit;
import ru.crystals.pos.visualization.payments.consumercredit.controller.ConsumerCreditController;
import ru.crystals.pos.visualization.payments.consumercredit.view.forms.interfaces.FormFilledHandler;
import ru.crystals.pos.visualization.payments.consumercredit.view.forms.panels.CommonSelectUnitPanel;
import ru.crystals.pos.visualization.payments.consumercredit.view.forms.panels.ConsumerCreditConfirmPanel;

import java.awt.event.KeyEvent;

/**
 * Created by myaichnikov on 21.11.2014.
 */
public class ConsumerCreditConfirmForm extends
        AbstractPaymentForm<ConsumerCreditPaymentEntity, ConsumerCreditConfirmPanel, CommonSelectUnitPanel, CommonPaymentToPayPanel, CommonYesNoPanel,
                ConsumerCreditController> implements
        FormFilledHandler {
    public ConsumerCreditConfirmForm(XListener outerListener) {
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
    public ConsumerCreditConfirmPanel createHeaderPanel() {
        return new ConsumerCreditConfirmPanel();
    }

    @Override
    public CommonSelectUnitPanel createLeftPanel() {
        return null;
    }

    @Override
    public CommonPaymentToPayPanel createRightPanel() {
        return null;
    }

    @Override
    public CommonYesNoPanel createFooterPanel() {
        return new CommonYesNoPanel(ResBundlePaymentConsumerCredit.getString("CANCEL"), ResBundlePaymentConsumerCredit.getString("CONFIRM"));
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (footerPanel.isYes()) {
                cancel();
            } else {
                proceed();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            cancel();
        }
        return true;
    }

    @Override
    public void proceed() {
        getController().processConfirmPayment();
    }

    @Override
    public void cancel() {
        getController().processCancelConfirmPayment();
    }

    public void setFIO(String fio) {
        headerPanel.setFioLabel(fio);
    }

    public void setBank(String bank) {
        headerPanel.setBankLabel(bank);
    }

    public void setBankProduct(String product) {
        headerPanel.setBankProductLabel(product);
    }

    public void setContractNumber(String contractNumber) {
        headerPanel.setContractNumLabel(contractNumber);
    }

    public void setCreditAmount(String creditAmount) {
        headerPanel.setCreditAmountLabel(creditAmount);
    }
}
