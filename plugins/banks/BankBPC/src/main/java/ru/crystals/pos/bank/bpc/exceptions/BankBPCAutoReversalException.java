package ru.crystals.pos.bank.bpc.exceptions;

import ru.crystals.pos.bank.exception.BankException;

public class BankBPCAutoReversalException extends BankException {
    private final String refNumber;

    public BankBPCAutoReversalException(String refNumber) {
        this.refNumber = refNumber;
    }

    public String getRefNumber() {
        return refNumber;
    }
}
