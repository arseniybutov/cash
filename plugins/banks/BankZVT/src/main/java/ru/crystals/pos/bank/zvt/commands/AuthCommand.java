package ru.crystals.pos.bank.zvt.commands;

import ru.crystals.pos.bank.zvt.utils.EncodingUtils;

public class AuthCommand implements Command {
    private long amount;

    public AuthCommand(long amount) {
        this.amount = amount;
    }

    @Override
    public boolean hasStatus() {
        return true;
    }

    @Override
    public String toString() {
        return "06010704" + EncodingUtils.encodeToBCD(amount, 12);
    }
}
