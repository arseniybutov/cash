package ru.crystals.pos.bank.sberbank;

import ru.crystals.pos.bank.BankDialog;
import ru.crystals.pos.bank.BankDialogType;

import java.util.ArrayList;
import java.util.List;

public class SberbankBankDialog implements BankDialog {
    private String title;
    private BankDialogType dialogType;
    private String message;
    private List<String> buttons;

    public static SberbankBankDialog createDialog(String dialogMessage) {
        SberbankBankDialog dialog = new SberbankBankDialog();
        dialog.setDialogType(BankDialogType.BINARY_SELECTION);
        dialog.setMessage(dialogMessage);
        List<String> buttons = new ArrayList<>();
        buttons.add(ResBundleBankSberbank.getString("CANCEL_BUTTON"));
        buttons.add(ResBundleBankSberbank.getString("REPEAT_BUTTON"));
        dialog.setButtons(buttons);
        return dialog;
    }

    private void setButtons(List<String> buttons) {
        this.buttons = buttons;
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
