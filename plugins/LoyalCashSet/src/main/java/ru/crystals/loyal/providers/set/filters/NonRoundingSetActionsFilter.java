package ru.crystals.loyal.providers.set.filters;

import java.util.Set;

import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.discounts.interfaces.IActionPlugin;
import ru.crystals.loyal.check.discount.DiscountType;
import ru.crystals.loyal.interfaces.IApplyObjectPlugin;

/**
 * Данный фильтр "признает" только те РА, что не дают скидку на округление.
 * 
 * @author aperevozchikov
 */
public class NonRoundingSetActionsFilter implements SetActionsFilter {

    @Override
    public boolean apply(AdvertisingActionEntity action) {
        boolean result = true;

        if (action == null) {
            return false;
        }

        try {
            Set<IActionPlugin> plugins = action.getDeserializedPlugins();
            for (IActionPlugin p : plugins) {
                if (p instanceof IApplyObjectPlugin && DiscountType.ROUND.equals(((IApplyObjectPlugin) p).getDiscountType())) {
                    // это скидка на округление
                    result = false;
                    break;
                }
            }
        } catch (Throwable t) {
            // РА невалидна
            return false;
        }

        return result;
    }

    @Override
    public String toString() {
        return String.format("non-rounding-set-actions-filter");
    }

}
