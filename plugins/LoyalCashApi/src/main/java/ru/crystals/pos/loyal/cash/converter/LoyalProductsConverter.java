package ru.crystals.pos.loyal.cash.converter;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.loyal.product.LoyalCountryEntity;
import ru.crystals.loyal.product.LoyalDepartEntity;
import ru.crystals.loyal.product.LoyalGroupEntity;
import ru.crystals.loyal.product.LoyalManufacturerEntity;
import ru.crystals.loyal.product.LoyalPriceEntity;
import ru.crystals.loyal.product.LoyalProductEntity;
import ru.crystals.loyal.product.LoyalProductType;
import ru.crystals.pos.catalog.CatalogService;
import ru.crystals.pos.catalog.PriceEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Класс для получения товаров лояльности на основании кассового чека
 *
 * @author ppavlov
 */
public class LoyalProductsConverter {
    /**
     * Признак, использовать ли кэширование товаров при повторном расчете скидок для чека с идентичным набором товаров Может может быть полезно при
     */
    public static boolean useCache = false;
    private static final Logger LOG = LoggerFactory.getLogger(LoyalProductsConverter.class);
    private static Map<String, LoyalProductEntity> loyalProducts = new HashMap<>();

    public static boolean isUseCache() {
        return useCache;
    }

    public static void setUseCache(boolean useCache) {
        LoyalProductsConverter.useCache = useCache;
    }

    /**
     * Проверка, не были ли товара из заданного списка только что найдены. Может быть полезно при печати отчета по скидкам сразу после чека.
     *
     * @param items список идентификаторов товаров для поиска
     * @return true, если все товары из списка были найдены предыдущим вызовом метода findLoyalProductsForPurchase, false в противном случае
     */
    private static boolean isAlreadyBeenFound(List<String> items) {
        if (!isUseCache()) {
            return false;
        }

        for (String item : items) {
            if (loyalProducts.get(item) == null) {
                return false;
            }
        }

        return true;
    }

    /**
     * Строит отображение &lt;код товара, товар> на основании найденного в базе списка, сохраняет его в переменной {@link
     * LoyalProductsConverter#loyalProducts}. Перед началом добавления отображение {@link LoyalProductsConverter#loyalProducts} очищается.
     *
     * @param productsList список найденных в базе товаров
     */
    private static void parseLoyalProducts(List<?> productsList) {
        loyalProducts.clear();
        if (productsList != null && !productsList.isEmpty()) {
            for (Object object : productsList) {
                if (object instanceof LoyalProductEntity) {
                    loyalProducts.put(((LoyalProductEntity) object).getMarkingOfTheGood(), (LoyalProductEntity) object);
                }
            }
        }
    }

    /**
     * Возвращает заглушку товара лояльности на основании его идентификатора
     *
     * @param item идентификатор товара лояльности
     * @return заглушка товара лояльности
     */
    private static LoyalProductEntity getFakeLoyalProduct(String item) {
        LoyalProductEntity result = new LoyalProductEntity();
        // пока временное решение; для того, чтобы в loy_discount_position сохранялся код товара
        result.setMarkingOfTheGood(item);
        result.setName("");

        // и тип товара
        LoyalProductType productType = new LoyalProductType();
        productType.setDiscountApplicable(true);
        result.setProductType(productType);

        return result;
    }

    private static LoyalProductEntity createLoyalProduct(PositionEntity pos) {
        LoyalProductEntity result = getFakeLoyalProduct(pos.getItem());
        if (pos.getProduct() != null) {
            result.setName(pos.getProduct().getName());
            result.setCountry(new LoyalCountryEntity(pos.getProduct().getCountryCode(), pos.getProduct().getCountryName()));
            result.setManufacturer(new LoyalManufacturerEntity(pos.getProduct().getManufacturerCode(), null));
            result.setGroup(new LoyalGroupEntity(pos.getProduct().getGroupCode()));
            result.setVat(pos.getProduct().getNds() != null ? new BigDecimal(pos.getProduct().getNds()) : null);
            result.setSalesTax(pos.getProduct().getSalesTax());
            result.setPluginFullClassName(pos.getProduct().getDiscriminator());
            for (PriceEntity price : pos.getProduct().getPrices()) {
                LoyalPriceEntity loyPrice = new LoyalPriceEntity();
                loyPrice.setPrice(price.getPrice());
                loyPrice.setNumber(price.getNumber() != null ? price.getNumber().longValue() : null);
                loyPrice.setDepartment(new LoyalDepartEntity(price.getDepartNumber(), null));
                loyPrice.setBeginDate(price.getBeginDate());
                loyPrice.setEndDate(price.getEndDate());
                loyPrice.setPriceType(price.getPriceType());
                loyPrice.setProduct(result);
                result.getPrices().add(loyPrice);
            }
        }
        return result;
    }

    /**
     * Ищет товары лояльности для заданного чека
     *
     * @param cashPurchase чек, для которого нужно найти товары
     */
    public static void findLoyalProductsForPurchase(PurchaseEntity cashPurchase, CatalogService catalogService) {
        if (cashPurchase == null) {
            return;
        }
        Map<String, PositionEntity> productsMap = cashPurchase.getPositions().stream()
                .filter(pos -> StringUtils.isNotEmpty(pos.getItem()))
                .collect(Collectors.toMap(PositionEntity::getItem, Function.identity(), (p1, p2) -> p2));
        List<String> productItems = new ArrayList<>(productsMap.keySet());
        if (!isAlreadyBeenFound(productItems)) {
            try {
                long startTime = System.currentTimeMillis();
                List<?> productsList = catalogService.getLoyalGoodsBatchByItems(productItems);
                parseLoyalProducts(productsList);
                productItems.removeAll(loyalProducts.keySet());
                productItems.forEach(item -> loyalProducts.put(item, createLoyalProduct(productsMap.get(item))));
                LOG.info("loyal goods searching: items size = {}, time = {} ms", loyalProducts.size(), System.currentTimeMillis() - startTime);
            } catch (Exception e) {
                LOG.error("There are an error during searching loyal products.", e);
            }
        }
    }

    /**
     * Возвращает товар лояльности из ранее найденых для чека методом findLoyalProductsForPurchase
     *
     * @param item идентификатор товара для поиска
     * @return товар лояльности
     */
    public static LoyalProductEntity getLoyalProductByItem(String item) {
        return getLoyalProductByItem(item, true);
    }

    /**
     * Возвращает товар лояльности из ранее найденых для чека методом findLoyalProductsForPurchase
     *
     * @param item           идентификатор товара для поиска
     * @param useFakeOnFault возвращать ли заглушку товара, если он не был найден в базе
     * @return товар лояльности, null если он не был найден и useFakeOnFault == false
     */
    public static LoyalProductEntity getLoyalProductByItem(String item, boolean useFakeOnFault) {
        LoyalProductEntity result = loyalProducts.get(item);
        if (result == null && useFakeOnFault) {
            result = getFakeLoyalProduct(item);
        }
        return result;
    }
}
