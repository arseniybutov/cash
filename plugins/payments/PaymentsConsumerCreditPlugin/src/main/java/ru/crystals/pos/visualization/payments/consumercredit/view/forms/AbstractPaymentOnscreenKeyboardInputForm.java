package ru.crystals.pos.visualization.payments.consumercredit.view.forms;

import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.payments.ConsumerCreditPaymentEntity;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductInputPanel;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentForm;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentHeaderPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentToPayPanel;
import ru.crystals.pos.visualization.payments.consumercredit.controller.ConsumerCreditController;
import ru.crystals.pos.visualization.payments.consumercredit.view.forms.interfaces.FormFilledHandler;
import ru.crystals.pos.visualization.payments.consumercredit.view.forms.panels.CommonOnscreenQwertyPanel;

import java.awt.event.KeyEvent;

/**
 * Created by myaichnikov on 20.11.2014.
 */
public abstract class AbstractPaymentOnscreenKeyboardInputForm extends AbstractPaymentForm<ConsumerCreditPaymentEntity,
        CommonPaymentHeaderPanel,
        CommonOnscreenQwertyPanel,
        CommonPaymentToPayPanel,
        CommonProductInputPanel,
        ConsumerCreditController> implements FormFilledHandler {
    protected CommonProductInputPanel footer;

    public AbstractPaymentOnscreenKeyboardInputForm(XListener outerListener) {
        super(outerListener);
    }

    protected void addAWTListener() {
        //
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
        return null;
    }

    @Override
    public CommonOnscreenQwertyPanel createLeftPanel() {
        return new CommonOnscreenQwertyPanel(createFooterPanel().getInputField(), this);
    }

    @Override
    public CommonPaymentToPayPanel createRightPanel() {
        return null;
    }

    @Override
    public CommonProductInputPanel createFooterPanel() {
        if (footer == null) {
            footer = new CommonProductInputPanel(CommonProductInputPanel.InputType.STRING);
        }
        return footer;
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (footer.getTextValue().isEmpty()) {
                cancel();
            } else {
                footer.keyPressed(e);
            }
        } else {
            unitPanel.keyPressedNew(e);
            footerPanel.keyPressed(e);

        }
        return true;
    }
}
