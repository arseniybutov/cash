package ru.crystals.pos.visualization.payments.supra.view;

import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductInputPanel;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentInfo;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentHeaderPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentPaidPanel;
import ru.crystals.pos.visualization.payments.common.panels.CommonPaymentToPayPanel;
import ru.crystals.pos.visualization.payments.supra.ResBundlePaymentSupraCard;
import ru.crystals.pos.visualization.payments.supra.model.SupraCardPaymentInfo;

import java.awt.event.KeyEvent;

public class SupraCardPaymentEnterCardNumberForm extends SupraCardPaymentBaseForm {
    public SupraCardPaymentEnterCardNumberForm(XListener outerListener) {
        super(outerListener);
    }

    @Override
    public CommonPaymentHeaderPanel createHeaderPanel() {
        return new CommonPaymentHeaderPanel(ResBundlePaymentSupraCard.getString("SUPRA_CARD_PAYMENT"),
                ResBundlePaymentSupraCard.getString("SUPRA_CARD_PAYMENT"));
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
        return new CommonProductInputPanel(CommonProductInputPanel.InputType.CARD_NUMBER);
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            String value = StringUtils.trimToNull(footerPanel.getValue().toString());
            if (value != null) {
                getController().getModel().getInfo().setCardNumber(value);
            } else {
                return true;
            }
        } else if ((e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9) || (e.getKeyCode() == KeyEvent.VK_BACK_SPACE)) {

            if (isUserInputAllowed()) {
                //Если включена опция "Пользовательский ввод"
                footerPanel.keyPressed(e);
            } else {
                controller.beepError("Manual input is not allowed");
            }
            return true;
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE && !footerPanel.isClean()) {
            footerPanel.clear();
            return true;
        } else if (Character.isDigit(e.getKeyChar()) || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            footerPanel.keyPressed(e);
            return true;
        }
        return false;
    }

    @Override
    protected boolean dispatchBarcodeEvent(String barcode) {
        getController().getModel().getInfo().setCardNumber(barcode);
        footerPanel.setTextValue(barcode);
        return false;
    }

    @Override
    protected boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        getController().getModel().getInfo().setCardNumber(track2);
        footerPanel.setTextValue(track2);
        return false;
    }

    public boolean isUserInputAllowed() {
        return (getController().getModel().getPayment() != null) && getController().getModel().getPayment().getPaymentSettings().isAllowUserInput();
    }

    @Override
    public void showForm(PaymentEntity payment, PaymentInfo info) {
        super.showForm(payment, info);
        String cardNumber = StringUtils.trimToNull(((SupraCardPaymentInfo) info).getCardNumber());
        if (StringUtils.isNotEmpty(cardNumber)) {
            footerPanel.setTextValue(cardNumber);
        } else {
            footerPanel.clear();
        }
    }
}
