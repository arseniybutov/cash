package ru.crystals.pos.visualization.payments.siebelbonuscard.view;

import ru.crystals.pos.cards.cft.CardType;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonMessageForm;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonSpinnerForm;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentView;
import ru.crystals.pos.visualization.payments.common.PaymentModelChangedEvent;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentController;
import ru.crystals.pos.visualization.payments.siebelbonuscard.ResBundlePaymentSiebelBonusCard;
import ru.crystals.pos.visualization.payments.siebelbonuscard.controller.SiebelBonusCardPaymentController;
import ru.crystals.pos.visualization.payments.siebelbonuscard.model.SiebelBonusCardPaymentInfo;
import ru.crystals.pos.visualization.payments.siebelbonuscard.model.SiebelBonusCardPaymentState;

import java.awt.event.KeyEvent;

public class SiebelBonusCardPaymentView extends AbstractPaymentView<SiebelBonusCardPaymentController> {
    private final SiebelBonusCardPaymentEnterCardNumberForm enterCardNumberForm;
    private final SiebelBonusCardPaymentConfirmForm confirmForm;
    private final CommonSpinnerForm spinnerForm;
    private final CommonMessageForm messageForm;

    /**
     * В конструкторе создаем все формы представления и добавляем их на него
     */
    public SiebelBonusCardPaymentView() {
        enterCardNumberForm = new SiebelBonusCardPaymentEnterCardNumberForm(this);
        confirmForm = new SiebelBonusCardPaymentConfirmForm(this);
        spinnerForm = new CommonSpinnerForm(this, ResBundlePaymentSiebelBonusCard.getString("GETTING_SIEBEL_BONUSCARD_INFO"));
        messageForm = new CommonMessageForm(this);

        this.add(enterCardNumberForm, enterCardNumberForm.getClass().getName());
        this.add(confirmForm, confirmForm.getClass().getName());
        this.add(spinnerForm, spinnerForm.getClass().getName());
        this.add(messageForm, messageForm.getClass().getName());
    }

    @Override
    public void setController(PaymentController controller) {
        this.controller = (SiebelBonusCardPaymentController) controller;
        enterCardNumberForm.setController(controller);
        confirmForm.setController(controller);
    }

    @Override
    public void modelChanged(PaymentModelChangedEvent event) {
        lastEvent = event;
        switch ((SiebelBonusCardPaymentState) lastEvent.getState()) {
            case ENTER_CARD_NUMBER:
                setCurrentForm(enterCardNumberForm);
                break;
            case PAYMENT:
                setCurrentForm(confirmForm);
                break;
            case PARSE_INTERNAL_CARD:
                spinnerForm.setTextMessage(ResBundlePaymentSiebelBonusCard.getString("GETTING_SIEBEL_BONUSCARD_INFO"));
                setCurrentForm(spinnerForm);
                break;
            case ERROR:
                messageForm.setMessage(((SiebelBonusCardPaymentInfo) event.getInfo()).getExceptionText());
                setCurrentForm(messageForm);
                break;
            default:
                break;
        }
    }

    public boolean dispatchKeyPressed(XKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (currentForm == confirmForm) {
                setCurrentForm(enterCardNumberForm);
                return !((SiebelBonusCardPaymentController) getController()).isCardApplied();
            } else if (currentForm == messageForm) {
                if (lastEvent.getState() == SiebelBonusCardPaymentState.ERROR) {
                    return false;
                }
                setCurrentForm(enterCardNumberForm);
                return true;
            } else if (currentForm == enterCardNumberForm) {
                return enterCardNumberForm.keyPressedNew(e);
            }
            return false;
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            // Ввели номер карты - вызываем метод проверки карты и получения баланса карты, возможного к списанию
            if (currentForm == enterCardNumberForm) {
                spinnerForm.setTextMessage(ResBundlePaymentSiebelBonusCard.getString("GETTING_SIEBEL_BONUSCARD_INFO"));
                setCurrentForm(spinnerForm);
                try {
                    controller.processGetCardInfo(CardType.PAN, enterCardNumberForm.getEnteredNumber());
                } catch (Exception exception) {
                    messageForm.setMessage(exception.getMessage());
                    setCurrentForm(messageForm);
                }
            } else if (currentForm == confirmForm) {
                try {
                    spinnerForm.setTextMessage(ResBundlePaymentSiebelBonusCard.getString("PROCESSING_SIEBEL_BONUSCARD"));
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
