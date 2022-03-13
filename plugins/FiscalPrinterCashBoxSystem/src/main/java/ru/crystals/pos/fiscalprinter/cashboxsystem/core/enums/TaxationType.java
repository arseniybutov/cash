package ru.crystals.pos.fiscalprinter.cashboxsystem.core.enums;

/**
 * Список типов налогообложения задаваемого в CBS
 */
public enum TaxationType {

    /**
     * УСН
     */
    TAXATION_STS(100),
    /**
     * ОСН
     */
    TAXATION_RTS(101),
    /**
     * Налоговый режим для крестьянских и фермерских хозяйств
     */
    TAXATION_TRFF(102),
    /**
     * Налоговый режим для малых предприятий на основе патента
     */
    TAXATION_TRBP(103);

    private final int code;

    TaxationType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
