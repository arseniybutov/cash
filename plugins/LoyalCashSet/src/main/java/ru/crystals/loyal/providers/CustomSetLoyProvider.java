package ru.crystals.loyal.providers;

import java.util.ArrayList;
import java.util.Collection;

import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.loyal.interfaces.ILoyTechProcess;
import ru.crystals.loyal.providers.set.filters.SetActionsFilter;
import ru.crystals.pos.check.PurchaseEntity;

/**
 * "Настраиваемый" вариант нашего (SET10) поставщика услуг лояльности.
 * 
 * @author aperevozchikov
 *
 */
public class CustomSetLoyProvider extends ClassicSetLoyProvider {
    
    /**
     * Фильтр РА, что признаются данной версией поставщика услуг лояльности
     */
    private SetActionsFilter filter;
    
    
    @Override
    protected Collection<AdvertisingActionEntity> getFilteredActions(PurchaseEntity purchase, ILoyTechProcess techProcess) {
        Collection<AdvertisingActionEntity> result = new ArrayList<>(super.getFilteredActions(purchase, techProcess));
        
        SetActionsFilter f = getFilter();
        
        // нет фильтра == классическая реализация:
        if (f == null) {
            return result;
        }

        // эта РА не удовлетворяет фильтру
        // for it
        result.removeIf(advertisingActionEntity -> !f.apply(advertisingActionEntity));
        
        return result;
    }
    
    @Override
    public String toString() {
        return String.format("custom-set-loy-provider [filter: %s]", getFilter());
    }

    // getters & setters

    public SetActionsFilter getFilter() {
        return filter;
    }

    public void setFilter(SetActionsFilter filter) {
        this.filter = filter;
    }
}
