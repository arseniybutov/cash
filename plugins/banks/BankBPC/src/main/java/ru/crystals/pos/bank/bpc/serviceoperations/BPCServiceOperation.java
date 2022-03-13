package ru.crystals.pos.bank.bpc.serviceoperations;

import ru.crystals.pos.bank.bpc.Request;
import ru.crystals.pos.bank.datastruct.ServiceBankOperation;

public abstract class BPCServiceOperation extends ServiceBankOperation {
    public abstract Request createRequest(String ecr, String ern);
}
