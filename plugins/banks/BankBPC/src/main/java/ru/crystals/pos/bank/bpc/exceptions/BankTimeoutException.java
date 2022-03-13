package ru.crystals.pos.bank.bpc.exceptions;

import ru.crystals.pos.bank.exception.BankException;

public class BankTimeoutException extends BankException {
    public BankTimeoutException(String message) {
        super(message);
    }
}
