package ru.crystals.pos.visualization.payments.kopilkabonuscard.view;

import ru.crystals.pos.check.InsertType;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonMessageForm;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonSpinnerForm;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentView;
import ru.crystals.pos.visualization.payments.common.PaymentModelChangedEvent;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentController;
import ru.crystals.pos.visualization.payments.kopilkabonuscard.ResBundlePaymentKopilkaBonusCard;
import ru.crystals.pos.visualization.payments.kopilkabonuscard.controller.KopilkaBonusCardPaymentController;
import ru.crystals.pos.visualization.payments.kopilkabonuscard.model.KopilkaBonusCardPaymentInfo;
import ru.crystals.pos.visualization.payments.kopilkabonuscard.model.KopilkaBonusCardPaymentState;

import java.awt.event.KeyEvent;
import java.util.Objects;

/**
 * Отображение форм при оплате бонусами Копилка
 */
public class KopilkaBonusCardPaymentView extends AbstractPaymentView<KopilkaBonusCardPaymentController> {
    private KopilkaBonusCardPaymentEnterCardNumberForm cardNumberForm = null;
    private KopilkaBonusCardPaymentConfirmForm confirmForm = null;
    private CommonSpinnerForm spinnerForm = null;
    private CommonMessageForm messageForm = null;

    /**
     * В конструкторе создаем все формы представления и добавляем их на него
     */
    public KopilkaBonusCardPaymentView() {
        cardNumberForm = new KopilkaBonusCardPaymentEnterCardNumberForm(this);
        confirmForm = new KopilkaBonusCardPaymentConfirmForm(this);
        spinnerForm = new CommonSpinnerForm(this, ResBundlePaymentKopilkaBonusCard.getString("GETTING_KOPILKA_BONUSCARD_INFO"));
        messageForm = new CommonMessageForm(this);

        this.add(cardNumberForm, cardNumberForm.getClass().getName());
        this.add(confirmForm, confirmForm.getClass().getName());
        this.add(spinnerForm, spinnerForm.getClass().getName());
        this.add(messageForm, messageForm.getClass().getName());
    }

    @Override
    public void setController(PaymentController controller) {
        this.controller = (KopilkaBonusCardPaymentController) controller;
        cardNumberForm.setController(controller);
        confirmForm.setController(controller);
    }

    @Override
    public void modelChanged(PaymentModelChangedEvent event) {
        lastEvent = event;
        switch ((KopilkaBonusCardPaymentState) lastEvent.getState()) {
            case ENTER_CARD_NUMBER:
                setCurrentForm(cardNumberForm);
                break;
            case PAYMENT:
                setCurrentForm(confirmForm);
                break;
            case PARSE_CARD:
                spinnerForm.setTextMessage(ResBundlePaymentKopilkaBonusCard.getString("GETTING_KOPILKA_BONUSCARD_INFO"));
                setCurrentForm(spinnerForm);
                break;
            case ERROR:
                messageForm.setMessage(((KopilkaBonusCardPaymentInfo) event.getInfo()).getExceptionText());
                setCurrentForm(messageForm);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean dispatchKeyPressed(XKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (currentForm == messageForm) {
                if (lastEvent.getState() == KopilkaBonusCardPaymentState.ERROR) {
                    return false;
                }
                setCurrentForm(cardNumberForm);
                return true;
            } else if (currentForm == cardNumberForm) {
                return cardNumberForm.keyPressedNew(e);
            }
            return false;
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (currentForm == confirmForm) {
                try {
                    spinnerForm.setTextMessage(ResBundlePaymentKopilkaBonusCard.getString("KOPILKA_BONUSCARD_PAYMENT"));
                    setCurrentForm(spinnerForm);
                    controller.processPayWithCard(confirmForm.getSumToPay());
                } catch (Exception exception) {
                    messageForm.setMessage(Objects.toString(exception.getMessage(), ResBundlePaymentKopilkaBonusCard.getString("KOPILKA_PAYMENT_ERROR")));
                    setCurrentForm(messageForm);
                }
                return true;
            }
        } else if (e.getKeyCode() == KeyEvent.VK_KANA) {
            getController().beepError(e.getSource().toString());
        }
        return false;
    }

    @Override
    public boolean barcodeScanned(String barcode) {
        return eventFindCard(InsertType.SCANNER, barcode);
    }

    @Override
    public boolean eventMSR(String track1, String track2, String track3, String track4) {
        return eventFindCard(InsertType.MSR, track1, track2, track3, track4);
    }

    private boolean eventFindCard(InsertType insertType, String... cardNumbers) {
        if (currentForm == messageForm || currentForm == spinnerForm) {
            return true;
        }
        setCurrentForm(spinnerForm);
        try {
            controller.processGetCardInfo(insertType, cardNumbers);
        } catch (Exception exception) {
            messageForm.setMessage(exception.getMessage());
            setCurrentForm(messageForm);
        }
        return true;
    }
}
