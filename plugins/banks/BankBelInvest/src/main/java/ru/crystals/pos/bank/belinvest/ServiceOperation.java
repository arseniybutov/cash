package ru.crystals.pos.bank.belinvest;

import ru.crystals.pos.bank.datastruct.ServiceBankOperation;

public class ServiceOperation extends ServiceBankOperation {
    private ServiceBankOperationType type;

    public ServiceOperation(ServiceBankOperationType type) {
        this.type = type;
    }

    @Override
    public String getCommandTitle() {
        return type.getCommandTitle();
    }

    @Override
    public String getFormTitle() {
        return type.getFormTitle();
    }

    @Override
    public String getSpinnerMessage() {
        return type.getSpinnerMessage();
    }

    public String[] getParams() {
        return type.getParams();
    }

    public String getOperation() {
        return type.getParams()[0];
    }

    ServiceBankOperationType getType() {
        return type;
    }
}
