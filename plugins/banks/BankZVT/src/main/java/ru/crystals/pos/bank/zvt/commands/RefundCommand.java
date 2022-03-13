package ru.crystals.pos.bank.zvt.commands;

import ru.crystals.pos.bank.zvt.utils.EncodingUtils;

public class RefundCommand implements Command {
    private String password;
    private long amount;

    public RefundCommand(String password, long amount) {
        this.password = password;
        this.amount = amount;
    }

    @Override
    public boolean hasStatus() {
        return true;
    }

    @Override
    public String toString() {
        return "0631" + "0A" + EncodingUtils.encodeToBCD(password, 6) + "04" + EncodingUtils.encodeToBCD(amount, 12);
    }
}
