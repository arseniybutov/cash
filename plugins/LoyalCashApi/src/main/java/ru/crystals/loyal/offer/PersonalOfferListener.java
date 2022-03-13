package ru.crystals.loyal.offer;

import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.offer.entity.PersonalOffer;

import java.util.Collection;

/**
 * Created by agaydenger on 29.09.16.
 * Интерфейс слушателя, который обрабатывает полученные персональные предложения
 */
public interface PersonalOfferListener {
    /**
     * Событие получения персонального предложения
     * @param personalOffer персональное предложение
     * @param purchase Чек лояльности
     * @param receiptId Id кассового чека
     */
    void offersReceived(Collection<PersonalOffer> personalOffer, Purchase purchase, long receiptId);

    boolean isEnabled();

}
