package ru.crystals.pos.visualization.payments.supra.view;

import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductInputPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentHeaderPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentPaidPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentToPayPanel;
import ru.crystals.pos.visualization.payments.supra.ResBundlePaymentSupraCard;

import java.awt.event.KeyEvent;

public class SupraCardPaymentEnterLastDigitsForm extends SupraCardPaymentBaseForm {

    public static final int NUMBER_DIGITS = 4;

    public SupraCardPaymentEnterLastDigitsForm(XListener outerListener) {
        super(outerListener);
    }

    @Override
    public CommonPaymentHeaderPanel createHeaderPanel() {
        return new CommonPaymentHeaderPanel(ResBundlePaymentSupraCard.getString("SUPRA_CARD_PAYMENT"));
    }

    @Override
    public CommonProductInputPanel createFooterPanel() {
        return new CommonProductInputPanel(CommonProductInputPanel.InputType.INTEGER, ResBundlePaymentSupraCard.getString("ENTER_LAST_FOUR"), "", 4);
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
                return false;
            } else {
                footerPanel.reset();
                return true;
            }
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            String digits = footerPanel.getTextValue();
            if (StringUtils.isEmpty(digits) || digits.length() != NUMBER_DIGITS) {
                controller.beepError("You should input 4 digits");
                return true;
            }
            getController().getModel().getInfo().setLastFour(digits);
        }
        return false;
    }

    public String getLastDigits() {
        return footerPanel.getTextValue();
    }
}
