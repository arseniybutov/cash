package ru.crystals.pos.bank.belinvest.exceptions;

import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.exception.BankException;

/**
 * Created by Tatarinov Eduard on 23.11.16.
 */
public class BankMakeRequestException extends BankException {
    public BankMakeRequestException() {
        //
    }

    public BankMakeRequestException(String message) {
        super(message);
    }

    public BankMakeRequestException(AuthorizationData ad) {
        super(ad);
    }
}
