package ru.crystals.loyal.providers.set.filters;

import ru.crystals.discounts.AdvertisingActionEntity;

/**
 * Данный фильтр просто инвертирует результат своего {@link #getHostFilter() host-фильтра}.
 * 
 * @author aperevozchikov
 */
public class SetActionsNotFilter implements SetActionsFilter {

    /**
     * Фильтр. рузультат которого надо инвертировать
     */
    private SetActionsFilter hostFilter;

    @Override
    public boolean apply(AdvertisingActionEntity action) {
        if (getHostFilter() == null) {
            return false;
        }
        return !getHostFilter().apply(action);
    }

    @Override
    public String toString() {
        return String.format("set-actions-NOT-filter [host: %s]", getHostFilter());
    }
    
    public SetActionsFilter getHostFilter() {
        return hostFilter;
    }

    public void setHostFilter(SetActionsFilter hostFilter) {
        this.hostFilter = hostFilter;
    }

}
