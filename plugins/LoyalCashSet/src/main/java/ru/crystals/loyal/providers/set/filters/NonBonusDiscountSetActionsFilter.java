package ru.crystals.loyal.providers.set.filters;

import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.discounts.interfaces.IActionPlugin;
import ru.crystals.loyal.interfaces.IApplyBonusObjectPlugin;

import java.util.Set;

/**
 * Данный фильтр "признает"/отфильтровывает те РА, что не дают первичный результат типа "бонусы как скидка".
 * Используется через рефлексомагию.
 */
public class NonBonusDiscountSetActionsFilter implements SetActionsFilter {

    @Override
    public boolean apply(AdvertisingActionEntity action) {
        boolean result = true;

        if (action == null) {
            return false;
        }

        Set<IActionPlugin> plugins = action.getDeserializedPlugins();
        for (IActionPlugin p : plugins) {
            if (p instanceof IApplyBonusObjectPlugin) {
                // это скидка типа "бонусы как скидка"
                result = false;
                break;
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return "non-bonus-discount-set-actions-filter";
    }
}
