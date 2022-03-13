package ru.crystals.pos.bank.arcom;

import ru.crystals.pos.bank.BankDialog;
import ru.crystals.pos.bank.BankDialogEvent;
import ru.crystals.pos.bank.BankEvent;

import java.util.ArrayList;
import java.util.Collection;

public class DialogListener {
    private Collection<BankEvent> bankListeners = new ArrayList<>();
    private BankDialogEvent serviceOperationListener;


    public void showDialogScreen(BankDialog dialog) {
        for (BankEvent bankEvent : bankListeners) {
            bankEvent.showCustomProcessScreen(dialog);
        }
        if (serviceOperationListener != null) {
            serviceOperationListener.showCustomProcessScreen(dialog);
        }
    }

    public void addListeners(Collection<BankEvent> bankListeners) {
        this.bankListeners.addAll(bankListeners);
    }

    public void removeBankListeners() {
        bankListeners.clear();
    }

    public void addServiceOperationListener(BankDialogEvent serviceOperationsListener) {
        serviceOperationListener = serviceOperationsListener;
    }

    public void removeServiceOperationListener() {
        serviceOperationListener = null;
    }
}
