package ru.crystals.pos.loyal.cash.service;

/**
 * Created by f.malyshko on 29.03.2017.
 */
public interface LoyalPropertiesUpdater {

    /**
     * Обновить настройки возврата начисленных бонусов
     *
     * @param status
     */
    void updateReturnAccruedBonuses(boolean status);


    /**
     * Обновить настройки возврата списанных бонусов
     *
     * @param status
     */
    void updateReturnChargedOffBonuses(boolean status);

    /**
     * Обновить настройки оповещения кассира о бесплатных подарках
     *
     * @param status
     */
    void updateLossOfProfitNotification(boolean status);

    /**
     * Обновить настройки расчета скидок при смешанной оплате
     *
     * @param status
     */
    void updateCalculateOnPayments(boolean status);
}
