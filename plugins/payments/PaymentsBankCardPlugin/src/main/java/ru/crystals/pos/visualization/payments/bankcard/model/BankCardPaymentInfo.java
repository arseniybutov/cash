package ru.crystals.pos.visualization.payments.bankcard.model;

import ru.crystals.pos.bank.BankDialog;
import ru.crystals.pos.visualization.payments.common.DefaultPaymentInfo;

/**
 * Created by agaydenger on 17.11.16.
 */
public class BankCardPaymentInfo extends DefaultPaymentInfo {
    private BankDialog dialog;
    private String exceptionText;
    private String messageText;
    private Long returnCashOutAmount;

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

    public Long getReturnCashOutAmount() {
        return returnCashOutAmount;
    }

    public void setReturnCashOutAmount(Long returnCashOutAmount) {
        this.returnCashOutAmount = returnCashOutAmount;
    }
}
