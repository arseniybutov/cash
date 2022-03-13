package ru.crystals.pos.visualization.payments.cftegc.view;

import ru.crystals.pos.cards.cft.CardType;
import ru.crystals.pos.cards.cft.exception.CFTException;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonMessageForm;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonSpinnerForm;
import ru.crystals.pos.visualization.payments.cftegc.ResBundlePaymentCFTEGC;
import ru.crystals.pos.visualization.payments.cftegc.controller.CFTEGCPaymentController;
import ru.crystals.pos.visualization.payments.cftegc.model.CFTEGCPaymentState;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentView;
import ru.crystals.pos.visualization.payments.common.PaymentModelChangedEvent;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentController;

import java.awt.event.KeyEvent;

public class CFTEGCPaymentView extends AbstractPaymentView<CFTEGCPaymentController> {
    private static final long serialVersionUID = -8125074201359643006L;
    private CFTEGCPaymentEnterCardNumberForm enterCardNumberForm = null;
    private CFTEGCPaymentConfirmForm confirmForm = null;
    private CommonSpinnerForm spinnerForm = null;
    private CommonMessageForm messageForm = null;
    private CFTEGCPaymentEnterPinCodeForm enterPinCodeForm = null;

    /**
     * В конструкторе создаем все формы представления и добавляем их на него
     */
    public CFTEGCPaymentView() {
        enterCardNumberForm = new CFTEGCPaymentEnterCardNumberForm(this);
        confirmForm = new CFTEGCPaymentConfirmForm(this);
        spinnerForm = new CommonSpinnerForm(this, ResBundlePaymentCFTEGC.getString("GETTING_CFT_CARD_INFO"));
        messageForm = new CommonMessageForm(this);
        enterPinCodeForm = new CFTEGCPaymentEnterPinCodeForm(this);

        this.add(enterCardNumberForm, enterCardNumberForm.getClass().getName());
        this.add(confirmForm, confirmForm.getClass().getName());
        this.add(spinnerForm, spinnerForm.getClass().getName());
        this.add(messageForm, messageForm.getClass().getName());
        this.add(enterPinCodeForm, enterPinCodeForm.getClass().getName());
    }

    @Override
    public void setController(PaymentController controller) {
        this.controller = (CFTEGCPaymentController) controller;
        enterCardNumberForm.setController(controller);
        confirmForm.setController(controller);
    }

    @Override
    public void modelChanged(PaymentModelChangedEvent event) {
        lastEvent = event;
        switch ((CFTEGCPaymentState) lastEvent.getState()) {
            case ENTER_CARD_NUMBER:
                setCurrentForm(enterCardNumberForm);
                break;
            case ENTER_PIN_CODE:
                setCurrentForm(enterPinCodeForm);
                break;
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
            if (currentForm == confirmForm || currentForm == enterPinCodeForm) {
                setCurrentForm(enterCardNumberForm);
                return true;
            } else if (currentForm == messageForm) {
                if (lastEvent.getState() instanceof CFTEGCPaymentState && ((CFTEGCPaymentState) lastEvent.getState()) == CFTEGCPaymentState.ENTER_PIN_CODE) {
                    setCurrentForm(enterPinCodeForm);
                } else {
                    setCurrentForm(enterCardNumberForm);
                }
                return true;
            } else if (currentForm == enterCardNumberForm) {
                return enterCardNumberForm.keyPressedNew(e);
            }
            return false;
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (currentForm == enterCardNumberForm) {
                processCardNumber(CardType.PAN, enterCardNumberForm.getEnteredNumber());
            } else if (currentForm == confirmForm) {
                try {
                    spinnerForm.setTextMessage(ResBundlePaymentCFTEGC.getString("PROCESSING_CFT"));
                    setCurrentForm(spinnerForm);
                    controller.processPayWithGiftCard(confirmForm.getSumToPay());
                } catch (Exception exception) {
                    messageForm.setMessage(exception.getMessage());
                    setCurrentForm(messageForm);
                }
                return true;
            } else if (currentForm == enterPinCodeForm) {
                processPinCode(enterPinCodeForm.getEnteredNumber());
            }
        } else if (e.getKeyCode() == KeyEvent.VK_KANA) {
            getController().beepError(e.getSource().toString());
        }
        return true;
    }

    @Override
    public boolean barcodeScanned(String barcode) {
        if (currentForm == messageForm || currentForm == spinnerForm || currentForm == enterPinCodeForm) {
            return true;
        }
        processCardNumber(CardType.BARCODE, barcode);
        return true;
    }

    @Override
    public boolean eventMSR(String track1, String track2, String track3, String track4) {
        return true;
    }

    private void processCardNumber(CardType cardType, String cardNumber) {
        try {
            controller.applyCardNumber(cardType, cardNumber);
        } catch (CFTException exception) {
            messageForm.setMessage(ResBundlePaymentCFTEGC.getString("BAD_EGC_NUMBER"));
            setCurrentForm(messageForm);
        }
    }

    private void processPinCode(String pincode) {
        try {
            controller.applyPinCode(pincode);
            processCardInfo();
        } catch (Exception e) {
            messageForm.setMessage(e.getMessage());
            setCurrentForm(messageForm);
        }
    }

    private void processCardInfo() {
        spinnerForm.setTextMessage(ResBundlePaymentCFTEGC.getString("GETTING_CFT_CARD_INFO"));
        setCurrentForm(spinnerForm);
        try {
            controller.processGetCardInfo();
        } catch (Exception exception) {
            messageForm.setMessage(exception.getMessage());
            setCurrentForm(messageForm);
        }
    }
}
