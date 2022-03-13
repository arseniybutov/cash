package ru.crystals.loyal.providers.set.filters;

import java.util.Set;

import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.discounts.interfaces.IActionPlugin;
import ru.crystals.loyal.interfaces.IActionResultPlugin;

/**
 * Данный фильтр "признает" только РА, второстепенным результатом применения которых является начисление бонусов [SET10].щ
 * 
 * @author aperevozchikov
 */
@SuppressWarnings("unused") // Использется через рефлексомагию
public class BonusAccrueSetActionsFilter implements SetActionsFilter {

    @Override
    public boolean apply(AdvertisingActionEntity action) {
        if (action == null) {
            return false;
        }
        try {
            Set<IActionPlugin> plugins = action.getDeserializedPlugins();
            for (IActionPlugin p : plugins) {
                if (p instanceof IActionResultPlugin && ((IActionResultPlugin) p).isBonusAccrueResult()) {
                    // да, этот плагин дает результат типа "начисление бонусов"
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
        return "bonus-accrue-set-actions-filter";
    }
    
}
