package ru.crystals.pos.loyal.cash.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.loyal.product.LoyalBaseRestrictionEntity;
import ru.crystals.loyal.product.LoyalMinPriceRestrictionEntity;
import ru.crystals.loyal.product.LoyalProductEntity;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.catalog.ProductType;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.techprocess.TechProcessEvents;
import ru.crystals.pos.techprocess.TechProcessServiceAsync;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Класс для обработки ограничений минимальной цены в конфигурационных файлах алкогольного товара
 *
 * @author ppavlov
 */
public class SpiritsRestrictionsHandler {
    private static final Logger LOG = LoggerFactory.getLogger(SpiritsRestrictionsHandler.class);
    static double PRICE_PRECISION = 100;
    static Map<Double, String[]> spiritsRestricitions = null;

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Map<Double, String[]> getSpiritsRestrictions(PositionEntity cashPosition) {
        final String getRestricitionMethod = "getRestrictions";
        if (spiritsRestricitions == null) {
            ProductType productType = cashPosition.getProductSettings();
            if (productType != null && productType.getConfigurationClass() != null && !productType.getConfigurationClass().isEmpty()) {
                try {
                    Class clazz = Class.forName(productType.getConfigurationClass());
                    java.lang.reflect.Method method = clazz.getDeclaredMethod(getRestricitionMethod);
                    Object o = method.invoke(null);
                    if ((o != null) && (o instanceof Map<?, ?>)) {
                        spiritsRestricitions = (Map<Double, String[]>) o;
                    }
                } catch (Exception ex) {
                    LOG.error("Error reading spirits restricitions from xml: " + ex.getMessage());
                }
            }
        }
        return spiritsRestricitions;
    }

    /**
     * Возвращает плагинные свойства процентного содержания спирта и объема
     * алкогольного товара
     *
     * @param cashPosition кассовая позиция
     * @param techProcess  интерфейс техпроцесса для поиска алкогольного товара
     * @return Map<String, Double> со значениями по ключам "alcoholicContent" и
     * "volume" не равными null, в случае отсутствия ошибок
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Map<String, Double> getSpiritsPluginProperties(PositionEntity cashPosition, TechProcessServiceAsync techProcess,
                                                                  TechProcessEvents techProcessEvents) {
        Map<String, Double> result = new HashMap<String, Double>();
        try {
            boolean beepWithScanner = techProcessEvents.getCashProperties().getBeepWithScanner();
            techProcessEvents.getCashProperties().setBeepWithScanner(false);
            ProductEntity cashProduct = techProcess.searchProduct(cashPosition.getItem());
            techProcessEvents.getCashProperties().setBeepWithScanner(beepWithScanner);

            if (cashProduct != null) {
                final String getAlcoholicContentMethod = "getAlcoholicContent";
                final String getVolumeMethod = "getVolume";
                try {
                    String className = cashPosition.getProductSettings().getProductTypeClass();
                    if (!className.contains(".")) {
                        className = "ru.crystals.pos.catalog." + className;
                    }
                    Class clazz = Class.forName(className);
                    java.lang.reflect.Method method = clazz.getDeclaredMethod(getAlcoholicContentMethod);
                    Object o = method.invoke(cashProduct);
                    if ((o != null) && (o instanceof Double)) {
                        result.put("alcoholicContent", (Double) o);
                    }
                    method = clazz.getDeclaredMethod(getVolumeMethod);
                    o = method.invoke(cashProduct);
                    if ((o != null) && (o instanceof Double)) {
                        result.put("volume", (Double) o);
                    }

                } catch (Exception ex) {
                    LOG.error("Error reading plugin properties alcoholicContent and volume from cash product with item  " + cashProduct.getItem() + ": "
                            + ex.getMessage());
                }
            }
        } catch (Exception e) {
            LOG.warn("Error during searching cash spirits product with item " + cashPosition.getItem(), e);
        }
        return result;
    }

    private static boolean isSpiritsPosition(PositionEntity cashPosition) {
        boolean result = false;
        final String spiritsAttribute = "Spirits";
        String productType = cashPosition.getProductType();
        if (productType != null) {
            if (productType.contains(spiritsAttribute)) {
                result = true;
            }
        }
        return result;
    }

    public static void addSpiritsMinPriceRestrictions(PositionEntity cashPosition, LoyalProductEntity loyalProductEntity,
                                                      TechProcessServiceAsync techProcess, TechProcessEvents techProcessEvents) {
        if (isSpiritsPosition(cashPosition)) {
            try {
                Map<Double, String[]> spiritsRestricitions = getSpiritsRestrictions(cashPosition);
                if (spiritsRestricitions != null && !spiritsRestricitions.isEmpty()) {
                    Map<String, Double> spiritsPluginProperties = getSpiritsPluginProperties(cashPosition, techProcess, techProcessEvents);
                    Double alcoholicContent = spiritsPluginProperties.get("alcoholicContent") != null ? spiritsPluginProperties.get("alcoholicContent") : 0D;
                    Double volume = spiritsPluginProperties.get("volume") != null ? spiritsPluginProperties.get("volume") : 0D;
                    if (alcoholicContent != 0 && volume != 0) {
                        double dif = Double.POSITIVE_INFINITY;
                        String[] interval = null;

                        for (Entry<Double, String[]> entry : spiritsRestricitions.entrySet()) {
                            if (entry.getKey() <= alcoholicContent) {
                                double difCur = alcoholicContent - entry.getKey();
                                if (difCur < dif) {
                                    dif = difCur;
                                    interval = entry.getValue();
                                }
                            }
                        }

                        if (interval != null) {
                            if (Double.valueOf(interval[1]) != null && Double.valueOf(interval[1]) != 0d) {
                                LoyalMinPriceRestrictionEntity minPriceRestriction = new LoyalMinPriceRestrictionEntity();

                                minPriceRestriction.setCode("spirits-restriction");
                                minPriceRestriction.setAllDaysMask();
                                minPriceRestriction.setDeleted(false);
                                minPriceRestriction.setGroup(null);
                                minPriceRestriction.setLastImportTime(new Date());
                                minPriceRestriction.setProduct(loyalProductEntity);
                                minPriceRestriction.setSinceDate(LoyalBaseRestrictionEntity.getNegativeInfinityDate());
                                minPriceRestriction.setTillDate(LoyalBaseRestrictionEntity.getPositiveInfinityDate());
                                minPriceRestriction.setSinceTime(LoyalBaseRestrictionEntity.getNegativeInfinityDate());
                                minPriceRestriction.setTillTime(LoyalBaseRestrictionEntity.getPositiveInfinityDate());
                                minPriceRestriction.setValue(Math.round(Double.valueOf(interval[1]) * volume * PRICE_PRECISION));

                                loyalProductEntity.getSaleRestrictions().add(minPriceRestriction);

                            }
                        }
                    }
                }
            } catch (Exception ex) {
                LOG.error("Error during adding spirits xml minPrice restricitions to loyal calculator: " + ex.getMessage());
            }
        }
    }
}
