package ru.crystals.pos.bank.tusson.serviceoperations;

import ru.crystals.pos.bank.datastruct.ServiceBankOperation;

import java.util.List;

public abstract class BankTussonServiceOperation extends ServiceBankOperation {
    public abstract List<List<String>> process();

    public abstract void suspend();

    @Override
    public boolean isOperationSuspendAllowed() {
        return true;
    }
}
