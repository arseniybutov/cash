package ru.crystals.pos.fiscalprinter.mstar.core.connect;

import java.util.Arrays;

/**
 * Признаки соответствия типов платежей(для установки таблицы настроек, N32, 33)
 */
public enum PaymentTableAttribute {
    /**
     * 0 – тип платежа не задан;
     */
    NOT_SPECIFIED(0),
    /**
     * 1 – наличные
     */
    CASH(1, 0L),
    /**
     * 2 – электронные;
     */
    ELECTRON(2, 1L),
    /**
     * 3 – аванс
     */
    PREPAYMENT(3, 13L),
    /**
     * 4 – кредит
     */
    CREDIT(4, 14L),
    /**
     * 5 – иная форма.
     */
    OTHE_PAYMENT(5, 15L);

    /**
     * Представление в таблице ФР
     */
    private int tableValue;
    /**
     * Представление в соотвествии с законодательством и нашей реализацией
     */
    private Long standardValue = null;

    PaymentTableAttribute(int tableValue, Long standardValue) {
        this.tableValue = tableValue;
        this.standardValue = standardValue;
    }


    PaymentTableAttribute(int tableValue) {
        this.tableValue = tableValue;
    }

    public static int getTableByStandard(long standardValue) {
        return Arrays.stream(PaymentTableAttribute.values())
                .filter(value -> value.getStandardValue() != null)
                .filter(value -> value.getStandardValue() == standardValue)
                .findAny().orElse(NOT_SPECIFIED).getTableValue();
    }

    public int getTableValue() {
        return tableValue;
    }

    public Long getStandardValue() {
        return standardValue;
    }
}
