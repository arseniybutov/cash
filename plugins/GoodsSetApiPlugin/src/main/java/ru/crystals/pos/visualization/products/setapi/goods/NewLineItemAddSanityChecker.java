package ru.crystals.pos.visualization.products.setapi.goods;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.api.plugin.goods.NewLineItem;

/**
 * Проверялка корректности возвращаемого результата от плагина товаров Set API на запрос о создании позиции для добавления в чек.
 */
public class NewLineItemAddSanityChecker {
    private static final Logger logger = LoggerFactory.getLogger(NewLineItemAddSanityChecker.class);
    private static final String SANITY_CHECK_FAIL_PREFIX = "SANITY CHECK FAILED:";

    private NewLineItemAddSanityChecker() {
        // NOP
    }

    /**
     * Проверяет корректность результата от плагина товаров Set API на запрос о создании позиции для добавления в чек.
     * @param item результат, что прислал плагин
     * @return true если результат допустимо признать корректным и false в противном случае.
     */
    public static boolean isValid(NewLineItem item) {
        if(item == null) {
            logger.error("{} the result cannot be null", SANITY_CHECK_FAIL_PREFIX);
            return false;
        }
        if(item.getPrice() == null) {
            logger.error("{} the new item price cannot be null", SANITY_CHECK_FAIL_PREFIX);
            return false;
        }
        if(StringUtils.isBlank(item.getName())) {
            logger.error("{} the new item name cannot be null", SANITY_CHECK_FAIL_PREFIX);
            return false;
        }
        if(item.getPrice().longValue() < 0) {
            logger.error("{} item price cannot be negative (found {})", SANITY_CHECK_FAIL_PREFIX, item.getPrice());
            return false;
        }
        if(StringUtils.isBlank(item.getMarking())) {
            logger.error("{} item marking cannot be blank", SANITY_CHECK_FAIL_PREFIX);
            return false;
        }
        if(item.getQuantity() <= 0) {
            logger.error("{} item quantity cannot be negative or equal to zero ({})", SANITY_CHECK_FAIL_PREFIX, item.getQuantity());
            return false;
        }
        return true;
    }
}
