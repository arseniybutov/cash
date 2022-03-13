package ru.crystals.pos.visualization.payments.cftgiftcard.view;

import ru.crystals.pos.cards.cft.CardType;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonMessageForm;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonSpinnerForm;
import ru.crystals.pos.visualization.payments.cftgiftcard.ResBundlePaymentCftGiftCard;
import ru.crystals.pos.visualization.payments.cftgiftcard.controller.CFTGiftCardPaymentController;
import ru.crystals.pos.visualization.payments.cftgiftcard.model.CFTGiftCardPaymentState;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentView;
import ru.crystals.pos.visualization.payments.common.PaymentModelChangedEvent;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentController;

import java.awt.event.KeyEvent;

public class CFTGiftCardPaymentView extends AbstractPaymentView<CFTGiftCardPaymentController> {
    private final CFTGiftCardPaymentEnterCardNumberForm enterCardNumberForm;
    private final CFTGiftCardPaymentConfirmForm confirmForm;
    private final CommonSpinnerForm spinnerForm;
    private final CommonMessageForm messageForm;

    /**
     * В конструкторе создаем все формы представления и добавляем их на него
     */
    public CFTGiftCardPaymentView() {
        enterCardNumberForm = new CFTGiftCardPaymentEnterCardNumberForm(this);
        confirmForm = new CFTGiftCardPaymentConfirmForm(this);
        spinnerForm = new CommonSpinnerForm(this, ResBundlePaymentCftGiftCard.getString("GETTING_CFT_CARD_INFO"));
        messageForm = new CommonMessageForm(this);

        this.add(enterCardNumberForm, enterCardNumberForm.getClass().getName());
        this.add(confirmForm, confirmForm.getClass().getName());
        this.add(spinnerForm, spinnerForm.getClass().getName());
        this.add(messageForm, messageForm.getClass().getName());
    }

    @Override
    public void setController(PaymentController controller) {
        this.controller = (CFTGiftCardPaymentController) controller;
        enterCardNumberForm.setController(controller);
        confirmForm.setController(controller);
    }

    @Override
    public void modelChanged(PaymentModelChangedEvent event) {
        lastEvent = event;
        switch ((CFTGiftCardPaymentState) lastEvent.getState()) {
            case REFUND:
            case PAYMENT:
                setCurrentForm(confirmForm);
                break;
            default:
                setCurrentForm(enterCardNumberForm);
                break;
        }
    }

    public boolean dispatchKeyPressed(XKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (currentForm == confirmForm || currentForm == messageForm) {
                setCurrentForm(enterCardNumberForm);
                return true;
            } else if (currentForm == enterCardNumberForm) {
                return enterCardNumberForm.keyPressedNew(e);
            }
            return false;
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            // Ввели номер карты - вызываем метод проверки карты и получения баланса карты, возможного к списанию
            if (currentForm == enterCardNumberForm) {
                spinnerForm.setTextMessage(ResBundlePaymentCftGiftCard.getString("GETTING_CFT_CARD_INFO"));
                setCurrentForm(spinnerForm);
                try {
                    controller.processGetCardInfo(CardType.PAN, enterCardNumberForm.getEnteredNumber());
                } catch (Exception exception) {
                    messageForm.setMessage(exception.getMessage());
                    setCurrentForm(messageForm);
                }
            } else if (currentForm == confirmForm) {
                try {
                    spinnerForm.setTextMessage(ResBundlePaymentCftGiftCard.getString("PROCESSING_CFT"));
                    setCurrentForm(spinnerForm);
                    controller.processPayWithGiftCard(confirmForm.getSumToPay());
                } catch (Exception exception) {
                    messageForm.setMessage(exception.getMessage());
                    setCurrentForm(messageForm);
                }
                return true;
            }
        } else if (e.getKeyCode() == KeyEvent.VK_KANA) {
            getController().beepError(e.getSource().toString());
        }
        return true;
    }

    @Override
    public boolean barcodeScanned(String barcode) {
        if (currentForm == messageForm || currentForm == spinnerForm) {
            return true;
        }
        setCurrentForm(spinnerForm);
        try {
            controller.processGetCardInfo(CardType.BARCODE, barcode);
        } catch (Exception exception) {
            messageForm.setMessage(exception.getMessage());
            setCurrentForm(messageForm);
        }
        return true;
    }

    @Override
    public boolean eventMSR(String track1, String track2, String track3, String track4) {
        if (currentForm == messageForm || currentForm == spinnerForm) {
            return true;
        }
        setCurrentForm(spinnerForm);
        try {
            controller.processGetCardInfo(CardType.TRACK2, track1, track2, track3, track4);
        } catch (Exception exception) {
            messageForm.setMessage(exception.getMessage());
            setCurrentForm(messageForm);
        }

        return true;
    }
}
