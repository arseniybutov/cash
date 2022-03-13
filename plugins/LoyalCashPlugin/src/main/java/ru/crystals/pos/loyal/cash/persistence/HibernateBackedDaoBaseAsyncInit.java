package ru.crystals.pos.loyal.cash.persistence;

import org.hibernate.Session;
import org.slf4j.Logger;

import java.util.concurrent.Executors;

/**
 * Реализация <code>HibernateBackedDaoBase</code> с возможностью настройки Hiberntae'овской session factory в отдельном потоке для
 * ускорения загрузки кассы.
 */
public class HibernateBackedDaoBaseAsyncInit extends HibernateBackedDaoBase {

    private Logger log;

    // injected
    /**
     * Чисто тюнинговая вещь: этот флаг указывает запускать ли настройку Hibernate'а (Hiberntae'овской session factory) в отдельном потоке, либо же
     * будет настроен в этом же потоке.
     * <p/>
     * side note: Замечено, что на "быстрых" кассах имели значительное сокращение времени старт-апа кассы (времени до появления окна логина),
     * если инициализация Hibernate'овских session factory велась в отдельных потоках, а на "медленных" (20ка (beetle 20k), например) старт
     * происходил быстрее если все эти Hibernate'овские session factory инициализировались в одном потоке (друг за другом - последовательно).
     */
    private boolean threadStart = true;

    // вот эти два поля нужны только из-за того, что инициализация Hibernate'а будет вестись в отдельном потоке (если threadStart == true)
    // (типа чтоб не задерживать старт-ап кассы: окно ввода логина появилось и юзер доволен; ну и что,
    // что при этом касса поначалу подтормаживает - кучу таких же как этот объектов продолжает в теневых потоках настраивать).
    // так вот: эти 2 поля нужны только для того, чтоб не допустить (да просто притормозить) попыток работы с БД
    // пока Hibernate еще не настроен полностью
    private final Object lock = new Object();

    // признак того, что инициализация этого объекта завершена
    private boolean started = false;

    public HibernateBackedDaoBaseAsyncInit(Logger log) {
        super(log);
        this.log = log;
    }

    /**
     * Инициализация: то, что в энтерпрайзе аннотируется как @PostConstruct, а в Spring'е указывается значением атрибута init-method:этот метод
     * следует вызывать до вызова любого бизнес-метода
     */
    @Override
    public void postConstruct() {
        log.trace("Start building hibernate session factory in {} thread.", threadStart ? "separate" : "main");
        Runnable starter = () -> {
            synchronized (lock) {
                HibernateBackedDaoBaseAsyncInit.super.postConstruct();
            }
        };

        if (threadStart) {
            Executors.newSingleThreadExecutor().execute(starter);
        } else {
            // видно, касса "медленная" - надо в этом же потоке Hibernate настроить. и пусть остальные Spring'овые бины ждут
            starter.run();
        }
    }

    @Override
    public void preDestroy() {
        super.preDestroy();
        started = false;
    }

    /**
     * А вот и самый главный метод: только так следует получать доступ до сессии перед тем как полезть в БД. Получение сессии только через этот метод
     * гарантирует, что Hibernate уже успели проинициализировать.
     *
     * @return сессию, через которую можно залезть в БД
     */
    @Override
    protected Session getSession() {
        // чудная версия double checked locking...
        if (!started) {
            long stopWatch = System.currentTimeMillis();
            synchronized (lock) {
                log.trace("getSession(): lock was obtained in {} [ms]", System.currentTimeMillis() - stopWatch);
                return super.getSession();
            }
        } else {
            return super.getSession();
        }
    }

    // getters & setters

    public boolean isThreadStart() {
        return threadStart;
    }

    public void setThreadStart(boolean threadStart) {
        this.threadStart = threadStart;
    }

}
