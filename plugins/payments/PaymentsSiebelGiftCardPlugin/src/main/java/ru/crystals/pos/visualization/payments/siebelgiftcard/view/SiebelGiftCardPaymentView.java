package ru.crystals.pos.visualization.payments.siebelgiftcard.view;

import ru.crystals.loyal.providers.LoyProcessingTryItAgainException;
import ru.crystals.pos.cards.exception.CardsException;
import ru.crystals.pos.cards.siebel.SiebelGiftCardResult;
import ru.crystals.pos.cards.siebel.exception.SiebelServiceException;
import ru.crystals.pos.check.InsertType;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonMessageForm;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonSpinnerForm;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentView;
import ru.crystals.pos.visualization.payments.common.PaymentModelChangedEvent;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentController;
import ru.crystals.pos.visualization.payments.siebelgiftcard.ResBundlePaymentSiebelGiftCard;
import ru.crystals.pos.visualization.payments.siebelgiftcard.controller.SiebelGiftCardPaymentController;
import ru.crystals.pos.visualization.payments.siebelgiftcard.model.SiebelGiftCardPaymentInfo;
import ru.crystals.pos.visualization.payments.siebelgiftcard.model.SiebelGiftCardPaymentState;

import java.awt.event.KeyEvent;

public class SiebelGiftCardPaymentView extends AbstractPaymentView<SiebelGiftCardPaymentController> {
    private final SiebelGiftCardPaymentEnterCardNumberForm enterCardNumberForm;
    private final SiebelGiftCardPaymentEnterVerificationCodeForm enterVerificationCodeForm;
    private final SiebelGiftCardPaymentConfirmForm confirmForm;
    private final CommonSpinnerForm spinnerForm;
    private final CommonMessageForm messageForm;

    /**
     * В конструкторе создаем все формы представления и добавляем их на него.
     */
    public SiebelGiftCardPaymentView() {
        enterCardNumberForm = new SiebelGiftCardPaymentEnterCardNumberForm(this);
        enterVerificationCodeForm = new SiebelGiftCardPaymentEnterVerificationCodeForm(this);
        confirmForm = new SiebelGiftCardPaymentConfirmForm(this);
        spinnerForm = new CommonSpinnerForm(this, ResBundlePaymentSiebelGiftCard.getString("GETTING_CARD_INFO"));
        messageForm = new CommonMessageForm(this);

        this.add(enterCardNumberForm, enterCardNumberForm.getClass().getName());
        this.add(enterVerificationCodeForm, enterVerificationCodeForm.getClass().getName());
        this.add(confirmForm, confirmForm.getClass().getName());
        this.add(spinnerForm, spinnerForm.getClass().getName());
        this.add(messageForm, messageForm.getClass().getName());
    }

    @Override
    public void setController(PaymentController controller) {
        this.controller = (SiebelGiftCardPaymentController) controller;
        enterCardNumberForm.setController(controller);
        enterVerificationCodeForm.setController(controller);
        confirmForm.setController(controller);
    }

    @Override
    public void modelChanged(PaymentModelChangedEvent event) {
        lastEvent = event;
        switch ((SiebelGiftCardPaymentState) lastEvent.getState()) {
            case VERIFICATION:
                setCurrentForm(enterVerificationCodeForm);
                break;
            case CASHIER_MESSAGE:
                SiebelGiftCardResult result = ((SiebelGiftCardPaymentInfo) event.getInfo()).getSiebelGiftCardResult();
                messageForm.setMessage(result.getCashierMessage());
                setCurrentForm(messageForm);
                break;
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
                if (lastEvent.getState() == SiebelGiftCardPaymentState.VERIFICATION) {
                    setCurrentForm(enterVerificationCodeForm);
                } else if (lastEvent.getState() == SiebelGiftCardPaymentState.CASHIER_MESSAGE) {
                    controller.processCashierMessageRead();
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
                // Ввели номер карты - вызываем метод проверки карты и получения баланса карты, возможного к списанию
                processCardInput(InsertType.HAND, enterCardNumberForm.getEnteredNumber());
                return true;
            } else if (currentForm == enterVerificationCodeForm) {
                // Ввели проверочный код из смс - вызываем метод проверки кода
                processVerificationCodeInput(enterVerificationCodeForm.getEnteredNumber());
                return true;
            } else if (currentForm == confirmForm) {
                try {
                    spinnerForm.setTextMessage(ResBundlePaymentSiebelGiftCard.getString("PROCESSING"));
                    setCurrentForm(spinnerForm);
                    controller.processPayWithGiftCard(confirmForm.getSumToPay());
                } catch (CardsException | SiebelServiceException ex) {
                    showErrorMessage(ex);
                }
                return true;
            } else if (lastEvent.getState() == SiebelGiftCardPaymentState.CASHIER_MESSAGE) {
                controller.processCashierMessageRead();
                return true;
            }
        } else if (e.getKeyCode() == KeyEvent.VK_KANA) {
            getController().beepError(e.getSource().toString());
        }
        return false;
    }

    @Override
    public boolean barcodeScanned(String barcode) {
        processCardInput(InsertType.SCANNER, barcode);
        return true;
    }

    @Override
    public boolean eventMSR(String track1, String track2, String track3, String track4) {
        processCardInput(InsertType.MSR, track1, track2, track3, track4);
        return true;
    }

    /**
     * Обработка введенного номера подарочной карты.
     */
    private void processCardInput(InsertType insertType, String... textStrings) {
        if (currentForm == messageForm || currentForm == spinnerForm) {
            return;
        }
        spinnerForm.setTextMessage(ResBundlePaymentSiebelGiftCard.getString("GETTING_CARD_INFO"));
        setCurrentForm(spinnerForm);
        try {
            controller.processCardInput(insertType, textStrings);
        } catch (CardsException | SiebelServiceException e) {
            showErrorMessage(e);
        }
    }

    /**
     * Обработка введенного проверочного кода из SMS.
     */
    private void processVerificationCodeInput(String code) {
        if (currentForm == messageForm || currentForm == spinnerForm) {
            return;
        }
        try {
            controller.processVerificationCodeInput(code);
        } catch (SiebelServiceException e) {
            showErrorMessage(e);
        }
    }

    private void showErrorMessage(Exception e) {
        String message = e.getCause() instanceof LoyProcessingTryItAgainException
                ? ResBundlePaymentSiebelGiftCard.getString("NO_CONNECTION_TO_SIEBEL")
                : e.getMessage();
        messageForm.setMessage(message);
        setCurrentForm(messageForm);
    }
}
