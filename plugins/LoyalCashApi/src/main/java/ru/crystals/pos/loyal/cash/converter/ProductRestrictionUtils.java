package ru.crystals.pos.loyal.cash.converter;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.loyal.calculation.RestrictionUtils;
import ru.crystals.loyal.model.LoyMetrics;
import ru.crystals.loyal.product.LoyalBaseRestrictionEntity;
import ru.crystals.loyal.product.LoyalMaxDiscountPercentRestrictionEntity;
import ru.crystals.loyal.product.LoyalMinPriceRestrictionEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PositionSpiritsEntity;
import ru.crystals.pos.currency.CurrencyUtil;

/**
 * Логика вычисления ограничений по позициям, по аналогии с {@link RestrictionUtils}, но для позиций кассвого чека.
 */
public class ProductRestrictionUtils {

    private static final Logger log = LoggerFactory.getLogger(ProductRestrictionUtils.class);


    /**
     * Для заданого товара получает минимальную цену.
     *
     * @param position позиция, минимальную цену которой необходимо выяснить
     * @return
     */
    public static long getMinPrice(PositionEntity position) {
        return getMinPrice(position, position.getDateTime());
    }

    /**
     * Для заданого товара получает минимальную цену.
     *
     * @param position позиция, минимальную цену которой необходимо выяснить
     * @param now дата для получения актуальных ограничений
     * @return
     */
    public static long getMinPrice(PositionEntity position, Date now) {
        Set<LoyalBaseRestrictionEntity> restrictions =
                LoyalProductsConverter.getLoyalProductByItem(position.getItem()).getSaleRestrictions();
        return getMinPrice(position, now, restrictions);
    }


    private static long getMinPrice(PositionEntity position, Date now, Set<LoyalBaseRestrictionEntity> restrictions) {
        Function<Double, Long> fromMaxPercent = maxPercent -> (position.getPriceStart() -
                CurrencyUtil.roundDown(1.0 * position.getPriceStart() * maxPercent / LoyMetrics.PRICE_PRECISION));

        Function<Long, Long> fromMinPrice = Function.identity();

        return getRestrictionsAsValues(position, now, 0, restrictions, fromMaxPercent, fromMinPrice)
                .max(Long::compareTo)
                .orElse(position.getPriceStart());
    }

    /**
     * Обобщенная функция получения и преобразования ограничений по позиции.
     */
    private static Stream<Long> getRestrictionsAsValues(PositionEntity position,
                                                        Date now,
                                                        long initialValue,
                                                        Set<LoyalBaseRestrictionEntity> restrictions,
                                                        Function<Double, Long> fromMaxPercent,
                                                        Function<Long, Long> fromMinPrice) {

        if (position == null || now == null || restrictions == null) {
            log.error("Restriction calculation failed: invalid arguments!");
            return Stream.empty();
        }

        if (position.isFixedPrice() || position.isNoActions()) {
            log.warn("Discount apply is forbidden for {}.", position);
            return Stream.empty();
        }

        List<Long> values = new LinkedList<>(Collections.singletonList(initialValue));

        if (position instanceof PositionSpiritsEntity) {
            values.add(fromMinPrice.apply(((PositionSpiritsEntity) position).getAlcoMinPrice()));
        }

        restrictions.stream()
                .filter(restriction -> restriction.isActive(now))
                .map(baseRestrict -> {
                    if (baseRestrict instanceof LoyalMaxDiscountPercentRestrictionEntity) {
                        return fromMaxPercent.apply(((LoyalMaxDiscountPercentRestrictionEntity) baseRestrict).getValue());
                    } else if (baseRestrict instanceof LoyalMinPriceRestrictionEntity) {
                        return fromMinPrice.apply(((LoyalMinPriceRestrictionEntity) baseRestrict).getValue());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .forEach(values::add);

        return values.stream().filter(Objects::nonNull);
    }

}
