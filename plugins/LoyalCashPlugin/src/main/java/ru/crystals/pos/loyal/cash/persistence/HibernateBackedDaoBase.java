package ru.crystals.pos.loyal.cash.persistence;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.slf4j.Logger;
import ru.crystals.pos.datasource.IHibernateConsts;
import ru.crystals.pos.speed.SpeedLog;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

public class HibernateBackedDaoBase {

    private Logger log;

    // injected
    /**
     * Коннект к БД.
     */
    private DataSource dataSource;

    // injected
    /**
     * Список {@link Class#getCanonicalName() полных канонических имен} классов сущностей, которые будем сохранять в БД (и извлекать оттуда).
     */
    private String[] mapping;

    // injected
    /**
     * Hibernate'овские настройки соединения с БД
     */
    private Properties hibernateProperties;

    /**
     * Вот оно: именно через эту штуку будем ковыряться в БД. Настраивается при старте/инициализации объекта.
     */
    private SessionFactory sessionFactory;

    public HibernateBackedDaoBase(Logger log) {
        this.log = log;
    }

    /**
     * Инициализация: то, что в энтерпрайзе аннотируется как @PostConstruct, а в Spring'е указывается значением атрибута init-method:этот метод
     * следует вызывать до вызова любого бизнес-метода
     */
    public void postConstruct() {
        long time = SpeedLog.getTime();

        try {
            Configuration config = getConfiguration();
            long stopWatch = System.currentTimeMillis();
            log.trace("building hibernate session factory");
            StandardServiceRegistryBuilder serviceRegistryBuilder = new StandardServiceRegistryBuilder();
            serviceRegistryBuilder.applySettings(config.getProperties());
            if (dataSource != null) {
                serviceRegistryBuilder.applySetting(Environment.DATASOURCE, dataSource);
            }
            sessionFactory = config.buildSessionFactory(serviceRegistryBuilder.build());
            log.trace("hibernate session factory was built in {} [ms]", System.currentTimeMillis() - stopWatch);
        } catch (Throwable t) {
            // да не важно по какой причине не смогли проинициализировать объект: этим объектом все равно нельзя пользоваться!
            log.error("failed to initialize this object!", t);

            // и пробросим дальше:
            throw new RuntimeException(t);
        }

        SpeedLog.logModuleLoadingTime(getClass().getSimpleName(), time);
    }

    private Configuration getConfiguration() throws ClassNotFoundException {
        Configuration config = new Configuration();

        if (getHibernateProperties() != null) {
            for (Map.Entry<Object, Object> entry : getHibernateProperties().entrySet()) {
                config.setProperty((String) entry.getKey(), (String) entry.getValue());
            }
        }

        if (getMapping() != null) {
            for (String classMap : getMapping()) {
                config.addAnnotatedClass(Class.forName(classMap));
            }
        }

        IHibernateConsts.fillValidateParams(config);
        return config;
    }

    /**
     * Еще один life-cycle method: этот должен вызываться перед уничтожением этого объекта. В энтерпрайзе такие аннотируются как @PreDestroy, а в
     * Spring'овых конфигах - суются как значение атрибута destroy-method.
     */
    public void preDestroy() {
        getSession().close();
        sessionFactory.close();
    }

    /**
     * А вот и самый главный метод: только так следует получать доступ до сессии перед тем как полезть в БД. Получение сессии только через этот метод
     * гарантирует, что Hibernate уже успели проинициализировать.
     *
     * @return сессию, через которую можно залезть в БД
     */
    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    protected interface Tx<T> {
        T call(Session session);
    }

    protected <T> T withTx(Tx<T> tx) {
        Session session = getSession();
        Transaction htx = session.beginTransaction();
        try {
            T res = tx.call(session);
            htx.commit();
            return res;
        } catch (Exception e) {
            htx.rollback();
            throw new RuntimeException(e);
        }
    }

    // getters & setters

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String[] getMapping() {
        return mapping;
    }

    public void setMapping(String[] mapping) {
        this.mapping = mapping;
    }

    public Properties getHibernateProperties() {
        return hibernateProperties;
    }

    public void setHibernateProperties(Properties hibernateProperties) {
        this.hibernateProperties = hibernateProperties;
    }
}
