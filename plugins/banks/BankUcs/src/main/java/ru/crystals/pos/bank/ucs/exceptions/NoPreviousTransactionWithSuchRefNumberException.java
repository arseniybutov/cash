package ru.crystals.pos.bank.ucs.exceptions;

import ru.crystals.pos.bank.exception.BankAuthorizationException;
import ru.crystals.pos.bank.ucs.ResBundleBankUcs;

public class NoPreviousTransactionWithSuchRefNumberException extends BankAuthorizationException {
    private static final long serialVersionUID = 361457887283700251L;

    @Override
    public String getMessage() {
        return "54 " + ResBundleBankUcs.getString("TRANSACTION_NOT_FOUND");
    }
}
