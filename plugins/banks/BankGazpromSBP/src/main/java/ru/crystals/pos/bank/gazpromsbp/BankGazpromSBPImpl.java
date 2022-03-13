package ru.crystals.pos.bank.gazpromsbp;

import ru.crystals.pos.bank.commonsbpprovider.BanksSBPCommon;
import ru.crystals.pos.bank.commonsbpprovider.SBPProvider;

public class BankGazpromSBPImpl extends BanksSBPCommon {

    public BankGazpromSBPImpl(SBPProvider sbpProvider) {
        super(sbpProvider);
    }

    public BankGazpromSBPImpl() {
        super(new BankGazpromSBPProvider());
    }
}
