package ru.crystals.loyal.providers.set.filters;

import java.util.Set;

import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.discounts.interfaces.IActionPlugin;
import ru.crystals.loyal.check.discount.DiscountType;
import ru.crystals.loyal.interfaces.IApplyObjectPlugin;

/**
 * Данный фильтр "признает" только те РА, что дают ТОЛЬКО скидку на округление.
 * 
 * @author aperevozchikov
 */
public class RoundingSetActionsFilter implements SetActionsFilter {

    @Override
    public boolean apply(AdvertisingActionEntity action) {
        boolean result = true;

        if (action == null) {
            return false;
        }

        try {
            Set<IActionPlugin> plugins = action.getDeserializedPlugins();
            boolean roundingFound = false;
            for (IActionPlugin p : plugins) {
                if (p instanceof IApplyObjectPlugin) {
                    IApplyObjectPlugin ia = (IApplyObjectPlugin) p;
                    
                    if (!DiscountType.ROUND.equals(ia.getDiscountType())) {
                        // это НЕ скидка на округление
                        result = false;
                        break;
                    }
                    
                    if (DiscountType.ROUND.equals(ia.getDiscountType())) {
                        // это скидка на округление
                        roundingFound = true;
                    }
                }
            } // for p
            if (!roundingFound) {
                result = false;
            }
        } catch (Throwable t) {
            // РА невалидна
            return false;
        }

        return result;
    }

    @Override
    public String toString() {
        return String.format("rounding-set-actions-filter");
    }
}
