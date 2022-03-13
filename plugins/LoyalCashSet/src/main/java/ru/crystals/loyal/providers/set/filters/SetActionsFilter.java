package ru.crystals.loyal.providers.set.filters;

import ru.crystals.discounts.AdvertisingActionEntity;

/**
 * Фильтр наших (SET10) РА (Рекламных Акций).
 * <p/>
 * NOTE: можно было бы и Guava'овский Predicate использовать - но зачем без нужды добавлять зависимость от нестандартной библиотеки?
 * 
 * @author aperevozchikov
 * @since 10.2.23.0
 */
public interface SetActionsFilter {

    /**
     * Вернет {@code true}, если указанная РА удовлетворяет данному фильтру.
     * 
     * @param action
     *            РА, что проверяем на удовлетворение условиям фильтрации
     * @return {@code false}, если аргумент невалиден
     */
    boolean apply(AdvertisingActionEntity action);
}