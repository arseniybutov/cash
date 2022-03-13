package ru.crystals.pos.bank.translink;

import ru.crystals.pos.bank.datastruct.ServiceBankOperation;
import ru.crystals.pos.bank.datastruct.TypeOperation;

public class PrintReportOperation extends ServiceBankOperation {

    @Override
    public String getCommandTitle() {
        return ResBundleBankTranslink.getString("PRINT_REPORT");
    }

    @Override
    public String getFormTitle() {
        return ResBundleBankTranslink.getString("PRINT_REPORT");
    }

    @Override
    public String getSpinnerMessage() {
        return ResBundleBankTranslink.getString("PRINT_REPORT");
    }

    @Override
    public TypeOperation getTypeOperation() {
        return TypeOperation.BANK_SHORT_REPORT;
    }

}
