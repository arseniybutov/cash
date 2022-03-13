package ru.crystals.pos.bank.emulator;

import ru.crystals.pos.bank.BankDialog;
import ru.crystals.pos.bank.BankDialogType;
import ru.crystals.pos.bank.ListItem;

import java.util.List;

public class InstallmentDialog implements BankDialog {

    private final String title;
    private final List<ListItem> items;

    public InstallmentDialog(String title, List<ListItem> items) {
        this.title = title;
        this.items = items;
    }

    @Override
    public BankDialogType getDialogType() {
        return BankDialogType.EXTENDED_LIST_SELECTION;
    }

    @Override
    public List<ListItem> getListItems() {
        return items;
    }

    @Override
    public String getTitle() {
        return title;
    }
}
