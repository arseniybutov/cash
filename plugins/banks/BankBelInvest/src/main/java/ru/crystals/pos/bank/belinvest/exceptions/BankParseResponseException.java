package ru.crystals.pos.bank.belinvest.exceptions;

import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.exception.BankException;

/**
 * Created by Tatarinov Eduard on 23.11.16.
 */
public class BankParseResponseException extends BankException {
    public BankParseResponseException() {
        //
    }

    public BankParseResponseException(String message) {
        super(message);
    }

    public BankParseResponseException(AuthorizationData ad) {
        super(ad);
    }
}
