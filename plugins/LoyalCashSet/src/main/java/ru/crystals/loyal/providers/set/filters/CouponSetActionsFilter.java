package ru.crystals.loyal.providers.set.filters;

import java.util.Set;

import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.discounts.interfaces.IActionPlugin;
import ru.crystals.loyal.interfaces.IActionResultPlugin;

/**
 * Данный фильтр "признает" только те РА, вторичным результатом работы которых является генерация/печать купонов.
 * 
 * @author aperevozchikov
 */
@SuppressWarnings("unused") // Используется через рефлексию.
public class CouponSetActionsFilter implements SetActionsFilter {

    @Override
    public boolean apply(AdvertisingActionEntity action) {
        if (action == null) {
            return false;
        }

        try {
            Set<IActionPlugin> plugins = action.getDeserializedPlugins();
            for (IActionPlugin p : plugins) {
                if (p instanceof IActionResultPlugin && ((IActionResultPlugin) p).isCouponResult()) {
                    // да, этот плагин дает результат типа "генерация/печать купона"
                    return true;
                }
            }
        } catch (Exception t) {
            // РА невалидна
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return String.format("coupon-set-actions-filter");
    }
}
