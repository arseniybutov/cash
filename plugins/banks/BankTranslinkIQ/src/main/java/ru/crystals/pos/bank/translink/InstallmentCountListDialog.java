package ru.crystals.pos.bank.translink;

import ru.crystals.pos.bank.BankDialog;
import ru.crystals.pos.bank.BankDialogType;
import ru.crystals.pos.bank.ListItem;

import java.util.List;

public class InstallmentCountListDialog implements BankDialog {

    private final String title;
    private final List<ListItem> items;

    public InstallmentCountListDialog(String title, List<ListItem> items) {
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
