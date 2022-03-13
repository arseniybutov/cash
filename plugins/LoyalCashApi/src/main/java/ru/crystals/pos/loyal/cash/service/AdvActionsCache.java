package ru.crystals.pos.loyal.cash.service;

import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.discounts.ApplyMode;
import ru.crystals.loyal.model.CommonActionsCache;
import ru.crystals.pos.check.ManualAdvertisingActionEntity;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Реализации этого интерфейса хранят кэш РА (Рекламных Акций) - уже вытащенных из БД акций и поддерживают этот кэш в актуальном состоянии: при
 * приеме/импорте новой РА этот кэш обновляется - см. методы {@link #update(AdvertisingActionEntity)} и {@link #remove(AdvertisingActionEntity)}.
 * <p/>
 * note: акции хранятся: guid -> акция
 * 
 * @author Anton Martynov &lt;amartynov@crystals.ru&gt;
 * @author aperevozchikov
 */
public interface AdvActionsCache extends CommonActionsCache {

    /**
     * Возвращает список всех активных акций
     * 
     * @param date
     *            дата, на момент которой рекламные акции активны
     * @param manualActionGuids
     *            идентификаторы ручных рекламных акций - нужны чтобы акции участвовали в расчете только если были применены в чеке
     * @return не {@code null} - Список активных акций
     */
    List<AdvertisingActionEntity> getActiveActions(Date date, Set<Long> manualActionGuids);

    /**
     * <em>[Следует вызывать при импорте новой РА]</em>: обновляет указанную РА в кэше: ищет в кэше {@link AdvertisingActionEntity РА} с таким же
     * {@link AdvertisingActionEntity#getGuid() GUID'ом} как и у аргумента этого метода и заменяет ее значением аргумента этого метода; если в кэше РА
     * с таким GUID'ом не оказалось, то указанная РА просто добавлчется в кэш.
     * <p/>
     * Implementation Note: предполагается, что этот метод будет вызываться при импорте РА с {@link AdvertisingActionEntity#getActive() флагом
     * активности} == <code>true</code>.
     * 
     * @param action
     *            РА, что надо обновить в кэше (или добавить в кэш - если ее там еще не было)
     */
    void update(AdvertisingActionEntity action);

    /**
     * <em>[Следует вызывать при импорте новой РА]</em>: Удаляет указанную РА из кэша ({@link AdvertisingActionEntity РА} с таким же
     * {@link AdvertisingActionEntity#getGuid() GUID'ом} как и у аргумента этого метода).
     * <p/>
     * Implementation Note: предполагается, что этот метод будет вызываться при импорте РА с {@link AdvertisingActionEntity#getActive() флагом
     * активности} == <code>false</code>.
     * 
     * @param action
     *            РА, что надо удалить из кэша
     */
    void remove(AdvertisingActionEntity action);

    /**
     * Вернет коллекцию активных ручных ({@link AdvertisingActionEntity#getMode()} == {@link ApplyMode#MANUAL}) действующих в данный момент РА из
     * кэша.
     * 
     * @return никогда не вернет <code>null</code> - в крайнем случае вернет пустую коллекцию
     */
    List<ManualAdvertisingActionEntity> getManualActions();

}
