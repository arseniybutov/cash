package ru.crystals.pos.bank.zvt.commands;

import ru.crystals.pos.bank.zvt.utils.EncodingUtils;

public class EndOfDayCommand implements Command {

    private String password;

    public EndOfDayCommand(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "065003" + EncodingUtils.encodeToBCD(password, 6);
    }

    @Override
    public boolean hasStatus() {
        return true;
    }
}
