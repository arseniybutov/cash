package ru.crystals.pos.loyal.cash.service;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.discounts.ActionPluginEntity;
import ru.crystals.discounts.ActionPluginPropertyEntity;
import ru.crystals.discounts.ActionResultTypeEntity;
import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.discounts.MasterActionEntity;
import ru.crystals.discounts.TimestampPeriodEntity;
import ru.crystals.pos.datasource.jdbc.JDBCMapper;
import ru.crystals.pos.datasource.jdbc.JDBCMapperDS;
import ru.crystals.pos.datasource.jdbc.JDBCMapperDSImpl;
import ru.crystals.pos.datasource.jdbc.JDBCMapperImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Эта реализация {@link CashAdvertisingActionDao} ковыряется в БД через JDBC.
 *
 * @author aperevozchikov
 */
public class JdbcBackedCashAdvertisingActionDao implements CashAdvertisingActionDao {

    /**
     * черный ящик
     */
    private static Logger log = LoggerFactory.getLogger(JdbcBackedCashAdvertisingActionDao.class);

    // injected
    /**
     * Класс SQL-драйвера, что будем использовать при установке соединения с БД через JDBC
     */
    private String sqlDriverClass = "org.postgresql.Driver";

    // injected
    /**
     * URL БД. с которой будем рабоать
     */
    private String dbUrl = "jdbc:postgresql://localhost:5432/discount";

    // injected
    /**
     * Логин пользователя, от имени которого будем выполнять запросы
     */
    private String login = "postgres";

    // injected
    /**
     * пароль пользователя
     */
    private String password = "postgres";

    // injected
    /**
     * размер пула соединений к БД. Нам нужено только одно соединение
     */
    private int connectionPoolSize = 1;

    // injected
    /**
     * FIXME: временное решение (2016-04-22).
     * Запросы обновления/модификации данных в БД будем делегировать к этой реализации {@link CashAdvertisingActionDao}.
     * <p/>
     * т.к. через JDBC без пока (2016-04-22) не умеем модифицировать данные в БД
     */
    private CashAdvertisingActionDao delegate;

    private JDBCMapper jdbcMapper = null;
    private JDBCMapper getJdbcMapper() {
        if (jdbcMapper != null) {
            return jdbcMapper;
        }
        log.info("creating jdbcMapper...");
        try {
            JDBCMapperDS ds = new JDBCMapperDSImpl(sqlDriverClass, dbUrl, login, password, connectionPoolSize);
            jdbcMapper = new JDBCMapperImpl(ds, true);
        } catch (Throwable t) {
            log.error("failed to create jdbc mapper!", t);
        }
        return jdbcMapper;
    }

    /**
     * Инициализация: то, что в энтерпрайзе аннотируется как @PostConstruct, а в Spring'е указывается значением атрибута init-method:этот метод
     * следует вызывать до вызова любого бизнес-метода
     */
    public void postConstruct() {
        log.trace("entering postConstruct()");
        getJdbcMapper();
        log.trace("leaving postConstruct()");
    }

    /**
     * Еще один life-cycle method: этот должен вызываться перед уничтожением этого объекта. В энтерпрайзе такие аннотируются как @PreDestroy, а в
     * Spring'овых конфигах - суются как значение атрибута destroy-method.
     */
    public void preDestroy() {
        log.trace("entering preDestroy()");
        // do nothing?
        log.trace("leaving preDestroy()");
    }

    @Override
    public Map<Long, AdvertisingActionEntity> getActionsByGuids(Collection<Long> guids, Date date) {
        Map<Long, AdvertisingActionEntity> result = new HashMap<Long, AdvertisingActionEntity>();
        long stopWatch = System.currentTimeMillis();

        if (log.isTraceEnabled()) {
            log.trace("entering getActionsByGuids(Collection, Date). The arguments are: guids [{}], date: {}", guids,
                date == null ? "(NULL)" : date);
        }

        if (guids != null && guids.isEmpty()) {
            // аргумент пуст. по контракту вернем пустую коллекцию. но это необычное событие:
            log.warn("getActionsByGuids(Collection, Date): the argument is EMPTY! Empty collection will be returned!");
            return result;
        }

        StringBuilder whereCondition = new StringBuilder();
        List<Object> args = new ArrayList<>();
        if (guids != null) {
            whereCondition.append(whereCondition.length() > 0 ? " AND" : "");
            whereCondition.append("(guid = ANY (:guid))");
            args.add(guids);
        }
        whereCondition.append(whereCondition.length() > 0 ? " AND" : "");
        whereCondition.append("(active = :active)");
        args.add(Boolean.TRUE);
        if (date != null) {
            whereCondition.append(whereCondition.length() > 0 ? " AND" : "");
            whereCondition.append("(periodfinish IS NULL OR periodfinish > :date)");
            args.add(date);
        }

        List<AdvertisingActionEntity> actions = null;
        try {
            getJdbcMapper().startTransaction();

            // 1. вытянем сами РА
            long interimStopWatch = System.currentTimeMillis();
            if (whereCondition.length() > 0) {
                actions = getJdbcMapper().selectListByCondition(AdvertisingActionEntity.class, "WHERE " + whereCondition.toString(), args.toArray());
            } else {
                actions = getJdbcMapper().selectList(AdvertisingActionEntity.class);
            }
            log.trace("actions (withou collections) were extracted in {} [ms]", System.currentTimeMillis() - interimStopWatch);

            // 2. и дотянем поля-коллекции (JDBC mapper пока (2016-04-22) этого делать не умеет)
            pullCollections(actions);

            getJdbcMapper().commitTransaction();
        } catch (Throwable t) {
            log.error("failed to get actions!", t);
            getJdbcMapper().rollbackTransaction();
        }

        // 3. формируем результат
        if (CollectionUtils.isNotEmpty(actions)) {
            for (AdvertisingActionEntity a : actions) {
                if (a == null || a.getGuid() == null) {
                    log.error("INVALID action was detected: {}", a);
                    continue;
                }
                result.put(a.getGuid(), a);
            }
        }

        log.trace("leaving getActionsByGuids(Collection, Date). The result size is: {}; it took {} [ms]", result.size(),
            System.currentTimeMillis() - stopWatch);

        return result;
    }

    /**
     * Подтянет из БД поля-коллекции указанных акций
     *
     * @param actions РА. чьи поля-коллекции надо вытянуть из БД
     */
    private void pullCollections(Collection<AdvertisingActionEntity> actions) {
        long stopWatch = System.currentTimeMillis();

        log.trace("entering pullCollections(Collection). The argument is: actions [size: {}]", actions == null ? "(NULL)" : actions.size());

        if (CollectionUtils.isEmpty(actions)) {
            log.trace("leaving pullCollections(Collection). The argument is EMPTY!");
            return;
        }

        Map<Long, AdvertisingActionEntity> actionsAsMap = new HashMap<>();
        for (AdvertisingActionEntity a : actions) {
            if (a == null || a.getId() == null) {
                log.error("INVALID action was detectd (ID is null): {}", a);
                continue;
            }
            actionsAsMap.put(a.getId(), a);
        }

        // 1. Плагины и плагинные свойства
        fillActionsPluginsAndProperties(actionsAsMap);

        // 2. мастер-акции
        fillActionsMasterActions(actionsAsMap);

        // 3. типы результата
        fillActionsResultTypes(actionsAsMap);

        // 4. топология (зона охвата) на кассе не нужна

        // 5. метки
        fillActionsLabels(actionsAsMap);

        log.trace("leaving pullCollections(Collection). It took {} [ms]", System.currentTimeMillis() - stopWatch);
    }


    @SuppressWarnings("unchecked")
    @Override
    public Map<Long, TimestampPeriodEntity> getActionsLifeSpans(Collection<Long> guids, Date date) {
        Map<Long, TimestampPeriodEntity> result = new HashMap<Long, TimestampPeriodEntity>();
        long stopWatch = System.currentTimeMillis();

        if (log.isTraceEnabled()) {
            log.trace("entering getActionsLifeSpans(Collection, Date). The arguments are: guids [{}], date: {}", guids,
                date == null ? "(NULL)" : String.format("%1$tF %1$tT.%1$tL", date));
        }

        if (guids != null && guids.isEmpty()) {
            log.trace("leaving getActionsLifeSpans(Collection, Date). The \"guids\" argument is EMPTY!");
            return result;
        }

        StringBuilder whereCondition = new StringBuilder();
        List<Object> args = new ArrayList<>();
        if (guids != null) {
            whereCondition.append(whereCondition.length() > 0 ? " AND" : "");
            whereCondition.append("(guid = ANY (:guid))");
            args.add(guids);
        }
        whereCondition.append(whereCondition.length() > 0 ? " AND" : "");
        whereCondition.append("(active = :active)");
        args.add(Boolean.TRUE);
        if (date != null) {
            whereCondition.append(whereCondition.length() > 0 ? " AND" : "");
            whereCondition.append("(periodfinish IS NULL OR periodfinish > :date)");
            args.add(date);
        }

        List<List<Object>> extracted = null;
        try {
            getJdbcMapper().startTransaction();

            extracted = getJdbcMapper().selectList("SELECT guid, periodstart, periodfinish FROM discounts_advertisingactions" +
                (whereCondition.length() < 1 ? "" : (" WHERE " + whereCondition.toString())), args.toArray());

            getJdbcMapper().commitTransaction();
        } catch (Throwable t) {
            log.error("failed to get action lifespans!", t);
            getJdbcMapper().rollbackTransaction();
        }

        if (CollectionUtils.isEmpty(extracted)) {
            log.trace("leaving getActionsLifeSpans(Collection, Date). No one action was detected");
            return result;
        }

        for (List<Object> row : extracted) {
            if (row == null || row.size() != 3 || !(row.get(0) instanceof Number)) {
                log.warn("getActionsLifeSpans(Collection, Date): illegal action was detected in the DB: GUID is NULL!");
                continue;
            }
            long guid = ((Number) row.get(0)).longValue();

            TimestampPeriodEntity value = new TimestampPeriodEntity();
            if (row.get(1) instanceof Date) {
                value.setStart((Date) row.get(1));
            }
            if (row.get(2) instanceof Date) {
                value.setFinish((Date) row.get(2));
            }

            result.put(guid, value);
        } // for row

        log.trace("leaving getActionsLifeSpans(Collection, Date). The result size is: {}; it took {} [ms]", result.size(),
            System.currentTimeMillis() - stopWatch);

        return result;
    }

    @Override
    public AdvertisingActionEntity saveAction(AdvertisingActionEntity action) {
        AdvertisingActionEntity result = null;
        long stopWatch = System.currentTimeMillis();

        log.trace("entering saveAction(AdvertisingActionEntity). The argument is: loyTx [{}]", action);

        result = delegate.saveAction(action);

        log.trace("leaving saveAction(AdvertisingActionEntity). The result is: {}; it took {} [ms]", result, System.currentTimeMillis() - stopWatch);

        return result;
    }

    @Override
    public Map<Long, AdvertisingActionEntity> removeExistingAndSave(Collection<AdvertisingActionEntity> actions) {
        Map<Long, AdvertisingActionEntity> result = new HashMap<Long, AdvertisingActionEntity>();
        long stopWatch = System.currentTimeMillis();

        log.trace("entering removeExistingAndSave(Collection). The argument size is: {}", actions == null ? "(NULL)" : actions.size());

        if (CollectionUtils.isEmpty(actions)) {
            log.warn("removeExistingAndSave(Collection): The argument is EMPTY! An empty collection will be returned!");
            return result;
        }

        result = delegate.removeExistingAndSave(actions);

        log.trace("leaving removeExistingAndSave(Collection). The result size is: {}; it took {} [ms]", result.size(),
            System.currentTimeMillis() - stopWatch);

        return result;
    }

    @Override
    public int removeStaleActions(int daysAfterFinishToRemove, int maxRecordsToRemoveAtOnce) {
        int deleted = 0;
        long stopWatch = System.currentTimeMillis();
        Date dateDeleteBefore = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(daysAfterFinishToRemove));

        log.trace("entering removeStaleActions(int). The argument maxRecordsToRemoveAtOnce is: {}", maxRecordsToRemoveAtOnce);
        try {
            jdbcMapper.startTransaction();

            deleted = jdbcMapper.deleteByCondition(AdvertisingActionEntity.class,
                    "WHERE ctid IN (SELECT ctid FROM discounts_advertisingactions WHERE periodfinish < :dateDeleteBefore LIMIT :maxRecords)",
                    dateDeleteBefore, maxRecordsToRemoveAtOnce);

            jdbcMapper.commitTransaction();
        } catch (Exception e) {
            log.error("failed to delete stale actions!", e);
            jdbcMapper.rollbackTransaction();
        }

        log.trace("leaving removeStaleActions(int). The result deleted is: {}; it took {} [ms]", deleted,
                System.currentTimeMillis() - stopWatch);
        return deleted;
    }

    private void fillActionsPluginsAndProperties(Map<Long, AdvertisingActionEntity> actionsMap) {
        long interimStopWatch = System.currentTimeMillis();
        // вытягивание плагинов
        Map<Long, ActionPluginEntity> pluginMap = new HashMap<>();
        List<ActionPluginEntity> plugins = jdbcMapper.selectListByCondition(ActionPluginEntity.class, "WHERE action_id = ANY (:ids) ORDER BY action_id", actionsMap.keySet());
        for (ActionPluginEntity plugin : plugins) {
            // сохраним связь с акцией
            AdvertisingActionEntity action = actionsMap.get(plugin.getAction().getId());
            plugin.setAction(action);
            action.getPlugins().add(plugin);
            pluginMap.put(plugin.getId(), plugin);
        }
        log.trace("plugins were extracted and mapped in {} [ms]", System.currentTimeMillis() - interimStopWatch);

        interimStopWatch = System.currentTimeMillis();
        // вытягивание свойств плагинов и свойств свойств
        List<ActionPluginPropertyEntity> properties = jdbcMapper.selectListByCondition(ActionPluginPropertyEntity.class, "WHERE action_id = ANY (:ids) ORDER BY action_id, parent_id NULLS FIRST", actionsMap.keySet());
        Map<Long, ActionPluginPropertyEntity> propertyMap = new HashMap<>();
        List<Long> notLinkedPropertyList = new ArrayList<>();

        for (ActionPluginPropertyEntity property : properties) {
            if (property.getPlugin() != null && property.getPlugin().getId() != null) {
                //сохраним связь с плагином
                ActionPluginEntity plugin = pluginMap.get(property.getPlugin().getId());
                property.setPlugin(plugin);
                plugin.getProperties().add(property);
            } else if (property.getParentId() != null) {
                if (propertyMap.containsKey(property.getParentId())) {
                    ActionPluginPropertyEntity parent = propertyMap.get(property.getParentId());
                    property.setPlugin(null);
                    parent.getProperties().add(property);
                } else {
                    notLinkedPropertyList.add(property.getId());
                }
            }
            // заполним связь с акцией
            property.setAction(actionsMap.get(property.getAction().getId()));

            propertyMap.put(property.getId(), property);
        }

        if (!notLinkedPropertyList.isEmpty()) {
            // слинкуем свойства, которые не удалось прежде
            for (Long propertyId : notLinkedPropertyList) {
                ActionPluginPropertyEntity property = propertyMap.get(propertyId);
                ActionPluginPropertyEntity parent = propertyMap.get(property.getParentId());
                property.setPlugin(null);
                parent.getProperties().add(property);
            }
        }
        log.trace("plugins properties were extracted and mapped in {} [ms]", System.currentTimeMillis() - interimStopWatch);
    }

    private void fillActionsMasterActions(Map<Long, AdvertisingActionEntity> actionsMap) {
        long stopWatch = System.currentTimeMillis();
        // вытягивание Guid'ов мастер акций
        List<MasterActionEntity> masterActions = jdbcMapper.selectListByCondition(MasterActionEntity.class, "WHERE discounts_advertisingactions_id = ANY (:ids) ORDER BY discounts_advertisingactions_id", actionsMap.keySet());
        for (MasterActionEntity masterAction : masterActions) {
            actionsMap.get(masterAction.getActionId()).getMasterActionGuids().add(masterAction.getMasterGuid());
        }
        log.trace("master actions were extracted and mapped in {} [ms]", System.currentTimeMillis() - stopWatch);
    }

    private void fillActionsResultTypes(Map<Long, AdvertisingActionEntity> actionsMap) {
        long stopWatch = System.currentTimeMillis();
        // вытягивание result types
        List<ActionResultTypeEntity> resultTypes = jdbcMapper.selectListByCondition(ActionResultTypeEntity.class, "WHERE discounts_advertisingactions_id = ANY (:ids) ORDER BY discounts_advertisingactions_id", actionsMap.keySet());
        for (ActionResultTypeEntity resultType : resultTypes) {
            actionsMap.get(resultType.getActionId()).getResultTypes().add(resultType.getResultTypeValue());
        }
        log.trace("result types were mapped in and mapped {} [ms]", System.currentTimeMillis() - stopWatch);
    }

    private void fillActionsLabels(Map<Long, AdvertisingActionEntity> actionsMap) {
        long stopWatch = System.currentTimeMillis();
        // вытягивание меток акций
        List<List<Object>> extracted = null;
        if (getJdbcMapper() != null) {
            try {
                jdbcMapper.startTransaction();
                extracted = jdbcMapper.selectList("SELECT action_id, label_value FROM discounts_advertisingactions_actions_labels" +
                        " WHERE action_id = ANY (:ids)", actionsMap.keySet());
                jdbcMapper.commitTransaction();
            } catch (Exception e) {
                log.error("failed to get action labels!", e);
                jdbcMapper.rollbackTransaction();
            }
        }

        if (CollectionUtils.isEmpty(extracted)) {
            log.trace("no labels were extracted");
            return;
        }

        for (List<Object> row : extracted) {
            if (row == null || row.size() != 2 || !(row.get(0) instanceof Number)) {
                log.warn("fillActionsLabels: invalid data!");
                continue;
            }
            long id = ((Number) row.get(0)).longValue();
            actionsMap.get(id).getLabels().add((String) row.get(1));
        }
        log.trace("labels were extracted and mapped in {} [ms]", System.currentTimeMillis() - stopWatch);
    }

    // getters & setters


    public String getSqlDriverClass() {
        return sqlDriverClass;
    }

    public void setSqlDriverClass(String sqlDriverClass) {
        this.sqlDriverClass = sqlDriverClass;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getConnectionPoolSize() {
        return connectionPoolSize;
    }

    public void setConnectionPoolSize(int connectionPoolSize) {
        this.connectionPoolSize = connectionPoolSize;
    }

    public CashAdvertisingActionDao getDelegate() {
        return delegate;
    }

    public void setDelegate(CashAdvertisingActionDao delegate) {
        this.delegate = delegate;
    }
}
