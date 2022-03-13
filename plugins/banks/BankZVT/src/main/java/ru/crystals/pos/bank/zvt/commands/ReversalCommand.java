package ru.crystals.pos.bank.zvt.commands;

import ru.crystals.pos.bank.zvt.utils.EncodingUtils;

public class ReversalCommand implements Command {
    private String password;
    private String receiptNumber;

    public ReversalCommand(String password, String receiptNumber) {
        this.password = password;
        this.receiptNumber = receiptNumber;
    }

    @Override
    public boolean hasStatus() {
        return true;
    }

    @Override
    public String toString() {
        return "0630" + "06" + EncodingUtils.encodeToBCD(password, 6) + "87" + EncodingUtils.encodeToBCD(receiptNumber, 4);
    }
}
