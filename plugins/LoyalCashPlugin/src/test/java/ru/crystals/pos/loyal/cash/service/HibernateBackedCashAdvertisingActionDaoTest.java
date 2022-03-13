package ru.crystals.pos.loyal.cash.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.powermock.reflect.Whitebox;

import ru.crystals.discounts.ActionPluginEntity;
import ru.crystals.discounts.ActionPluginPropertyEntity;
import ru.crystals.discounts.ActionPluginType;
import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.discounts.ApplyMode;
import ru.crystals.discounts.TimestampPeriodEntity;
import ru.crystals.pos.datasource.jdbc.JDBCMapperImpl;
import ru.crystals.test.DataBase;


/**
 * Будет тестировать корректность реализации {@link CashAdvertisingActionDao} классом {@link HibernateBackedCashAdvertisingActionDao}.
 *
 * @author aperevozchikov
 */
public class HibernateBackedCashAdvertisingActionDaoTest {
    /**
     * the SUT (System Under Test)
     */
    private static CashAdvertisingActionDao sut;

    /**
     * for JDBC tests
     */
    private static JdbcBackedCashAdvertisingActionDao sutJdbc;

    /**
     * Дата - разделитель (порог), по которой в тестах будем искать РА
     */
    private static final Date DATE = new Date();

    /**
     * А это будут АКТИВНЫЕ РА в тестах. Ключ - GUID РА, значение - период действия этой РА.
     * <p/>
     * Implementation Note: будете редактировать - проследите, чтоб GUID'ы активных и пассивных РА не пересекались
     */
    private static Map<Long, TimestampPeriodEntity> activeActions;
    static {
        activeActions = new HashMap<>();
        TimestampPeriodEntity timeSpan;
        long guid = 1L;

        // насчитал всего 6 классов эквивалентности:
        // 1. бесконечный период действия (от минус до плюс бесконечности):
        timeSpan = new TimestampPeriodEntity();
        timeSpan.setStart(null);
        timeSpan.setFinish(null);
        activeActions.put(guid++, timeSpan);

        // 2. Начинается до DATE и не заканчивается:
        timeSpan = new TimestampPeriodEntity();
        timeSpan.setStart(new Date(DATE.getTime() - TimeUnit.DAYS.toMillis(10)));
        timeSpan.setFinish(null);
        activeActions.put(guid++, timeSpan);

        // 3. Начинается до DATE, заканчивается после:
        timeSpan = new TimestampPeriodEntity();
        timeSpan.setStart(new Date(DATE.getTime() - TimeUnit.DAYS.toMillis(10)));
        timeSpan.setFinish(new Date(DATE.getTime() + TimeUnit.DAYS.toMillis(10)));
        activeActions.put(guid++, timeSpan);

        // 4. Начинается и заканичивается до DATE:
        timeSpan = new TimestampPeriodEntity();
        timeSpan.setStart(new Date(DATE.getTime() - TimeUnit.DAYS.toMillis(10)));
        timeSpan.setFinish(new Date(DATE.getTime() - TimeUnit.DAYS.toMillis(5)));
        activeActions.put(guid++, timeSpan);

        // 5. Начинается и заканичивается после DATE:
        timeSpan = new TimestampPeriodEntity();
        timeSpan.setStart(new Date(DATE.getTime() + TimeUnit.DAYS.toMillis(5)));
        timeSpan.setFinish(new Date(DATE.getTime() + TimeUnit.DAYS.toMillis(10)));
        activeActions.put(guid++, timeSpan);

        // 6. Начинается после DATE и не заканчивается:
        timeSpan = new TimestampPeriodEntity();
        timeSpan.setStart(new Date(DATE.getTime() + TimeUnit.DAYS.toMillis(5)));
        timeSpan.setFinish(null);
        activeActions.put(guid, timeSpan);
    }

    /**
     * А это будут ПАССИВНЫЕ РА в тестах. Ключ - GUID РА, значение - период действия этой РА.
     * <p/>
     * Implementation Note: будете редактировать - проследите, чтоб GUID'ы активных и пассивных РА не пересекались
     */
    private static Map<Long, TimestampPeriodEntity> passiveActions;
    static {
        passiveActions = new HashMap<>();
        TimestampPeriodEntity timeSpan;
        long guid = 10L;

        // NOTE: вообще, достаточно одно пассивной РА, чтоб убедиться, что пассивные РА никогда не попадают в выборку
        // 1. бесконечный период действия (от минус до плюс бесконечности):
        timeSpan = new TimestampPeriodEntity();
        timeSpan.setStart(null);
        timeSpan.setFinish(null);
        passiveActions.put(guid++, timeSpan);

        // 2. Начинается до DATE и не заканчивается:
        timeSpan = new TimestampPeriodEntity();
        timeSpan.setStart(new Date(DATE.getTime() - TimeUnit.DAYS.toMillis(10)));
        timeSpan.setFinish(null);
        passiveActions.put(guid++, timeSpan);

        // 3. Начинается до DATE, заканчивается после:
        timeSpan = new TimestampPeriodEntity();
        timeSpan.setStart(new Date(DATE.getTime() - TimeUnit.DAYS.toMillis(10)));
        timeSpan.setFinish(new Date(DATE.getTime() + TimeUnit.DAYS.toMillis(10)));
        passiveActions.put(guid++, timeSpan);

        // 4. Начинается и заканичивается до DATE:
        timeSpan = new TimestampPeriodEntity();
        timeSpan.setStart(new Date(DATE.getTime() - TimeUnit.DAYS.toMillis(10)));
        timeSpan.setFinish(new Date(DATE.getTime() - TimeUnit.DAYS.toMillis(5)));
        passiveActions.put(guid++, timeSpan);

        // 5. Начинается и заканичивается после DATE:
        timeSpan = new TimestampPeriodEntity();
        timeSpan.setStart(new Date(DATE.getTime() + TimeUnit.DAYS.toMillis(5)));
        timeSpan.setFinish(new Date(DATE.getTime() + TimeUnit.DAYS.toMillis(10)));
        passiveActions.put(guid++, timeSpan);

        // 6. Начинается после DATE и не заканчивается:
        timeSpan = new TimestampPeriodEntity();
        timeSpan.setStart(new Date(DATE.getTime() + TimeUnit.DAYS.toMillis(5)));
        timeSpan.setFinish(null);
        passiveActions.put(guid++, timeSpan);

        // 7. Давно закончилась
        timeSpan = new TimestampPeriodEntity();
        timeSpan.setStart(new Date(DATE.getTime() - TimeUnit.DAYS.toMillis(50)));
        timeSpan.setFinish(new Date(DATE.getTime() - TimeUnit.DAYS.toMillis(45)));
        passiveActions.put(guid, timeSpan);
    }

    /**
     * метки РА
     */
    private static Map<Long, Collection<String>> actionsLabels;
    static {
        actionsLabels = new HashMap<>();
        actionsLabels.put(1L, Lists.newArrayList("Label0", "Label1"));
        actionsLabels.put(2L, Lists.newArrayList("Label2"));
    }

    /**
     * Проверим, что вернет все активные РА, если оба аргумента == NULL
     */
    @Test
    public void shouldReturnAllActiveActionsIfBothArgumentsAreNull() {
        // given
        // РА уже в БД

        // when
        // Извлечем без указания GUID'ов
        Map<Long, AdvertisingActionEntity> extrated = sut.getActionsByGuids(null, null);

        // then
        // 1. Убедимся, что пассивные РА не попали в выборку:
        Set<Long> e = new HashSet<>(extrated.keySet());
        e.retainAll(passiveActions.keySet());
        Assert.assertTrue("some passive actions were extracted!", e.isEmpty());

        // 2. Убедимся, что нам вернули правильные РА:
        Assert.assertTrue("wrong actions were extracted!", extrated.keySet().containsAll(activeActions.keySet()) && activeActions.keySet().containsAll(extrated.keySet()));
    }

    /**
     * Проверим, что GUID'ы, передаваемые в метод корректно учитываются
     */
    @Test
    public void shouldRegardGuidsWhileExtractingActions() {
        // given
        // РА уже в БД
        Set<Long> guids = new HashSet<>();

        // впихнем все активные и пассивные Ра и исключим одну (произвольную) активную:
        guids.addAll(activeActions.keySet());
        guids.addAll(passiveActions.keySet());
        long excludedAction = activeActions.keySet().iterator().next();
        guids.remove(excludedAction);

        // when
        // Извлечем без указания даты
        Map<Long, AdvertisingActionEntity> extrated = sut.getActionsByGuids(guids, null);

        // then
        // 1. Убедимся, что пассивные РА не попали в выборку:
        Set<Long> e = new HashSet<>(extrated.keySet());
        e.retainAll(passiveActions.keySet());
        Assert.assertTrue("some passive actions were extracted!", e.isEmpty());

        // 2. В результат не попала исключенная по GUID активная акция:
        Assert.assertTrue("the excluded action was returned!", !extrated.keySet().contains(excludedAction));

        // 3. В результат попали ВСЕ активные РА, за исключением ... исключенной:
        e = new HashSet<>(activeActions.keySet());
        e.removeAll(extrated.keySet());
        Assert.assertTrue("wrong actions were extracted!", e.size() == 1 && e.contains(excludedAction));
    }

    /**
     * Проверим, что дата, передаваемая в метод корректно учитывается
     */
    @Test
    public void shouldRegardDateArgumentWhileExtractingActions() {
        // given
        // РА уже в БД

        // when
        // Извлечем без указания GUID'ов
        Map<Long, AdvertisingActionEntity> extrated = sut.getActionsByGuids(null, DATE);

        // then
        // 1. Убедимся, что пассивные РА не попали в выборку:
        Set<Long> e = new HashSet<>(extrated.keySet());
        e.retainAll(passiveActions.keySet());
        Assert.assertTrue("some passive actions were extracted!", e.isEmpty());

        // 2. Убедимся, что нам вернули правильные РА:
        Set<Long> expected = new HashSet<>();
        for (long guid : activeActions.keySet()) {
            TimestampPeriodEntity lifeSpan = activeActions.get(guid);
            if (lifeSpan.getFinish() == null || lifeSpan.getFinish().compareTo(DATE) >= 0) {
                // эта РА должна быть в результате
                expected.add(guid);
            }
        }
        Assert.assertTrue("wrong actions were extracted!", extrated.keySet().containsAll(expected) && expected.containsAll(extrated.keySet()));
    }

    /**
     * Проверим, что корректно учитывается оба аргумента
     */
    @Test
    public void shouldRegardBothArgumentsWhileExtractingActions() {
        // given
        // РА уже в БД
        Set<Long> guids = new HashSet<>();

        // впихнем все активные и пассивные РА и исключим две произвольные активные:
        guids.addAll(activeActions.keySet());
        guids.addAll(passiveActions.keySet());
        Iterator<Long> it = activeActions.keySet().iterator();
        Set<Long> excludedActions = new HashSet<>();
        excludedActions.add(it.next());
        excludedActions.add(it.next());
        guids.removeAll(excludedActions);

        // when
        // Извлечем акции
        Map<Long, AdvertisingActionEntity> extrated = sut.getActionsByGuids(guids, DATE);

        // then
        // 1. Убедимся, что пассивные РА не попали в выборку:
        Set<Long> e = new HashSet<>(extrated.keySet());
        e.retainAll(passiveActions.keySet());
        Assert.assertTrue("some passive actions were extracted!", e.isEmpty());

        // 2. Убедимся, что нам вернули правильные РА:
        Set<Long> expected = new HashSet<>();
        for (long guid : activeActions.keySet()) {
            TimestampPeriodEntity lifeSpan = activeActions.get(guid);
            if ((lifeSpan.getFinish() == null || lifeSpan.getFinish().compareTo(DATE) >= 0) && !excludedActions.contains(guid)) {
                // эта РА должна быть в результате
                expected.add(guid);
            }
        }
        Assert.assertTrue("wrong actions were extracted!", extrated.keySet().containsAll(expected) && expected.containsAll(extrated.keySet()));
    }

    /**
     * Проверим, что вернет периоды действия всех активных РА, если оба аргумента == NULL
     */
    @Test
    public void shouldReturnAllActiveActionsLifespansIfBothArgumentsAreNull() {
        // given
        // РА уже в БД

        // when
        // Извлечем без указания GUID'ов
        Map<Long, TimestampPeriodEntity> extrated = sut.getActionsLifeSpans(null, null);

        // then
        // 1. Убедимся, что пассивные РА не попали в выборку:
        Set<Long> e = new HashSet<>(extrated.keySet());
        e.retainAll(passiveActions.keySet());
        Assert.assertTrue("some passive actions were extracted!", e.isEmpty());

        // 2. Убедимся, что нам вернули правильные РА:
        Assert.assertTrue("wrong actions were extracted!", extrated.keySet().containsAll(activeActions.keySet()) && activeActions.keySet().containsAll(extrated.keySet()));
    }

    /**
     * Проверим, что GUID'ы, передаваемые в метод корректно учитываются - при извлечении периодов действия РА
     */
    @Test
    public void shouldRegardGuidsWhileExtractingActionsLifespans() {
        // given
        // РА уже в БД
        Set<Long> guids = new HashSet<>();

        // впихнем все активные и пассивные Ра и исключим одну (произвольную) активную:
        guids.addAll(activeActions.keySet());
        guids.addAll(passiveActions.keySet());
        long excludedAction = activeActions.keySet().iterator().next();
        guids.remove(excludedAction);

        // when
        // Извлечем без указания даты
        Map<Long, TimestampPeriodEntity> extrated = sut.getActionsLifeSpans(guids, null);

        // then
        // 1. Убедимся, что пассивные РА не попали в выборку:
        Set<Long> e = new HashSet<>(extrated.keySet());
        e.retainAll(passiveActions.keySet());
        Assert.assertTrue("some passive actions were extracted!", e.isEmpty());

        // 2. В результат не попала исключенная по GUID активная акция:
        Assert.assertTrue("the excluded action was returned!", !extrated.keySet().contains(excludedAction));

        // 3. В результат попали ВСЕ активные РА, за исключением ... исключенной:
        e = new HashSet<>(activeActions.keySet());
        e.removeAll(extrated.keySet());
        Assert.assertTrue("wrong actions were extracted!", e.size() == 1 && e.contains(excludedAction));
    }

    /**
     * Проверим, что дата, передаваемая в метод корректно учитывается - при извлечении периодов действия РА
     */
    @Test
    public void shouldRegardDateArgumentWhileExtractingActionsLifespans() {
        // given
        // РА уже в БД

        // when
        // Извлечем без указания GUID'ов
        Map<Long, TimestampPeriodEntity> extrated = sut.getActionsLifeSpans(null, DATE);

        // then
        // 1. Убедимся, что пассивные РА не попали в выборку:
        Set<Long> e = new HashSet<>(extrated.keySet());
        e.retainAll(passiveActions.keySet());
        Assert.assertTrue("some passive actions were extracted!", e.isEmpty());

        // 2. Убедимся, что нам вернули правильные РА:
        Set<Long> expected = new HashSet<>();
        for (long guid : activeActions.keySet()) {
            TimestampPeriodEntity lifeSpan = activeActions.get(guid);
            if (lifeSpan.getFinish() == null || lifeSpan.getFinish().compareTo(DATE) >= 0) {
                // эта РА должна быть в результате
                expected.add(guid);
            }
        }
        Assert.assertTrue("wrong actions were extracted!", extrated.keySet().containsAll(expected) && expected.containsAll(extrated.keySet()));
    }

    /**
     * Проверим, что корректно учитывается оба аргумента - при извлечении периодов действия РА
     */
    @Test
    public void shouldRegardBothArgumentsWhileExtractingActionsLifespans() {
        // given
        // РА уже в БД
        Set<Long> guids = new HashSet<>();

        // впихнем все активные и пассивные РА и исключим две произвольные активные:
        guids.addAll(activeActions.keySet());
        guids.addAll(passiveActions.keySet());
        Iterator<Long> it = activeActions.keySet().iterator();
        Set<Long> excludedActions = new HashSet<>();
        excludedActions.add(it.next());
        excludedActions.add(it.next());
        guids.removeAll(excludedActions);

        // when
        // Извлечем акции
        Map<Long, TimestampPeriodEntity> extrated = sut.getActionsLifeSpans(guids, DATE);

        // then
        // 1. Убедимся, что пассивные РА не попали в выборку:
        Set<Long> e = new HashSet<>(extrated.keySet());
        e.retainAll(passiveActions.keySet());
        Assert.assertTrue("some passive actions were extracted!", e.isEmpty());

        // 2. Убедимся, что нам вернули правильные РА:
        Set<Long> expected = new HashSet<>();
        for (long guid : activeActions.keySet()) {
            TimestampPeriodEntity lifeSpan = activeActions.get(guid);
            if ((lifeSpan.getFinish() == null || lifeSpan.getFinish().compareTo(DATE) >= 0) && !excludedActions.contains(guid)) {
                // эта РА должна быть в результате
                expected.add(guid);
            }
        }
        Assert.assertTrue("wrong actions were extracted!", extrated.keySet().containsAll(expected) && expected.containsAll(extrated.keySet()));
    }

    /**
     * Проверим, что корректно учитывается оба аргумента - при извлечении периодов действия РА
     */
    @Test
    public void shouldDeleteStaleActions() {
        int deleted = sutJdbc.removeStaleActions(40, Integer.MAX_VALUE);

        Assert.assertEquals(1, deleted);
    }

    /**
     * Проверим, что корректно считываются метки РА
     */
    @Test
    public void shouldGetLabelsWhenGetActionsByGuids() {
        Map<Long, AdvertisingActionEntity> result = sutJdbc.getActionsByGuids(null, DATE);
        for (Map.Entry<Long, Collection<String>> labelsEntry : actionsLabels.entrySet()) {
            assertThat(result.get(labelsEntry.getKey()).getLabels()).containsOnlyElementsOf(labelsEntry.getValue()).hasSize(labelsEntry.getValue().size());
        }
    }

    /**
     * Инициализация: подготовка БД и SUT к тестированию.
     */
    @BeforeClass
    public static void setUp() throws Exception {
        // Настроим наш SUT:
        PGSimpleDataSource dataSource = (PGSimpleDataSource) DataBase.DISCOUNT.getDataSource();

        HibernateBackedCashAdvertisingActionDao sutToBe = new HibernateBackedCashAdvertisingActionDao();
        sutToBe.setDataSource(dataSource);
        sutToBe.setHibernateProperties(getProperties());
        sutToBe.setMapping(annotatedClasses);
        sutToBe.setThreadStart(false); // да для теста тоже не важно

        // ну и инициализация пошла:
        sutToBe.postConstruct();

        sut = sutToBe;

        sutJdbc = new JdbcBackedCashAdvertisingActionDao();
        Whitebox.setInternalState(sutJdbc, "jdbcMapper", new JDBCMapperImpl(DataBase.DISCOUNT.getDataSource(), true));

        // А теперь тестовые РА в БД захерачим:
        // 1. активные РА:
        for (long guid : activeActions.keySet()) {
            AdvertisingActionEntity action = buildRandomAction();
            action.setActive(true);
            action.setGuid(guid);
            action.setWorkPeriod(activeActions.get(guid));
            if (actionsLabels.get(guid) != null) {
                action.getLabels().addAll(actionsLabels.get(guid));
            }

            // и сохранить в БД:
            sut.saveAction(action);
        }

        // 2. пассивные РА:
        for (long guid : passiveActions.keySet()) {
            AdvertisingActionEntity action = buildRandomAction();
            action.setActive(false);
            action.setGuid(guid);
            action.setWorkPeriod(passiveActions.get(guid));

            // и сохранить в БД:
            sut.saveAction(action);
        }
    }
    /**
     * Чистка после теста: надо прибрать за собой: почистить что натворили в БД.
     */
    @AfterClass
    public static void tearDown() {
        // чистим SUT:
        ((HibernateBackedCashAdvertisingActionDao) sut).preDestroy();
    }

    private static final String[] annotatedClasses = new String[] {
            "ru.crystals.discounts.AdvertisingActionEntity",
            "ru.crystals.discounts.ActionPluginEntity",
            "ru.crystals.discounts.ActionPluginPropertyEntity",
            "ru.crystals.discounts.CoverageAreaEntity"
    };

    /**
     * Эта штука вернет настройки соединения с БД для нашего SUT'а.
     */
    private static Properties getProperties() {
        Properties result = new Properties();
        result.setProperty("hibernate.current_session_context_class", "thread");
        result.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        result.setProperty("show_sql", "false");
        result.setProperty("format_sql", "true");
        result.setProperty("transaction.factory_class", "org.hibernate.transaction.JDBCTransactionFactory");
        return result;
    }

    /**
     * Содаст какую-то пустую, бессмысленную РА. Только для проверки сохранения и поиска акций в БД.
     *
     * @return бессмысленную РА. активную, но без GUID и периода действия
     */
    private static AdvertisingActionEntity buildRandomAction() {
        AdvertisingActionEntity result = new AdvertisingActionEntity();

        result.setActive(true);
        result.setAllNodes(true);
        result.setCoverageArea(null);//currently not using in calculation
        result.setDescription(RandomStringUtils.randomAlphabetic(255));
        result.setDisplayStyleName(RandomStringUtils.randomAlphabetic(255));
        result.setGuid(null);
        result.setLastChanges(new Date());
        result.setMode(ApplyMode.UNCONDITIONAL);//fill it in current testCase!
        result.setName(RandomStringUtils.randomAlphabetic(255));
        result.setParentGuid(null);
        result.setPriority(RandomUtils.nextDouble());
        result.setResultTypes(null);
        result.setUseRestrictions(true);//fill it in current testCase!
        result.setVersion(1);
        result.setWorkPeriod(null);
        result.setWorksAnytime(false);
        result.setPlugins(new HashSet<>());

        // плагины (хоть один плагин да должен быть):
        int pluginsCount = 1 + RandomUtils.nextInt(2);
        for (int i = 0; i < pluginsCount; i++) {
            result.getPlugins().add(buildRandomPlugin(result));
        }

        return result;
    }

    /**
     * Создаст бессмысленный плагин РА, годный только для сохранения/чтения из БД. Пробовать его десериализовать не стоит.
     *
     * @param action РА, к которой относится этот плагин
     * @return ActionPluginEntity
     */
    private static ActionPluginEntity buildRandomPlugin(AdvertisingActionEntity action) {
        ActionPluginEntity result = new ActionPluginEntity();

        result.setClassName(RandomStringUtils.randomAlphabetic(255));
        result.setType(ActionPluginType.values()[RandomUtils.nextInt(ActionPluginType.values().length)]);

        // свойства
        int propertiesCount = 1 + RandomUtils.nextInt(3);
        int depth = 1 + RandomUtils.nextInt(2);

        for (int i = 0; i < propertiesCount; i++) {
            result.getProperties().add(buildRandomProperty(action, depth));
        }

        return result;
    }

    /**
     * Создает бессмысленное свойство плагина РА.
     *
     * @param action
     *            РА, к которой относится плагин, к которому относится данное (генерируемое) свойство
     * @param depth
     *            глубина дочерних свойств, что надо создать
     * @return херню
     */
    private static ActionPluginPropertyEntity buildRandomProperty(AdvertisingActionEntity action, int depth) {
        ActionPluginPropertyEntity result = new ActionPluginPropertyEntity();

        result.setAction(action);
        result.setClassName(RandomStringUtils.randomAlphabetic(255));
        result.setName(RandomStringUtils.randomAlphabetic(255));
        result.setPluginName(RandomStringUtils.randomAlphabetic(255));
        result.setType(RandomStringUtils.randomAlphabetic(255));
        result.setValue(RandomStringUtils.randomAlphabetic(255));

        if (depth > 0) {
            // количество дочерних пропертин:
            int childrenCount = 1 + RandomUtils.nextInt(3);
            for (int i = 0; i < childrenCount; i++) {
                result.getProperties().add(buildRandomProperty(action, depth - 1));
            }
        }

        return result;
    }
}
