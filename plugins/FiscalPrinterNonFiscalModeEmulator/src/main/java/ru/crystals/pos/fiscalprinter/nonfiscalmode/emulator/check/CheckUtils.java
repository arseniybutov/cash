package ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.check;

import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class CheckUtils {

    /**
     * Проверяет чек на соответствие ограничениям ФЗ-54.
     *
     * @param check чек
     * @return нарушения ФЗ-54
     */
    public static Set<String> validateCheck(Check check) {
        Set<String> violations = new HashSet<>();
        long actualSum = 0;
        for (Goods good : check.getGoods()) {
            long pos = good.getPositionNum();
            validate(violations::add, good.getEndPricePerUnit() >= 0, "цена за единицу в позиции %d ниже нуля", pos);
            validate(violations::add, good.getEndPositionPrice() >= 0, "цена за позицию %d ниже нуля", pos);
            validate(violations::add,
                    Math.abs(CurrencyUtil.getPositionSum(good.getEndPricePerUnit(), good.getQuant()) - good.getEndPositionPrice()) <= 1,
                    "в позиции %d: ЦЕНА * КОЛИЧЕСТВО != СТОИМОСТЬ", pos);
            actualSum += good.getEndPositionPrice();
        }
        validate(violations::add, actualSum == check.getCheckSumEnd(), "сумма позиций не совпадает с суммой чека");
        return violations;
    }

    private static void validate(Consumer<String> consumer, boolean assertionResult, String expectedViolation, Object... args) {
        if (!assertionResult) {
            consumer.accept(String.format(expectedViolation, args));
        }
    }

}
