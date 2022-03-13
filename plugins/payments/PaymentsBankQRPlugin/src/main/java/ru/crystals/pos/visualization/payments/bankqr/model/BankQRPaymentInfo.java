package ru.crystals.pos.visualization.payments.bankqr.model;

import ru.crystals.pos.bank.BankDialog;
import ru.crystals.pos.visualization.payments.common.DefaultPaymentInfo;

public class BankQRPaymentInfo extends DefaultPaymentInfo {
    private BankDialog dialog;
    private String exceptionText;
    private String messageText;

    public BankDialog getDialog() {
        return dialog;
    }

    public void setDialog(BankDialog dialog) {
        this.dialog = dialog;
    }

    public String getExceptionText() {
        return exceptionText;
    }

    public void setExceptionText(String exceptionText) {
        this.exceptionText = exceptionText;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
}
