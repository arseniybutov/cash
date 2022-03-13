package ru.crystals.pos.fiscalprinter.atol3;

public enum Registers {
    UNKNOWN(0),
    REGISTATION_SUMS(1),
    STORNO_SUMS(2),
    PAYMENT_SUMS(3),
    CASHIN_SUM(4),
    CASHOUT_SUM(5),

    REGISTATION_COUNT(6),
    STORNO_COUNT(7),
    CASHIN_COUNT(8),
    CASHOUT_COUNT(9),

    CASH_AMOUNT(10),
    RECEIPTS(11),
    SHIFT_TOTAL(12),

    //  Количество чеков (закрытых/отмененых)
    PURCHASE_COUNT(65);

    /**
     * Номер регистра
     */
    private final int registerNumber;

    Registers(int registerNumber) {
        this.registerNumber = registerNumber;
    }

    public int getRegisterNumber() {
        return registerNumber;
    }
}
