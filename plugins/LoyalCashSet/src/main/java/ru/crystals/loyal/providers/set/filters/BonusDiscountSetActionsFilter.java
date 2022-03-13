package ru.crystals.loyal.providers.set.filters;

import java.util.Set;

import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.discounts.interfaces.IActionPlugin;
import ru.crystals.loyal.interfaces.IApplyBonusObjectPlugin;

/**
 * Данный фильтр "признает"/отфильтровывает те РА, что дают первичный результат типа "бонусы как скидка".
 * 
 * @author aperevozchikov
 */
@SuppressWarnings("unused") // Используется через рефлексомагию.
public class BonusDiscountSetActionsFilter implements SetActionsFilter {

    @Override
    public boolean apply(AdvertisingActionEntity action) {
        if (action == null) {
            return false;
        }

        try {
            Set<IActionPlugin> plugins = action.getDeserializedPlugins();
            for (IActionPlugin p : plugins) {
                if (p instanceof IApplyBonusObjectPlugin) {
                    // это скидка типа "бонусы как скидка"
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
        return "bonus-discount-set-actions-filter";
    }
}
