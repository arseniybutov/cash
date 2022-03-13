package ru.crystals.pos.loyal.cash.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.discounts.ActionPluginPropertyEntity;
import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.discounts.TimestampPeriodEntity;
import ru.crystals.loyal.interfaces.ActionIntrospector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Одна из реализаций {@link ActionIntrospector}. Вот эта залезает в БД через JDBC.
 *
 * @author aperevozchikov
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ActionIntrospectorImpl implements ActionIntrospector {

    /**
     * черный ящик
     */
    private static Logger log = LoggerFactory.getLogger(ActionIntrospectorImpl.class);

    // injected
    /**
     * Настройки для подключения к БД
     */
    private Properties jdbcProperties;

    // injected
    /**
     * URL для доступа до базы с сущностями лояльности
     */
    private String jdbcUrl;

    /**
     * Кэш номеров купонов, что необходимы для срабатывания хоть одной акции
     */
    private final Map<String, Set<ActionRange>> actionCouponsCache = new ConcurrentHashMap<>();

    /**
     * Этот ANSI-SQL скрипт будет искать номера купонов, что надо применить в чеке для срабатывания хоть одной действующией РА
     */
    private static final String GET_ACTION_TRIGGERING_COUPONS = "SELECT a.\"value\", c.periodstart, c.periodfinish, c.guid " +
    		"FROM discounts_action_plugin_property AS a " +
    		"INNER JOIN discounts_action_plugin AS b ON a.plugin_id = b.id " +
    		"INNER JOIN discounts_advertisingactions AS c ON b.action_id = c.id " +
    		"WHERE a.\"name\" = 'couponNumber' AND length(a.\"value\") > 0 " +
    		"     AND b.class_name = 'ru.crystalservice.setv6.discounts.plugins.CouponsCondition'";

    protected static class ActionRange {
        Long guid;
        Date start;
        Date finish;

        public ActionRange(Long guid, Date start, Date finish) {
            this.guid = guid;
            this.start = start;
            this.finish = finish;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ActionRange that = (ActionRange) o;
            return guid.equals(that.guid);
        }

        @Override
        public int hashCode() {
            return guid.hashCode();
        }
    }

    private void updateCache() {
        actionCouponsCache.clear();
        actionCouponsCache.putAll(getActionTriggeringCouponsFromDB());
    }

    @Override
    public void updateActions(Collection<AdvertisingActionEntity> newActions, Collection<AdvertisingActionEntity> removedActions) {
        for (AdvertisingActionEntity action : newActions) {
            action.getPlugins().stream()
                    .filter(pl -> "ru.crystalservice.setv6.discounts.plugins.CouponsCondition".equals(pl.getClassName()))
                    .flatMap(pl -> pl.getProperties().stream())
                    .filter(pr -> "couponNumber".equals(pr.getName()))
                    .map(ActionPluginPropertyEntity::getValue)
                    .findAny().ifPresent(couponNumber -> {
                Set<ActionRange> actionRanges = actionCouponsCache.computeIfAbsent(couponNumber, k -> new HashSet<>());
                TimestampPeriodEntity workPeriod = action.getWorkPeriod();
                workPeriod = workPeriod == null ? new TimestampPeriodEntity() : workPeriod;
                actionRanges.add(new ActionRange(action.getGuid(), workPeriod.getStart(), workPeriod.getFinish()));
            });
        }
        Set<Long> removeGuids = removedActions.stream()
                .map(AdvertisingActionEntity::getGuid)
                .collect(Collectors.toSet());
        actionCouponsCache.values().forEach(ranges -> ranges.removeIf(range -> removeGuids.contains(range.guid)));
    }

    @Override
    public Set<String> getActionTriggeringCoupons(Date date) {
        long stopWatch = System.currentTimeMillis();

        if (log.isTraceEnabled()) {
            log.trace("entering getActionTriggeringCoupons(Date). The argument is: date [{}]",
                date == null ? "(NULL)" : String.format("%1$tF %1$tT.%1$tL", date));
        }
        if (date == null) {
            date = new Date();
        }

        final Date finalDate = date;
        Set<String> result = actionCouponsCache.entrySet().stream()
                .filter(entry -> entry.getValue().stream() // проверяем, есть ли у купона попадание в текущую дату
                        .anyMatch(range -> (range.start == null || range.start.getTime() <= finalDate.getTime()) &&
                                (range.finish == null || range.finish.getTime() >= finalDate.getTime())))
                .map(Map.Entry::getKey).collect(Collectors.toSet());

        log.trace("leaving getActionTriggeringCoupons(Date). the result is: {}; it took {} [ms]",
            result, System.currentTimeMillis() - stopWatch);

        return result;
    }

    private Map<String, Set<ActionRange>> getActionTriggeringCouponsFromDB() {
        Map<String, Set<ActionRange>> result = new HashMap<>();
        long stopWatch = System.currentTimeMillis();

        log.trace("getActionTriggeringCouponsFromDB: query to execute: \"{}\"", GET_ACTION_TRIGGERING_COUPONS);

        try(Connection con = DriverManager.getConnection(getJdbcUrl(), getJdbcProperties());
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(GET_ACTION_TRIGGERING_COUPONS)) {

            while (rs.next()) {
                String number = rs.getString(1);
                Timestamp start = rs.getTimestamp(2);
                Timestamp end = rs.getTimestamp(3);
                Long guid = rs.getLong(4);
                Set<ActionRange> ranges = result.computeIfAbsent(number, k -> new HashSet<>());
                ranges.add(new ActionRange(guid, start, end));
            }
        } catch (SQLException se) {
            log.error("getActionTriggeringCoupons!", se);
            result = null;
        }

        log.trace("leaving getActionTriggeringCouponsFromDB(). the result is: {}; it took {} [ms]",
                result, System.currentTimeMillis() - stopWatch);

        return result;
    }

    // init-method
    /**
     * Инициализация. Должна быть вызвана сразу же после создания после создания этого бина и DI (dependency injections) - до вызова любого
     * бизнес-метода.
     */
    public void postConstruct() {
        log.trace("<ActionIntrospector> was registered!");
        Executors.newSingleThreadExecutor().submit(this::updateCache);
    }

    // destroy-method
    /**
     * Другой метод жизненного цикла (Lifecycle method): этот следует вызывать перед уничтожением этого объекта.
     */
    public void preDestroy() {
        // do some clean-up. Можно, например, из BundleManager'а себя выкинуть... как-то
    }

    public Properties getJdbcProperties() {
        return jdbcProperties;
    }

    public void setJdbcProperties(Properties jdbcProperties) {
        this.jdbcProperties = jdbcProperties;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

}
