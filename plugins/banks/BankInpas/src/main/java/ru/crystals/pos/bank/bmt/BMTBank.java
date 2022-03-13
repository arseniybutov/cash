package ru.crystals.pos.bank.bmt;

import ru.crystals.pos.bank.pbf.PBFBank;

public class BMTBank extends PBFBank {

    @Override
    public boolean canBeUsedWithOtherBanks() {
        return true;
    }

    @Override
    public synchronized boolean requestTerminalStateIfOffline() {
        // Пока не работает в БМТ, когда почнят, сделать как в ПБФ
        return true;
    }
}
