package ru.crystals.pos.fiscalprinter.cashboxsystem.core.enums;

/**
 * Список типов налога в CBS
 */
public enum TaxType {

    /**
     * НДС
     */
    TAX_VAT(100),
    /**
     * Без НДС. В этом случае режим налогооблажения игнорируется
     */
    TAX_WITHOUT_VAT(0);

    private final int code;

    TaxType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
