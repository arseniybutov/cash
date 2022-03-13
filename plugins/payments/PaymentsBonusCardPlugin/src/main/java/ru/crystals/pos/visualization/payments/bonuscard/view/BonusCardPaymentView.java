package ru.crystals.pos.visualization.payments.bonuscard.view;

import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonMessageForm;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonSpinnerForm;
import ru.crystals.pos.visualization.payments.bonuscard.ResBundlePaymentBonusCard;
import ru.crystals.pos.visualization.payments.bonuscard.controller.BonusCardPaymentController;
import ru.crystals.pos.visualization.payments.bonuscard.model.BonusCardPaymentInfo;
import ru.crystals.pos.visualization.payments.bonuscard.model.BonusCardPaymentState;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentView;
import ru.crystals.pos.visualization.payments.common.PaymentModelChangedEvent;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentController;

import java.awt.event.KeyEvent;


public class BonusCardPaymentView extends AbstractPaymentView<BonusCardPaymentController> {
    private BonusCardPaymentEnterCardNumberForm enterCardNumberForm = null;
    private BonusCardPaymentConfirmForm confirmForm = null;
    private BonusCardPaymentWriteOffConfirmForm alertForm = null;
    private BonusCardPaymentAccountListForm accountListForm = null;
    private CommonSpinnerForm spinnerForm = null;
    private CommonMessageForm messageForm = null;

    public BonusCardPaymentView() {
        enterCardNumberForm = new BonusCardPaymentEnterCardNumberForm(this);
        confirmForm = new BonusCardPaymentConfirmForm(this);
        spinnerForm = new CommonSpinnerForm(this, ResBundlePaymentBonusCard.getString("CHECK_DATA_WITH_SERVER"));
        messageForm = new CommonMessageForm(this);
        alertForm = new BonusCardPaymentWriteOffConfirmForm(this);
        accountListForm = new BonusCardPaymentAccountListForm(this);

        this.add(enterCardNumberForm, enterCardNumberForm.getClass().getName());
        this.add(confirmForm, confirmForm.getClass().getName());
        this.add(spinnerForm, spinnerForm.getClass().getName());
        this.add(messageForm, messageForm.getClass().getName());
        this.add(alertForm, alertForm.getClass().getName());
        this.add(accountListForm, accountListForm.getClass().getName());
    }

    @Override
    public void setController(PaymentController controller) {
        this.controller = (BonusCardPaymentController) controller;
        enterCardNumberForm.setController(controller);
        confirmForm.setController(controller);
        alertForm.setController(controller);
        accountListForm.setController(controller);
    }

    @Override
    public void modelChanged(PaymentModelChangedEvent event) {
        lastEvent = event;
        switch ((BonusCardPaymentState) lastEvent.getState()) {
            case PAYMENT:
                setCurrentForm(confirmForm);
                break;
            case PARSE_INTERNAL_CARD:
                spinnerForm.setTextMessage(ResBundlePaymentBonusCard.getString("CHECK_DATA_WITH_SERVER"));
                setCurrentForm(spinnerForm);
                break;
            case ERROR:
                messageForm.setMessage(((BonusCardPaymentInfo) event.getInfo()).getExceptionText());
                setCurrentForm(messageForm);
                break;
            case CONFIRM_AMOUNT:
                setCurrentForm(alertForm);
                break;
            case CHOOSE_ACCOUNT:
                setCurrentForm(accountListForm);
                break;
            case ENTER_CARD_NUMBER:
                setCurrentForm(enterCardNumberForm);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean dispatchKeyPressed(XKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            return onEsc(e);
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            return onEnter();
        } else if (e.getKeyCode() == KeyEvent.VK_KANA) {
            getController().beepError(e.getSource().toString());
        }
        return true;
    }

    @Override
    public boolean barcodeScanned(String barcode) {
        if (!controller.isInformixProcessing() && currentForm == enterCardNumberForm) {
            spinnerForm.setTextMessage(ResBundlePaymentBonusCard.getString("CHECK_DATA_WITH_SERVER"));
            setCurrentForm(spinnerForm);
            try {
                controller.getAccountInformation();
            } catch (Exception exception) {
                messageForm.setMessage(exception.getMessage());
                setCurrentForm(messageForm);
            }
        }
        return true;
    }

    @Override
    public boolean eventMSR(String track1, String track2, String track3, String track4) {
        if (!controller.isInformixProcessing() && currentForm == enterCardNumberForm) {
            spinnerForm.setTextMessage(ResBundlePaymentBonusCard.getString("CHECK_DATA_WITH_SERVER"));
            setCurrentForm(spinnerForm);
            try {
                controller.getAccountInformation();
            } catch (Exception exception) {
                messageForm.setMessage(exception.getMessage());
                setCurrentForm(messageForm);
            }
        }
        return true;
    }

    private boolean onEnter() {
        if (currentForm == enterCardNumberForm) {
            spinnerForm.setTextMessage(ResBundlePaymentBonusCard.getString("CHECK_DATA_WITH_SERVER"));
            setCurrentForm(spinnerForm);
            try {
                controller.getAccountInformation();
            } catch (Exception exception) {
                messageForm.setMessage(exception.getMessage());
                setCurrentForm(messageForm);
            }
        } else if (currentForm == confirmForm) {
            if (controller.verifySum(confirmForm.getSumToPay())) {
                processPayment();
            } else {
                setCurrentForm(alertForm);
            }
        } else if (currentForm == accountListForm) {
            setCurrentForm(confirmForm);
        } else if (currentForm == alertForm) {
            if (alertForm.isConfirmButtonPressed()) {
                processPayment();
            } else {
                setCurrentForm(confirmForm);
            }
        }
        return true;
    }

    private void processPayment() {
        try {
            spinnerForm.setTextMessage(ResBundlePaymentBonusCard.getString("CHECK_DATA_WITH_SERVER"));
            setCurrentForm(spinnerForm);
            controller.processPaymentBonuses(confirmForm.getSumToPay());

        } catch (Exception exception) {
            messageForm.setMessage(exception.getMessage());
            setCurrentForm(messageForm);
        }
    }

    private boolean onEsc(XKeyEvent keyEvent) {
        if (currentForm == confirmForm) {
            if (controller.isRefund()) {
                return false;
            } else {
                controller.unblockBonusesIfNeeded();
                if (controller.getModel().getInfo().getCardInfo().getBonusAccounts().size() > 1) {
                    controller.getModel().setState(BonusCardPaymentState.CHOOSE_ACCOUNT);
                    return true;
                } else {
                    return false;
                }
            }
        } else if (currentForm == messageForm) {
            if (lastEvent.getState() == BonusCardPaymentState.ERROR) {
                return false;
            }
            setCurrentForm(enterCardNumberForm);
            return true;
        } else if (currentForm == enterCardNumberForm) {
            return enterCardNumberForm.keyPressedNew(keyEvent);
        } else if (currentForm == accountListForm) {
            controller.unblockBonusesIfNeeded();
            controller.getModel().setState(BonusCardPaymentState.ENTER_CARD_NUMBER);
            return true;
        }
        return false;
    }

    public BonusCardPaymentState getState() {
        return (BonusCardPaymentState) lastEvent.getState();
    }
}
