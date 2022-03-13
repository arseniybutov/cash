package ru.crystals.loyal.providers.set.filters;

import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.discounts.enums.ActionPluginAttributes;
import ru.crystals.discounts.interfaces.ActionPluginAttributable;

import java.util.stream.Stream;

/**
 * Данный фильтр "признает" только те РА, что содержат плагин результата с указанным атрибутом
 */
public class PluginAttributesSetActionsFilter implements SetActionsFilter {

    private ActionPluginAttributes attribute;

    @Override
    public boolean apply(AdvertisingActionEntity action) {
        try {
            return action != null && attribute != null && action.getDeserializedPlugins().stream()
                    .filter(ActionPluginAttributable.class::isInstance).map(ActionPluginAttributable.class::cast)
                    .flatMap(p -> Stream.of(p.getPluginAttributes())).anyMatch(attribute::equals);
        } catch (Exception e) {
            return false;
        }
    }

    public ActionPluginAttributes getAttribute() {
        return attribute;
    }

    public void setAttribute(ActionPluginAttributes attribute) {
        this.attribute = attribute;
    }

    @Override
    public String toString() {
        return "sales-tax-discount-set-actions-filter";
    }
}
