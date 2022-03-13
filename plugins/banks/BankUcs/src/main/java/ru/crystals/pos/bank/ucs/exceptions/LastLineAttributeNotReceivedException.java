package ru.crystals.pos.bank.ucs.exceptions;

import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.exception.BankException;

public class LastLineAttributeNotReceivedException extends BankException {
    private static final long serialVersionUID = -3692788472860563020L;

    public LastLineAttributeNotReceivedException(AuthorizationData authorizationData) {
        super(authorizationData);
    }
}
