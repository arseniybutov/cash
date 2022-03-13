package ru.crystals.comportemulator.mstar;

public enum DocumentType {
    /**
     * Сервисный документ
     */
    SERVICE_DOCUMENT(1L),
    /**
     * Приход
     */
    SALE(2L),
    /**
     * Возврат прихода
     */
    RETURN_SALE(3L),
    /**
     * Внесение в кассу
     */
    CASH_IN(4L),
    /**
     * Изъятие
     */
    CASH_OUT(5L),
    /**
     * Расход
     */
    CONSUMPTION(6L),
    /**
     * Возврат расхода
     */
    RETURN_CONSUMPTION(7L),
    /**
     * Чек коррекции
     */
    CORRECTION_RECEIPT(8L);

    private long value;

    DocumentType(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }
}
