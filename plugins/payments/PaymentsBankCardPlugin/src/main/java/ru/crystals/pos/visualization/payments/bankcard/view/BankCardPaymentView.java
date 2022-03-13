package ru.crystals.pos.visualization.payments.bankcard.view;

import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonMessageForm;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonSpinnerForm;
import ru.crystals.pos.visualization.payments.bankcard.controller.BankCardPaymentController;
import ru.crystals.pos.visualization.payments.bankcard.model.BankCardPaymentInfo;
import ru.crystals.pos.visualization.payments.bankcard.model.BankCardPaymentState;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentView;
import ru.crystals.pos.visualization.payments.common.PaymentModelChangedEvent;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentController;

import javax.swing.JPanel;
import java.awt.event.KeyEvent;

/**
 * Created by agaydenger on 17.11.16.
 */
public class BankCardPaymentView extends AbstractPaymentView<BankCardPaymentController> {

    private BankDialogCommitCancelForm commitCancelForm = null;
    private BankCardPaymentEnterSumForm enterSumForm = null;
    private CommonSpinnerForm spinnerForm = null;
    private CommonMessageForm messageForm = null;
    private BankDialogInputComponent dialogInputComponent = null;
    private BankDialogListComponent dialogListComponent = null;


    public BankCardPaymentView() {
        commitCancelForm = new BankDialogCommitCancelForm(this);
        enterSumForm = new BankCardPaymentEnterSumForm(this);
        spinnerForm = new CommonSpinnerForm(this, "");
        messageForm = new CommonMessageForm(this);
        dialogInputComponent = new BankDialogInputComponent(this);
        dialogListComponent = new BankDialogListComponent(this);

        this.add(commitCancelForm, commitCancelForm.getClass().getName());
        this.add(enterSumForm, enterSumForm.getClass().getName());
        this.add(spinnerForm, spinnerForm.getClass().getName());
        this.add(messageForm, messageForm.getClass().getName());
        this.add(dialogInputComponent, dialogInputComponent.getClass().getName());
        this.add(dialogListComponent, dialogListComponent.getClass().getName());
    }

    @Override
    public void setController(PaymentController controller) {
        this.controller = (BankCardPaymentController) controller;
        commitCancelForm.setController(controller);
        enterSumForm.setController(controller);
        dialogInputComponent.setController(controller);
        dialogListComponent.setController(controller);
    }

    @Override
    public void modelChanged(PaymentModelChangedEvent event) {
        lastEvent = event;
        BankCardPaymentInfo info = ((BankCardPaymentInfo) event.getInfo());
        switch ((BankCardPaymentState) lastEvent.getState()) {
            case PAYMENT:
                setCurrentForm(enterSumForm);
                break;
            case ERROR:
                messageForm.setMessage(info.getExceptionText());
                setCurrentForm(messageForm);
                break;
            case SHOW_WAIT:
                spinnerForm.setTextMessage(info.getMessageText());
                setCurrentForm(spinnerForm);
                break;
            case DIALOG:
                JPanel panelToInstall = null;
                switch (info.getDialog().getDialogType()) {
                    case BINARY_SELECTION:
                        panelToInstall = commitCancelForm;
                        break;
                    case MESSAGE:
                        panelToInstall = spinnerForm;
                        spinnerForm.setTextMessage(info.getDialog().getMessage());
                        break;
                    case PERCENT_INPUT:
                    case STRING_INPUT:
                    case SUM_INPUT:
                        panelToInstall = dialogInputComponent;
                        break;
                    case LIST_SELECTION:
                        panelToInstall = dialogListComponent;
                        break;
                    default:
                        break;
                }
                if (panelToInstall != null) {
                    setCurrentForm(panelToInstall);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean dispatchKeyPressed(XKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            return onEsc();
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            return onEnter();
        }
        //Немного подстлим соломки, вдруг пролетит когда-нибудь из спиннера
        return currentForm == spinnerForm;
    }

    private boolean onEnter() {
        if (currentForm == messageForm) {
            setCurrentForm(enterSumForm);
            return true;
        }
        return false;
    }

    private boolean onEsc() {
        if (currentForm == messageForm) {
            setCurrentForm(enterSumForm);
            return true;
        }
        return currentForm != enterSumForm;
    }

    @Override
    public boolean barcodeScanned(String barcode) {
        return currentForm != enterSumForm;
    }

    @Override
    public boolean eventMSR(String track1, String track2, String track3, String track4) {
        return currentForm != enterSumForm;
    }

    public boolean isMoveCursorAvailable() {
        return ((BankCardPaymentState) lastEvent.getState()) == BankCardPaymentState.PAYMENT;
    }
}
