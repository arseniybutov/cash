package ru.crystals.pos.fiscalprinter.mstar.core.connect;

import java.util.Arrays;
import java.util.Collection;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.mstar.core.ResBundleFiscalPrinterMstar;

/**
 * Список налогов в ФР.
 * Независимо от значений таблицы список налогов по факту не меняется,
 * поэтому будем сетить в таблицу в том же порядке.
 * Таблица потом понадобится для получения списка налогов,
 * и учитывая что ФР в альфа-версии, могут это еще исправить.
 */
public enum Taxes {
    TAX_18(18f),
    TAX_10(10f),
    TAX_18_118(-18f),
    TAX_10_110(-10f),
    TAX_0(0f),
    TAX_NONDS(-1f);

    private static final float EPSILON = 0.00001f;
    private float internalValue;

    Taxes(float internalValue) {
        this.internalValue = internalValue;
    }

    public static Taxes findByInternalValue(float internalValue) throws FiscalPrinterException {
        return Arrays.stream(Taxes.values())
                .filter(value -> Math.abs(value.internalValue - internalValue) < EPSILON)
                .findAny().orElseThrow(() -> new FiscalPrinterException(ResBundleFiscalPrinterMstar.getString("UNSUPPORTED_TAXES")));
    }
    
    public static Taxes findByInternalValue(Collection <Float> internalValue) throws FiscalPrinterException {
        return Arrays.stream(Taxes.values())
                .filter(value -> internalValue.contains(value.internalValue))
                .findAny().orElseThrow(() -> new FiscalPrinterException(ResBundleFiscalPrinterMstar.getString("UNSUPPORTED_TAXES")));
    }
}
