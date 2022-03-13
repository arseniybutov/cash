package ru.crystals.pos.bank.arcom.operations;

import ru.crystals.pos.bank.arcom.ResBundleBankArcom;
import ru.crystals.pos.bank.datastruct.ServiceBankOperation;
import ru.crystals.pos.bank.datastruct.TypeOperation;

/**
 * Сервисная команда - полный отчет по банку
 */
public class FullReportOperation extends ServiceBankOperation {

    @Override
    public String getCommandTitle() {
        return ResBundleBankArcom.getString(TypeOperation.BANK_FULL_REPORT.name());
    }

    @Override
    public String getFormTitle() {
        return ResBundleBankArcom.getString(TypeOperation.BANK_FULL_REPORT.name());
    }

    @Override
    public String getSpinnerMessage() {
        return ResBundleBankArcom.getString(TypeOperation.BANK_FULL_REPORT.name());
    }

    @Override
    public TypeOperation getTypeOperation() {
        return TypeOperation.BANK_FULL_REPORT;
    }

}