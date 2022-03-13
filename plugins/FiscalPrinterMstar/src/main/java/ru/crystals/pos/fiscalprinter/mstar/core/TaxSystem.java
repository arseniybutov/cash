package ru.crystals.pos.fiscalprinter.mstar.core;

import java.util.Arrays;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

public enum TaxSystem {
    COMMON(1),
    SIMPLIFIED_INCOME(2),
    SIMPLIFIED_INCOME_MINUS_EXPENSE(4),
    UNIFIED(8),
    UNIFIED_AGRICULTURAL(16),
    PATENT(32);

    public final long flag;

    TaxSystem(int flag) {
        this.flag = flag;
    }

    public static TaxSystem getByFlag(long flag) throws FiscalPrinterException {
        return Arrays.stream(TaxSystem.values())
                .filter(ts -> ts.flag == flag)
                .findAny()
                .orElseThrow(() -> new FiscalPrinterException("Tax system was not found"));
    }
}
