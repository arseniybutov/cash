package ru.crystals.pos.bank.tusson.exception;

import ru.crystals.pos.bank.exception.BankException;

public class TussonServiceOperationException extends BankException {

    public TussonServiceOperationException(String message) {
        super(message);
    }
}
