package ru.crystals.pos.visualization.payments.bankqr.view;

import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonDialogForm;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonMessageForm;
import ru.crystals.pos.visualization.payments.bankqr.ResBundlePaymentBankQR;
import ru.crystals.pos.visualization.payments.bankqr.controller.BankQRPaymentController;
import ru.crystals.pos.visualization.payments.bankqr.model.BankQRPaymentInfo;
import ru.crystals.pos.visualization.payments.bankqr.model.BankQRPaymentState;
import ru.crystals.pos.visualization.payments.common.AbstractPaymentView;
import ru.crystals.pos.visualization.payments.common.PaymentModelChangedEvent;
import ru.crystals.pos.visualization.payments.common.PaymentSpinnerForm;
import ru.crystals.pos.visualization.payments.common.interfaces.PaymentController;

import javax.swing.JPanel;
import java.awt.event.KeyEvent;
import java.util.concurrent.TimeUnit;

public class BankQRPaymentView extends AbstractPaymentView<BankQRPaymentController> {

    /**
     * Время на показ кнопки отмены после начала банковской операции продажи
     */
    private static final int DEFAULT_CANCEL_SHOW_TIME = (int) TimeUnit.SECONDS.toMillis(5);

    private final BankDialogCommitCancelForm commitCancelForm;
    private final BankQRPaymentEnterSumForm enterSumForm;
    private final PaymentSpinnerForm paymentSpinnerForm;
    private final CommonMessageForm messageForm;
    private final BankDialogInputComponent dialogInputComponent;
    private final BankDialogListComponent dialogListComponent;
    private final ScanQrMessageForm scanQrForm;
    private final CommonDialogForm paymentCancelForm;

    public BankQRPaymentView() {
        commitCancelForm = new BankDialogCommitCancelForm(this);
        enterSumForm = new BankQRPaymentEnterSumForm(this);
        paymentSpinnerForm = new PaymentSpinnerForm(this, "", ResBundlePaymentBankQR.getString("ABORT_OPERATION"));
        messageForm = new CommonMessageForm(this);
        dialogInputComponent = new BankDialogInputComponent(this);
        dialogListComponent = new BankDialogListComponent(this);
        scanQrForm = new ScanQrMessageForm(this);
        paymentCancelForm = new CommonDialogForm(this);
        paymentCancelForm.setMessage(ResBundlePaymentBankQR.getString("ABORT_SALE_OPERATION_QUESTION"));


        this.add(commitCancelForm, commitCancelForm.getClass().getName());
        this.add(enterSumForm, enterSumForm.getClass().getName());
        this.add(paymentSpinnerForm, paymentSpinnerForm.getClass().getName());
        this.add(messageForm, messageForm.getClass().getName());
        this.add(dialogInputComponent, dialogInputComponent.getClass().getName());
        this.add(dialogListComponent, dialogListComponent.getClass().getName());
        this.add(scanQrForm, scanQrForm.getClass().getName());
        this.add(paymentCancelForm, paymentCancelForm.getClass().getName());
    }

    @Override
    public void setController(PaymentController controller) {
        this.controller = (BankQRPaymentController) controller;
        commitCancelForm.setController(controller);
        enterSumForm.setController(controller);
        dialogInputComponent.setController(controller);
        dialogListComponent.setController(controller);
    }

    @Override
    public void modelChanged(PaymentModelChangedEvent event) {
        lastEvent = event;
        BankQRPaymentInfo info = ((BankQRPaymentInfo) event.getInfo());
        switch ((BankQRPaymentState) lastEvent.getState()) {
            case PAYMENT:
                setCurrentForm(enterSumForm);
                break;
            case ERROR:
                messageForm.setMessage(info.getExceptionText());
                paymentSpinnerForm.hideButton();
                setCurrentForm(messageForm);
                break;
            case SHOW_WAIT:
                paymentSpinnerForm.setTextMessage(info.getMessageText());
                paymentSpinnerForm.hideButton();
                setCurrentForm(paymentSpinnerForm);
                break;
            case SHOW_ABORT_WAIT:
                paymentSpinnerForm.hideButton();
                paymentSpinnerForm.setTextMessage(info.getMessageText());
                paymentSpinnerForm.startButtonShow(getCancelAvailableTime());
                setCurrentForm(paymentSpinnerForm);
                break;
            case DIALOG:
                JPanel panelToInstall = null;
                switch (info.getDialog().getDialogType()) {
                    case BINARY_SELECTION:
                        panelToInstall = commitCancelForm;
                        break;
                    case MESSAGE:
                        panelToInstall = paymentSpinnerForm;
                        paymentSpinnerForm.setTextMessage(info.getDialog().getMessage());
                        paymentSpinnerForm.hideButton();
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
            case SCAN_CUSTOMER_QR:
                scanQrForm.setMessage(ResBundlePaymentBankQR.getString("SCAN_CUSTOMER_QR"));
                setCurrentForm(scanQrForm);
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
        return currentForm == paymentSpinnerForm;
    }

    private boolean onEnter() {
        if (currentForm == messageForm) {
            setCurrentForm(enterSumForm);
            return true;
        }
        //Отменяем операцию оплаты если кнопка видна
        if (currentForm == paymentSpinnerForm && paymentSpinnerForm.isButtonAvailable()) {
            setCurrentForm(paymentCancelForm);
            return true;
        }
        if (currentForm == paymentCancelForm) {
            if (paymentCancelForm.isYes()) {
                paymentSpinnerForm.hideButton();
                controller.setBankOperationStopped(true);
            }
            setCurrentForm(paymentSpinnerForm);
            return true;
        }
        return false;
    }

    private boolean onEsc() {
        if (currentForm == messageForm) {
            setCurrentForm(enterSumForm);
            return true;
        }
        if (currentForm == paymentCancelForm) {
            setCurrentForm(paymentSpinnerForm);
            return true;
        }
        if (currentForm == scanQrForm) {
            setCurrentForm(enterSumForm);
            controller.checkDisplayHideAll();
            return true;
        }
        return currentForm != enterSumForm;
    }

    @Override
    public boolean barcodeScanned(String barcode) {
        if (currentForm == scanQrForm) {
            controller.processSaleByCustomerQR(barcode);
            return true;
        }
        return currentForm != enterSumForm;
    }

    @Override
    public boolean eventMSR(String track1, String track2, String track3, String track4) {
        return currentForm != enterSumForm;
    }

    public boolean isMoveCursorAvailable() {
        return lastEvent.getState() == BankQRPaymentState.PAYMENT;
    }

    private int getCancelAvailableTime() {
        if (controller.getModel().getPayment() != null) {
            Integer paymentCancelAvailableTime = ((ru.crystals.pos.payments.BankQRPaymentController)
                    controller.getModel().getPayment().getPaymentSettings()).
                    getCancelAvailableTime();
            if (paymentCancelAvailableTime != null) {
                return (int) TimeUnit.SECONDS.toMillis(paymentCancelAvailableTime);
            }
        }
        return DEFAULT_CANCEL_SHOW_TIME;
    }
}
