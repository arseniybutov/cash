package ru.crystals.loyal.providers.set.filters;

import java.util.Collection;
import java.util.LinkedList;

import ru.crystals.discounts.AdvertisingActionEntity;

/**
 * Данный фильтр является контейнером для коллекции {@link #getFilters() других фильтров} и {@link #apply(AdvertisingActionEntity) результатом}
 * данного фильтра является конъюнкция (логическое И) результатов всех фильтров, входящих в коллекцию.
 * <p/>
 * NOTE: если {@link #getFilters() коллекция фильтров} ПУСТА, то {@link #apply(AdvertisingActionEntity) результатом} будет {@code true}: каждый фильтр
 * предполагает некое ОГРАНИЧЕНИЕ, накладываемое на РА - и если фильтров нет, то нет и ограничений: любая РА (если она валидна, конечно - см. javadoc
 * {@link #apply(AdvertisingActionEntity)}) подойдет.
 * 
 * @author aperevozchikov
 */
public class SetActionsAndFilter implements SetActionsFilter {

    /**
     * Фильтры, логическое И которых даст результат данного фильтра.
     */
    private Collection<SetActionsFilter> filters;

    @Override
    public boolean apply(AdvertisingActionEntity action) {
        boolean result = true;

        if (action == null) {
            // РА невалидна
            return false;
        }
        for (SetActionsFilter f : getFilters()) {
            if (f == null) {
                continue;
            }
            if (!f.apply(action)) {
                result = false;
                break;
            }
        } // for f

        return result;
    }

    @Override
    public String toString() {
        return String.format("set-actions-AND-filter [%s]", getFilters());
    }

    public Collection<SetActionsFilter> getFilters() {
        if (filters == null) {
            filters = new LinkedList<>();
        }
        return filters;
    }

    public void setFilters(Collection<SetActionsFilter> filters) {
        this.filters = filters;
    }
}
