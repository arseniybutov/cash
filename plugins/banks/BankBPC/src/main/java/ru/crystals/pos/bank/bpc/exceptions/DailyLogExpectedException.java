package ru.crystals.pos.bank.bpc.exceptions;

import ru.crystals.pos.bank.bpc.ResBundleBankBPC;
import ru.crystals.pos.bank.exception.BankException;

public class DailyLogExpectedException extends BankException {
    public DailyLogExpectedException() {
        super(ResBundleBankBPC.getString("DAILY_LOG_OPERATION_EXPECTED"));
    }
}
