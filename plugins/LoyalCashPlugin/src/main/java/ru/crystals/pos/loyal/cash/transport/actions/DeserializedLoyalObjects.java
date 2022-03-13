package ru.crystals.pos.loyal.cash.transport.actions;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.transport.TransferObject;

/**
 * Этот контейнер будет просто хранить импортированные объекты лояльности (десериализованные/расшифрованные объекты из {@link TransferObject
 * транспортных}). Чисто для удобства.
 * 
 * @author aperevozchikov
 */
public class DeserializedLoyalObjects implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Список РА, что была извлечена из {@link TransferObject транспортного сообщения}.
     */
    private List<AdvertisingActionEntity> actions;
    
    @Override
    public String toString() {
        return String.format("loyal-objects:\n\tactions: %s", getActions());
    }
    
    // getters & setters:
    
    public List<AdvertisingActionEntity> getActions() {
        if (actions == null) {
            actions = new LinkedList<>();
        }
        return actions;
    }

    public void setActions(List<AdvertisingActionEntity> actions) {
        this.actions = actions;
    }
}
