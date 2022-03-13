package ru.crystals.pos.bank.tinkoffsbp;

import ru.crystals.pos.bank.commonsbpprovider.BanksSBPCommon;
import ru.crystals.pos.bank.commonsbpprovider.SBPProvider;

public class BankTinkoffSBPImpl extends BanksSBPCommon {
    public BankTinkoffSBPImpl(SBPProvider sbpProvider) {
        super(sbpProvider);
    }

    public BankTinkoffSBPImpl() {
        super(new BankTinkoffSBPProvider());
    }
}
