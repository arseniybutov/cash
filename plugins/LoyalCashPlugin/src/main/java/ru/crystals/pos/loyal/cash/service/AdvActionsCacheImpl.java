package ru.crystals.pos.loyal.cash.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.discounts.ApplyMode;
import ru.crystals.discounts.TimestampPeriodEntity;
import ru.crystals.pos.check.ManualAdvertisingActionEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * note: акции хранятся: guid -> акция
 *
 * @author Anton Martynov &lt;amartynov@crystals.ru&gt;
 * @author aperevozchikov
 */
public class AdvActionsCacheImpl implements AdvActionsCache {

    /**
     * The logger.
     */
    private static final Logger log = LoggerFactory.getLogger(AdvActionsCacheImpl.class);

    /**
     * Сам кэш РА - то ради чего весь этот класс и был создан.
     */
    private final Map<Long, AdvertisingActionEntity> cache = new ConcurrentHashMap<>();
    /**
     * Кеш РА, актуальных только для текущего чека (индивидуальных предложений)
     */
    private Map<Long, AdvertisingActionEntity> pluginsCache;
    /**
     * А через эту штуку будем добывать из БД инфу о существующих активных {@link AdvertisingActionEntity РА}.
     */
    private CashAdvertisingActionDao actionsDao;

    // injected
    /**
     * Чисто тюнинговая вещь: этот флаг указывает запускать ли заполнение кэша РА (при старте кассы) в отдельном потоке, либо в этом же.
     */
    private boolean threadStart = true;

    // injected
    /**
     * Чисто тюнинговая вещь: этот флаг указывает инициализировать ли кэш РА при старте кассы "легковесными" версиями РА (только GUID + период
     * действия) - для ускорения старта кассы, либо же сразу же полноценными (полная информация о РА).
     */
    private boolean initOnlyWithGuids = false;


    // life-cycle methods:

    /**
     * Инициализация: то, что в энтерпрайзе аннотируется как @PostConstruct, а в Spring'е указывается значением атрибута init-method:этот метод
     * следует вызывать до вызова любого бизнес-метода
     */
    public void postConstruct() {
        Runnable starter = () -> {
            log.trace("reading all active actions..");
            long t = System.currentTimeMillis();
            try {
                initActionsCache();
            } catch (Exception e) {
                log.error("Could not init actions cache", e);
            } finally {
                log.info("Time of init actions cache: {} ms", System.currentTimeMillis() - t);
            }
        };

        if (threadStart) {
            log.trace("starting AdvActionsCacheImpl initialization in a separate thread");
            Executors.newSingleThreadExecutor().execute(starter);
        } else {
            // видно, касса "медленная" - надо в этом же потоке кэш РА заполнить. и пусть остальные Spring'овые бины ждут
            log.trace("starting AdvActionsCacheImpl initialization in THIS (SAME) thread");
            starter.run();
        }
    }

    /**
     * Еще один life-cycle method: этот должен вызываться перед уничтожением этого объекта. В энтерпрайзе такие аннотируются как @PreDestroy, а в
     * Spring'овых конфигах - суются как значение атрибута destroy-method.
     */
    public void preDestroy() {
        // пока ничего не надо делать
    }

    // life-cycle methods^


    /**
     * {@inheritDoc}
     */
    @Override
    public List<AdvertisingActionEntity> getActiveActions(Date date, Set<Long> manualActionGuids) {
        List<AdvertisingActionEntity> result;

        long stopWatch = System.currentTimeMillis();
        synchronized (cache) {
            log.trace("getActiveActions: lock on cache was obtained in {} [ms]", System.currentTimeMillis() - stopWatch);
            result = getActiveActionsInner(date, manualActionGuids);
        }

        return result;
    }

    private List<AdvertisingActionEntity> getActiveActionsInner(Date date, Set<Long> manualActionGuids) {
        List<AdvertisingActionEntity> result;
        long stopWatch = System.currentTimeMillis();

        if (log.isTraceEnabled()) {
            log.trace("entering getActiveActions(Date, Set). The arguments are: date [{}], manualActionGuids: {}",
                    date == null ? "(NULL)" : date, manualActionGuids);
        }

        if (date == null) {
            date = new Date();
        }
        if (manualActionGuids == null) {
            manualActionGuids = Collections.emptySet();
        }
        result = new ArrayList<>(cache.size());
        Map<Long, AdvertisingActionEntity> allActionsMap = new HashMap<>();
        allActionsMap.putAll(cache);
        if (pluginsCache != null) {
            allActionsMap.putAll(pluginsCache);
        }
        // GUID'ы РА, что надо будет вытянуть из БД
        Set<Long> actionsToPull = new HashSet<>();
        for (Entry<Long, AdvertisingActionEntity> a : allActionsMap.entrySet()) {
            AdvertisingActionEntity action = a.getValue();
            if (action == null) {
                // здесь можно прочитать из БД и записать в кэш
                log.trace("Cache does not contain action[guid={}].", a.getKey());
                continue;
            }

            // проверим период
            if (!isTheDateWithinTheInterval(date, action.getWorkPeriod())) {
                // todo: кстати, если акция устарела - её можно удалить из кэша
                continue;
            }

            //если акция ручная и она не была применена в чеке, пропустить
            if (ApplyMode.MANUAL.equals(action.getMode()) && !manualActionGuids.contains(action.getGuid())) {
                continue;
            }

            // если флаг active акции null, значит, акция в кэше неполная
            if (action.getActive() == null) {
                // эта РА в кэше не полная. ее надо вытянуть и положить в результат
                actionsToPull.add(a.getKey());
            } else {
                // эта акция в кэше полная и годна чтоб поместить ее в результат:
                result.add(action);
            }
        } // for guid


        if (!actionsToPull.isEmpty()) {
            // не все активные РА были в кэше полными. Это нештатная ситуация:
            log.info("getActiveActions: about to pull [{}] active actions from the DB", actionsToPull.size());

            // 1. вытянем акции
            Map<Long, AdvertisingActionEntity> actions = getActionsDao().getActionsByGuids(actionsToPull, date);

            // 2. положим их в кэш
            Set<Long> invalidGuids = putActionsIntoCacheAndRemoveInvalidOnes(actions);

            // 3. валидные из вытянутых - в результат
            actions.keySet().removeAll(invalidGuids);
            result.addAll(actions.values());
        }

        log.trace("leaving getActiveActions(Date, Set). It took {} [ms]", System.currentTimeMillis() - stopWatch);
        return result;
    }

    private void initActionsCache() {
        long stopWatch = System.currentTimeMillis();
        synchronized (cache) {
            log.trace("initActionsCache: lock on cache was obtained in {} [ms]", System.currentTimeMillis() - stopWatch);

            log.trace("clearing cache..");
            cache.clear();
            log.trace("cache cleared");

            if (isInitOnlyWithGuids()) {
                initActionsCacheWithGuidsOnly();
            } else {
                initActionsCacheComplete();
            }
        }
    }

    private void initActionsCacheComplete() {
        long stopWatch = System.currentTimeMillis();

        log.trace("entering initActionsCacheComplete()");

        // 1. вытягиваем акции
        Map<Long, AdvertisingActionEntity> actions = getActionsDao().getActionsByGuids(null, new Date());

        // 2. и кладем их в кэш
        putActionsIntoCacheAndRemoveInvalidOnes(actions);

        log.trace("leaving initActionsCacheComplete(). it took {} [ms]", System.currentTimeMillis() - stopWatch);
    }

    private void initActionsCacheWithGuidsOnly() {
        long stopWatch = System.currentTimeMillis();

        log.trace("entering initActionsCacheWithGuidsOnly()");

        Map<Long, TimestampPeriodEntity> actionsLifespans = getActionsDao().getActionsLifeSpans(null, new Date());
        for (Long guid : actionsLifespans.keySet()) {
            TimestampPeriodEntity tsp = actionsLifespans.get(guid);

            AdvertisingActionEntity action = new AdvertisingActionEntity();
            action.setActive(null); // null значение будет являться признаком "неполной" акции в кэше
            action.setGuid(guid);
            action.setWorkPeriod(new TimestampPeriodEntity());
            action.getWorkPeriod().setFinish(tsp.getFinish());
            action.getWorkPeriod().setStart(tsp.getStart());

            // to cache
            cache.put(action.getGuid(), action);
        } // for guid

        log.trace("leaving initActionsCacheWithGuidsOnly(). it took {} [ms]", System.currentTimeMillis() - stopWatch);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(AdvertisingActionEntity action) {
        long stopWatch = System.currentTimeMillis();
        synchronized (cache) {
            log.trace("update: lock on cache was obtained in {} [ms]", System.currentTimeMillis() - stopWatch);
            updateInner(action);
        }
    }

    private void updateInner(AdvertisingActionEntity action) {
        if (action == null || action.getGuid() == null) {
            log.error("update(AdvertisingActionEntity): invalid argument [{}]!", action);
            return;
        }

        // для статистики узнаем, что было в кэше
        AdvertisingActionEntity old = cache.get(action.getGuid());

        // поместим новую версию РА в кэш
        Set<Long> invalidGuids = putActionsIntoCacheAndRemoveInvalidOnes(Collections.singletonMap(action.getGuid(), action));

        // и логгируем что получилось
        if (!invalidGuids.isEmpty()) {
            // прислали невалидную акцию!
            log.error("update: invalid action [{}] was passed as the argument! The old action [{}] if any was removed from cache", action, old);
        } else if (old != null) {
            // акцию в кэше обновляем
            log.trace("Action[guid={}] is updated in cache", action.getGuid());
        } else {
            // добавляем акцию в кэш
            log.trace("Action[guid={}] is inserted in cache", action.getGuid());
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(AdvertisingActionEntity action) {
        long stopWatch = System.currentTimeMillis();
        synchronized (cache) {
            log.trace("remove: lock on cache was obtained in {} [ms]", System.currentTimeMillis() - stopWatch);
            AdvertisingActionEntity existent = cache.remove(action.getGuid());
            if (existent != null) {
                log.trace("Action[guid={}] is removed from cache", action.getGuid());
            } else {
                log.trace("Action[guid={}] is not in cache; nothing to remove", action.getGuid());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ManualAdvertisingActionEntity> getManualActions() {
        List<ManualAdvertisingActionEntity> result;

        long stopWatch = System.currentTimeMillis();
        synchronized (cache) {
            log.trace("getManualActions: lock on cache was obtained in {} [ms]", System.currentTimeMillis() - stopWatch);
            result = getManualActionsInner();
        }

        return result;
    }

    private List<ManualAdvertisingActionEntity> getManualActionsInner() {
        List<ManualAdvertisingActionEntity> result = new ArrayList<ManualAdvertisingActionEntity>();
        long stopWatch = System.currentTimeMillis();

        log.trace("entering getManualActions()");

        // GUID'ы РА, что надо будет вытянуть из БД
        Set<Long> actionsToPull = new HashSet<Long>();
        Date now = new Date();
        for (AdvertisingActionEntity action : cache.values()) {
            // только ручные и активные
            if (action.getMode() == null || action.getMode() != ApplyMode.MANUAL) {
                continue;
            }

            // и дейтсвующие в настоящее время
            if (!isTheDateWithinTheInterval(now, action.getWorkPeriod())) {
                continue;
            }

            // если свойство action акции null, значит, акция в кэше неполная
            if (action.getActive() == null) {
                actionsToPull.add(action.getGuid());
            } else {
                // эта РА в кэше полная - сразу впихнем в результат
                result.add(new ManualAdvertisingActionEntity(action.getGuid(), action.getName()));
            }
        } // for action

        if (!actionsToPull.isEmpty()) {
            // не все ручные РА были в кэше полными. Это нештатная ситуация:
            log.info("getManualActions: about to pull [{}] manual actions from the DB", actionsToPull.size());

            // 1. вытянем акции
            Map<Long, AdvertisingActionEntity> actions = getActionsDao().getActionsByGuids(actionsToPull, now);

            // 2. положим их в кэш
            Set<Long> invalidGuids = putActionsIntoCacheAndRemoveInvalidOnes(actions);

            // 3. валидные из вытянутых - в результат
            actions.keySet().removeAll(invalidGuids);
            for (Long guid : actions.keySet()) {
                AdvertisingActionEntity action = actions.get(guid);
                result.add(new ManualAdvertisingActionEntity(guid, action.getName()));
            } // for guid
        }

        log.trace("leaving getManualActions(). The result size is: {}; it  took {} [ms]",
                result.size(), System.currentTimeMillis() - stopWatch);
        return result;
    }


    /**
     * Поместит {@link #validateAction(AdvertisingActionEntity) валидные} из указанных акций в {@link #cache кэш}, а также удалит из {@link #cache
     * кэша} те из указанных акций, что признаны невалидными.
     *
     * @param actions акции ,что собираемся добавить в кэш
     * @return никогда не вернет <code>null</code> - в кранейм случае вернет пустую коллекцию; GUID'ы акций, что признаны невалидными
     */
    private Set<Long> putActionsIntoCacheAndRemoveInvalidOnes(Map<Long, AdvertisingActionEntity> actions) {
        Set<Long> result = new HashSet<Long>();

        if (actions == null || actions.isEmpty()) {
            return result;
        }

        // 1. сначала положим их в кэш не глядя
        cache.putAll(actions);

        // 2. а потом выкинем те, что невалидны:
        result = getInvalidActions(actions);
        cache.keySet().removeAll(result);

        return result;
    }

    /**
     * Вернет {@link AdvertisingActionEntity#getGuid() GUID'ы} тех акций из указанных, что {@link #validateAction(AdvertisingActionEntity) не валидны}
     *
     * @param actions акции, невалидные из которых надо вявить. ключ - GUID акции, значение - сама акция
     * @return никогда не вернет <code>null</code> - в кранейм случае вернет пустую коллекцию
     */
    private Set<Long> getInvalidActions(Map<Long, AdvertisingActionEntity> actions) {
        Set<Long> result = new HashSet<Long>();

        if (actions == null || actions.isEmpty()) {
            return result;
        }

        for (Long guid : actions.keySet()) {
            AdvertisingActionEntity action = actions.get(guid);
            if (!validateAction(action)) {
                log.error("INVALID action [{}, guid: {}] was detected: not all plugins recognzed?", action, guid);
                result.add(guid);
            }
        } // for guid

        return result;
    }

    /**
     * Проверяет указанную, <em>уже полностью вытянутую из БД (со всеми полями-коллекциями)</em> {@link AdvertisingActionEntity РА} на валидность.
     * <p/>
     * NOTE: пока (2015-05-25) на валидность проверяем только то, что плагины РА сумели ВСЕ распознать.
     *
     * @param action акция, что надо проверить
     * @return <code>false</code>, если указанная акция невалидна (т.е., ее не стоит применять)
     */
    private boolean validateAction(AdvertisingActionEntity action) {
        boolean result = false;

        if (action == null) {
            return result;
        }

        int pluginsCount = action.getPlugins().size();
        int deserializedPluginsCount = action.getDeserializedPlugins().size();

        // если хоть один плагин не удалось десериализовать, то количество де-сериализованных плагинов будет меньше:

        return pluginsCount == deserializedPluginsCount;
    }

    /**
     * Вернет <code>true</code>, если указанная дата внутри указанного периода времени.
     *
     * @param date   дата, что надо проверить
     * @param period период, на принадлежность которому надо проверить эту дату
     * @return <code>false</code>, если аргумент <code>date</code> is <code>null</code>
     */
    private static boolean isTheDateWithinTheInterval(Date date, TimestampPeriodEntity period) {
        boolean result = false;

        if (date != null && (period == null ||
                ((period.getStart() == null || period.getStart().before(date)) && // start
                        (period.getFinish() == null || period.getFinish().after(date))))) { // finish
            result = true;
        }

        return result;
    }

    @Override
    public void setPurchaseCache(Map<Long, AdvertisingActionEntity> pluginsCache) {
        this.pluginsCache = pluginsCache;
    }

    @Override
    public Map<Long, AdvertisingActionEntity> getCache() {
        return cache;
    }
    // getters and setters:

    public CashAdvertisingActionDao getActionsDao() {
        return actionsDao;
    }

    public void setActionsDao(CashAdvertisingActionDao actionsDao) {
        this.actionsDao = actionsDao;
    }

    public boolean isThreadStart() {
        return threadStart;
    }

    public void setThreadStart(boolean threadStart) {
        this.threadStart = threadStart;
    }

    public boolean isInitOnlyWithGuids() {
        return initOnlyWithGuids;
    }

    public void setInitOnlyWithGuids(boolean initOnlyWithGuids) {
        this.initOnlyWithGuids = initOnlyWithGuids;
    }
}
