package ru.crystals.pos.bank.opensbp;

import ru.crystals.pos.bank.commonsbpprovider.BanksSBPCommon;
import ru.crystals.pos.bank.commonsbpprovider.SBPProvider;

public class BankOpenSBPImpl extends BanksSBPCommon {
    public BankOpenSBPImpl(SBPProvider sbpProvider) {
        super(sbpProvider);
    }

    public BankOpenSBPImpl() {
        super(new BankOpenSBPProvider());
    }
}
