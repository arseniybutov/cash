package ru.crystals.pos.bank.ucs.serviceoperations;

import ru.crystals.pos.bank.datastruct.ServiceBankOperation;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.ucs.messages.requests.Request;

public abstract class UCSServiceOperation extends ServiceBankOperation {
    public abstract Request createRequest() throws BankException;

    public abstract boolean needsInLastTransactionID();

    public abstract boolean hasInitialResponse();
}
