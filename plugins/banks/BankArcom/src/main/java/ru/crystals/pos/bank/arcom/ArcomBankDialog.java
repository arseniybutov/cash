package ru.crystals.pos.bank.arcom;

import ru.crystals.pos.bank.BankDialog;
import ru.crystals.pos.bank.BankDialogType;

import java.util.List;

public class ArcomBankDialog implements BankDialog {

    private String title;
    private BankDialogType dialogType;
    private String message;
    private List<String> buttons;

    public static ArcomBankDialog createDialog(String dialogMessage) {
        ArcomBankDialog dialog = new ArcomBankDialog();
        dialog.setDialogType(BankDialogType.MESSAGE);
        dialog.setMessage(dialogMessage);
        return dialog;
    }

    private void setMessage(String message) {
        this.message = message;
    }

    @Override
    public BankDialogType getDialogType() {
        return dialogType;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public List<String> getValues() {
        return null;
    }

    @Override
    public List<String> getButtons() {
        return buttons;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getTitle() {
        return title;
    }

    private void setDialogType(BankDialogType dialogType) {
        this.dialogType = dialogType;
    }
}
