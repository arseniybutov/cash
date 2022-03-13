package ru.crystals.pos.fiscalprinter.atol.universal.connector;

/**
 * Системы налогообложения с флагами для Атол
 */
enum TaxMode {

    /**
     * По умолчанию
     */
    DEFAULT(0),
    /**
     * Общая
     */
    OSN(1),
    /**
     * Упрощенная доход
     */
    USN_INCOME(2),
    /**
     * Упрощенная доход минус расход
     */
    USN_INCOME_OUTCOME(4),
    /**
     * Единый налог на вмененный доход
     */
    ENVD(8),
    /**
     * Единый сельскохозяйственный налог
     */
    ESN(16),
    /**
     * Патентная система налогообложения
     */
    PATENT(32);

    private final int mask;

    TaxMode(int mask) {
        this.mask = mask;
    }

    /**
     * По значению тега 1062 Системы налогообложения, полученном из ФР, берем первую подходящую СНО.
     * Предполагается, что в большинстве случаев в ФР заведена одна система налогообложения,
     * но если их несколько, то возьмем первую.
     * @param frTaxMode значение тега 1062 Системы налогообложения из ФР
     */
    public static TaxMode getFirstByMask(long frTaxMode) {
        for (TaxMode taxMode : TaxMode.values()) {
            if ((frTaxMode & taxMode.getMask()) > 0) {
                return taxMode;
            }
        }
        return DEFAULT;
    }

    public int getMask() {
        return mask;
    }
}
