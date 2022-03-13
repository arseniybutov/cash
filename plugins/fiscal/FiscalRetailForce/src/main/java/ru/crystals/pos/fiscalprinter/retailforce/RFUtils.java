package ru.crystals.pos.fiscalprinter.retailforce;

import ru.crystals.pos.check.BigDecimalConverter;

import java.math.BigDecimal;

public class RFUtils {


    public static BigDecimal asMoney(long value, boolean negative) {
        return BigDecimalConverter.convertMoney(negative ? -value : value);
    }

    public static BigDecimal asQuantity(long value, boolean negative) {
        return BigDecimalConverter.convertQuantity(negative ? -value : value);
    }

    /**
     * В API нумерация позиций начинается с нуля
     */
    public static int convertPositionNumber(long number) {
        return (int) (number - 1);
    }
}
