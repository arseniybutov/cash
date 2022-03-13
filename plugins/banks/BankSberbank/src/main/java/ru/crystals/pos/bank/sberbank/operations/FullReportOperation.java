package ru.crystals.pos.bank.sberbank.operations;

import ru.crystals.pos.bank.datastruct.ServiceBankOperation;
import ru.crystals.pos.bank.datastruct.TypeOperation;
import ru.crystals.pos.bank.sberbank.ResBundleBankSberbank;

/**
 * Сервисная команда - полный отчет по банку
 */
public class FullReportOperation extends ServiceBankOperation {

    @Override
    public String getCommandTitle() {
        return ResBundleBankSberbank.getString(TypeOperation.BANK_FULL_REPORT.name());
    }

    @Override
    public String getFormTitle() {
        return ResBundleBankSberbank.getString(TypeOperation.BANK_FULL_REPORT.name());
    }

    @Override
    public String getSpinnerMessage() {
        return ResBundleBankSberbank.getString(TypeOperation.BANK_FULL_REPORT.name());
    }

    @Override
    public TypeOperation getTypeOperation() {
        return TypeOperation.BANK_FULL_REPORT;
    }

}