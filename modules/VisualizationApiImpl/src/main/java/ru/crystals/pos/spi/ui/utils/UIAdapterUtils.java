package ru.crystals.pos.spi.ui.utils;

import java.math.BigDecimal;
import ru.crystals.pos.spi.ui.payment.SumToPayFormParameters;

public class UIAdapterUtils {

    public static void correctSumToPayParameters(SumToPayFormParameters parameters, BigDecimal maxSum) {
        // check min
        if (isWrongSum(parameters.getMinSum(), maxSum)) {
            parameters.setMinSum(BigDecimal.ZERO);
        }
        // check max
        if (isWrongSum(parameters.getMaxSum(), maxSum)) {
            parameters.setMaxSum(maxSum);
        }
        // check default
        if (isWrongSum(parameters.getDefaultSum(), maxSum)) {
            parameters.setDefaultSum(maxSum);
        }
    }

    private static boolean isWrongSum(BigDecimal sum, BigDecimal max) {
        return sum == null || !isBetween(sum, BigDecimal.ZERO, max);
    }

    public static boolean isBetween(BigDecimal value, BigDecimal min, BigDecimal max) {
        return (value.compareTo(min) >= 0 && value.compareTo(max) <= 0);
    }

}
