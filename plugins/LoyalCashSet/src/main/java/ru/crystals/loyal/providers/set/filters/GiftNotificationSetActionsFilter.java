package ru.crystals.loyal.providers.set.filters;

import java.util.Set;

import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.discounts.interfaces.IActionPlugin;
import ru.crystals.loyal.interfaces.IActionResultPlugin;

/**
 * Данный фильтр "признает" только РА, второстепенным результатом применения которых является оповещение о подарках ("смурфики":
 * "вы получили X наклеек").
 * 
 * @author aperevozchikov
 */
public class GiftNotificationSetActionsFilter implements SetActionsFilter {

    @Override
    public boolean apply(AdvertisingActionEntity action) {
        boolean result = true;

        if (action == null) {
            return false;
        }

        try {
            Set<IActionPlugin> plugins = action.getDeserializedPlugins();
            for (IActionPlugin p : plugins) {
                if (p instanceof IActionResultPlugin && ((IActionResultPlugin) p).isAdvancedMessageResult()) {
                    // да, этот плагин дает результат типа "оповещение о подарках"
                    result = true;
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
        return String.format("gift-notification-set-actions-filter");
    }

}
