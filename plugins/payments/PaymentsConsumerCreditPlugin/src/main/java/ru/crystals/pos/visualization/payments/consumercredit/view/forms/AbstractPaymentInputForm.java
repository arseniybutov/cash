package ru.crystals.pos.visualization.payments.consumercredit.view.forms;

import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.payments.ConsumerCreditPaymentEntity;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductInputPanel;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentForm;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentHeaderPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentToPayPanel;
import ru.crystals.pos.visualization.payments.consumercredit.ResBundlePaymentConsumerCredit;
import ru.crystals.pos.visualization.payments.consumercredit.controller.ConsumerCreditController;
import ru.crystals.pos.visualization.payments.consumercredit.view.forms.interfaces.FormFilledHandler;
import ru.crystals.pos.visualization.payments.consumercredit.view.forms.panels.CommonOnscreenQwertyPanel;

import java.awt.event.KeyEvent;

/**
 * Created by myaichnikov on 21.11.2014.
 */
public abstract class AbstractPaymentInputForm extends
        AbstractPaymentForm<ConsumerCreditPaymentEntity, CommonPaymentHeaderPanel, CommonOnscreenQwertyPanel, CommonPaymentToPayPanel, CommonProductInputPanel,
                ConsumerCreditController> implements
        FormFilledHandler {
    public AbstractPaymentInputForm(XListener outerListener) {
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
        return new CommonPaymentHeaderPanel(ResBundlePaymentConsumerCredit.getString("CONSUMER_CREDIT_PAYMENT"));
    }

    @Override
    public CommonOnscreenQwertyPanel createLeftPanel() {
        return null;
    }

    @Override
    public CommonPaymentToPayPanel createRightPanel() {
        return null;
    }

    @Override
    public CommonProductInputPanel createFooterPanel() {
        return new CommonProductInputPanel(CommonProductInputPanel.InputType.STRING, ResBundlePaymentConsumerCredit.getString("ENTER_FIO").toLowerCase(), "");
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            String value = StringUtils.trimToNull(footerPanel.getValue().toString());
            if (value != null) {
                proceed();
            }
            return true;
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (footerPanel.getTextValue().isEmpty()) {
                cancel();
            } else {
                footerPanel.keyPressed(e);
            }
            return true;
        } else {
            footerPanel.keyPressed(e);
            return true;
        }
    }

    @Override
    public abstract void proceed();

    @Override
    public abstract void cancel();
}
