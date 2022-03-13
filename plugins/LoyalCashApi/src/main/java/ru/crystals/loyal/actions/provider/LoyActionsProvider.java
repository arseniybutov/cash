package ru.crystals.loyal.actions.provider;

import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.pos.check.PurchaseEntity;

import java.util.Collection;
import java.util.Map;

/**
 * Средство для получения РА (Рекламных Акций), что могут действовать (применимы в теории) на определенный чек.
 *
 * @author aperevozchikov
 * @since 10.2.23.0
 */
public interface LoyActionsProvider {

    /**
     * Метод оповещения о начале расчета скидок.
     *
     * @param startPurchase   чек лояльности
     * @param purchaseExtData расширенные атрибуты чека
     */
    void onDiscountCalculationStarted(Purchase startPurchase, Map<String, String> purchaseExtData);

    /**
     * Метод оповещения о завершении работы с чеком.
     */
    void onPurchaseProcessingFinished();

    /**
     * Вернет коллекцию РА, что могут быть применены на указанный чек.
     *
     * @param receipt
     *            чек
     * @return не {@code null}; в крайнем случае вернет пустую коллекцию
     */
    Collection<AdvertisingActionEntity> getActions(PurchaseEntity receipt);

    /**
     * Вернет коллекцию РА, что могут быть применены на указанный чек.
     *
     * @param purchase
     *            чек калькулятора
     * @return не {@code null}; в крайнем случае вернет пустую коллекцию
     */
    Collection<AdvertisingActionEntity> getActions(Purchase purchase);

}
