package ru.crystals.pos.visualization.payments.consumercredit.view.forms;

import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.payments.ConsumerCreditPaymentEntity;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductInputPanel;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentForm;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentHeaderPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentToPayPanel;
import ru.crystals.pos.visualization.payments.consumercredit.ResBundlePaymentConsumerCredit;
import ru.crystals.pos.visualization.payments.consumercredit.controller.ConsumerCreditController;
import ru.crystals.pos.visualization.payments.consumercredit.view.forms.panels.CommonSelectUnitPanel;

import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Created by myaichnikov on 19.11.2014.
 */
public class ConsumerCreditPaymentChooseBankForm extends AbstractPaymentForm<
        ConsumerCreditPaymentEntity,
        CommonPaymentHeaderPanel, CommonSelectUnitPanel,
        CommonPaymentToPayPanel,
        CommonProductInputPanel,
        ConsumerCreditController> {

    public ConsumerCreditPaymentChooseBankForm(XListener outerListener) {
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
        return new CommonPaymentHeaderPanel(ResBundlePaymentConsumerCredit.getString("CHOSE_THE_BANK"));
    }

    @Override
    public CommonSelectUnitPanel createLeftPanel() {
        return new CommonSelectUnitPanel();
    }

    @Override
    public CommonPaymentToPayPanel createRightPanel() {
        return null;
    }

    @Override
    public CommonProductInputPanel createFooterPanel() {
        return null;
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        if (e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9 || e.getKeyCode() == KeyEvent.VK_COMMA
                || e.getKeyChar() == KeyEvent.VK_COMMA
                || e.getKeyCode() == KeyEvent.VK_BACK_SPACE || Character.isDigit(e.getKeyChar())) {
            return true;
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            getController().processChooseBank(unitPanel.getSelectedValue());
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            getController().processCancelChooseBank();
            return true;
        } else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
            unitPanel.keyPressed(e);
            return true;
        }
        return false;
    }

    public void setBanks(List<String> values) {
        unitPanel.setValues(values);
    }
}
