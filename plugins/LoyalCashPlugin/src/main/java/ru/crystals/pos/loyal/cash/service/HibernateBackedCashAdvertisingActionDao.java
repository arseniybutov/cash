package ru.crystals.pos.loyal.cash.service;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.discounts.TimestampPeriodEntity;
import ru.crystals.pos.loyal.cash.persistence.HibernateBackedDaoBaseAsyncInit;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Эта реализация {@link CashAdvertisingActionDao} ковыряется в БД через Hibernate.
 *
 * @author aperevozchikov
 */
public class HibernateBackedCashAdvertisingActionDao extends HibernateBackedDaoBaseAsyncInit implements CashAdvertisingActionDao {

    /**
     * черный ящик
     */
    private static Logger log = LoggerFactory.getLogger(HibernateBackedCashAdvertisingActionDao.class);

    public HibernateBackedCashAdvertisingActionDao() {
        super(log);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Long, AdvertisingActionEntity> getActionsByGuids(Collection<Long> guids, Date date) {
        Map<Long, AdvertisingActionEntity> result = new HashMap<Long, AdvertisingActionEntity>();
        long stopWatch = System.currentTimeMillis();

        if (log.isTraceEnabled()) {
            log.trace("entering getActionsByGuids(Collection, Date). The arguments are: guids [{}], date: {}",
                guids, date == null ? "(NULL)" : String.format("%1$tF %1$tT.%1$tL", date));
        }

        if (guids == null || !guids.isEmpty()) {
            Transaction tx = null;
            try {
                long interimStopWatch = System.currentTimeMillis();
                Session session = getSession();
                log.trace("getActionsByGuids(Collection, Date): it took {} [ms] to get session", System.currentTimeMillis() - interimStopWatch);

                interimStopWatch = System.currentTimeMillis();
                tx = session.beginTransaction();
                log.trace("getActionsByGuids(Collection, Date): it took {} [ms] to begin transaction", System.currentTimeMillis() - interimStopWatch);

                session.setDefaultReadOnly(true);

                //сформируем критерии для извлечения РА:
                Criterion activeActions = Restrictions.eq("active", Boolean.TRUE);
                if (guids != null) {
                    activeActions = Restrictions.and(activeActions, Restrictions.in("guid", guids));
                }
                if (date != null) {
                    activeActions = Restrictions.and(activeActions, Restrictions.or(Restrictions.isNull("workPeriod.finish"), Restrictions.gt("workPeriod.finish", date)));
                }

                Criteria c = session.createCriteria(AdvertisingActionEntity.class);
                c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
                c.setFetchMode("plugins", FetchMode.JOIN);
                c.setReadOnly(true);
                c.add(activeActions);
                c.setLockMode(LockMode.NONE);

                List<AdvertisingActionEntity> actions = c.list();
                if (actions != null && !actions.isEmpty()) {
                    for (AdvertisingActionEntity aae : actions) {
                        if (aae != null && aae.getGuid() != null) {
                            result.put(aae.getGuid(), aae);
                        } else {
                            log.error("getActionsByGuids(Collection, Date): the DB is INCONSISTENT: an action [{}] " +
                            		"having NULL GUID was extracted from the DB", aae);
                        }
                    } // for aae
                } else {
                    log.trace("no one action was found");
                }

                tx.commit();
            } catch (Exception e) {
                log.error(String.format("failed to retrieve actions by guids: %s", guids), e);
                if (tx != null) {
                    tx.rollback();
                }
                throw new RuntimeException(e);
            }
        } else {
            // аргумент пуст. по контракту вернем пустую коллекцию. но это необычное событие:
            log.warn("getActionsByGuids(Collection, Date): the argument is EMPTY! Empty collection will be returned!");
        }
        log.trace("leaving getActionsByGuids(Collection, Date). The result size is: {}; it took {} [ms]",
            result.size(), System.currentTimeMillis() - stopWatch);

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Long, TimestampPeriodEntity> getActionsLifeSpans(Collection<Long> guids, Date date) {
        Map<Long, TimestampPeriodEntity> result = new HashMap<Long, TimestampPeriodEntity>();
        long stopWatch = System.currentTimeMillis();

        if (log.isTraceEnabled()) {
            log.trace("entering getActionsLifeSpans(Collection, Date). The arguments are: guids [{}], date: {}",
                guids, date == null ? "(NULL)" : String.format("%1$tF %1$tT.%1$tL", date));
        }

        if (guids == null || !guids.isEmpty()) {
            Transaction tx = null;
            try {
                Session session = getSession();
                tx = session.beginTransaction();

                // критерии выборки:
                Criterion activeActions = Restrictions.eq("active", Boolean.TRUE);
                if (guids != null) {
                    activeActions = Restrictions.and(activeActions, Restrictions.in("guid", guids));
                }
                if (date != null) {
                    activeActions = Restrictions.and(activeActions, Restrictions.or(Restrictions.isNull("workPeriod.finish"), Restrictions.gt("workPeriod.finish", date)));
                }

                // установка набора полей, что надо вернуть (guid + start + finish):
                Criteria c = session.createCriteria(AdvertisingActionEntity.class);
                c.setProjection(Projections.projectionList().add(Projections.property("guid")).add(Projections.property("workPeriod.start")).add(Projections.property("workPeriod.finish")));
                c.setReadOnly(true);
                c.add(activeActions);

                // ну и сама выборка:
                List<Object[]> list = c.list();
                if (list != null && list.size() > 0) {
                    for (Object[] row : list) {
                        if (row != null && row.length == 3 && row[0] instanceof Number) {
                            long guid = ((Number) row[0]).longValue();

                            TimestampPeriodEntity value = new TimestampPeriodEntity();
                            if (row[1] instanceof Date) {
                                value.setStart((Date) row[1]);
                            }
                            if (row[2] instanceof Date) {
                                value.setFinish((Date) row[2]);
                            }

                            result.put(guid, value);
                        } else {
                            log.warn("getActionsLifeSpans(Collection, Date): illegal action was detected in the DB: GUID is NULL!");
                        }
                    } // for row
                } else {
                    log.trace("no one action was found");
                }

                tx.commit();
            } catch (Exception e) {
                log.error(String.format("failed to retrieve actions life-spans by guids: %s", guids), e);
                if (tx != null) {
                    tx.rollback();
                }
                throw new RuntimeException(e);
            }
        } else {
            // аргумент пуст. по контракту вернем пустую коллекцию. но это необычное событие:
            log.warn("getActionsLifeSpans(Collection, Date): the argument is EMPTY! Empty collection will be returned!");
        }
        log.trace("leaving getActionsLifeSpans(Collection, Date). The result size is: {}; it took {} [ms]",
            result.size(), System.currentTimeMillis() - stopWatch);

        return result;
    }

    @Override
    public AdvertisingActionEntity saveAction(AdvertisingActionEntity action) {
        AdvertisingActionEntity result = null;
        long stopWatch = System.currentTimeMillis();

        log.trace("entering saveAction(AdvertisingActionEntity). The argument is: loyTx [{}]", action);

        if (action != null) {
            Transaction tx = null;
            try {
                Session session = getSession();
                tx = session.beginTransaction();
                result = (AdvertisingActionEntity) session.merge(action);
                tx.commit();
            } catch (Exception e) {
                log.error(String.format("Failed to save action: %s", action), e);

                if (tx != null) {
                    tx.rollback();
                }

                throw new RuntimeException(e);
            }
        } else {
            log.warn("saveAction: the argument is NULL!");
        }
        log.trace("leaving saveAction(AdvertisingActionEntity). The result is: {}; it took {} [ms]",
            result, System.currentTimeMillis() - stopWatch);

        return result;
    }

    /**
     * Этот HQL-запрос позволит удалить РА из БД по их {@link AdvertisingActionEntity#getGuid() GUID'ам}
     */
    private static final String DELETE_ACTIONS_BY_GUIDS = "DELETE FROM AdvertisingActionEntity AS a WHERE a.guid IN (:guids)";

    @Override
    public Map<Long, AdvertisingActionEntity> removeExistingAndSave(Collection<AdvertisingActionEntity> actions) {
        Map<Long, AdvertisingActionEntity> result = new HashMap<Long, AdvertisingActionEntity>();
        long stopWatch = System.currentTimeMillis();

        log.trace("entering removeExistingAndSave(Collection). The argument size is: {}", actions == null ? "(NULL)" : actions.size());
        if (actions != null && !actions.isEmpty()) {
            Map<Long, AdvertisingActionEntity> actionsToSave = new HashMap<Long, AdvertisingActionEntity>(actions.size());
            for (AdvertisingActionEntity action : actions) {
                if (action != null && action.getGuid() != null) {
                    if (actionsToSave.containsKey(action.getGuid())) {
                        // видимо быстро дважды поменяли РА? И эта РА пришла дважды в той же пачке?
                        if (log.isWarnEnabled()) {
                            log.warn("removeExistingAndSave(Collection): at least two actions having the same GUID [{}] were detecetd in the argument! " +
                            		"The latter [{}] will be preferred to the earlier one [{}]",
                            		new Object[] {action.getGuid(), action, actionsToSave.get(action.getGuid())});
                        }
                    }
                    actionsToSave.put(action.getGuid(), action);
                } else {
                    log.warn("removeExistingAndSave(Collection): an ILLEGAL element [{}] was detected in the argument: " +
                    		"either NULL, or its GUID is NULL. It will be removed from further processing!", action);
                }
            } // for actions
            log.trace("about to persist the following actions [by GUIDs] ({}) : {}", actionsToSave.size(), actionsToSave.keySet());

            if (!actionsToSave.isEmpty()) {
                Transaction tx = null;
                try {
                    Session session = getSession();
                    tx = session.beginTransaction();

                    // 1. удалим РА с этими GUID'ами
                    Query q = session.createQuery(DELETE_ACTIONS_BY_GUIDS);
                    q.setParameterList("guids", actionsToSave.keySet());
                    int removed = q.executeUpdate();
                    log.trace("[{}] actions were removed from the DB", removed);

                    // 2. выкинем те РА, чей статус не активен:
                    for (Iterator<Long> it = actionsToSave.keySet().iterator(); it.hasNext();) {
                        Long guid = it.next();
                        AdvertisingActionEntity action = actionsToSave.get(guid);
                        // если NULL, то все же считаем, что акция активна
                        if (Boolean.FALSE.equals(action.getActive())) {
                            log.trace("the action [{}] will not be persisted because it is NOT ACTIVE", action);
                            it.remove();
                        } else if (action.getActive() == null) {
                            // все-таки ругнемся:
                            log.warn("removeExistingAndSave(Collection): activity-flag of the actions [{}] is set to NULL! " +
                            		"It will be treated as ACTIVE while persisting in the DB though...", action);
                        }
                    } // for it

                    // 3. и сохраним оставшиеся РА в БД
                    for (AdvertisingActionEntity action : actionsToSave.values()) {
                        // тупо persist - никакой не merge!
                        session.persist(action);

                        // и впихнуть в результат:
                        result.put(action.getGuid(), action);
                    } // for action
                    log.trace("about to commit the TX: [{}] actions will be persisted", actionsToSave.size());

                    tx.commit();
                } catch (Exception e) {
                    log.error("removeExistingAndSave(Collection): Failed to save actions!", e);

                    if (tx != null) {
                        tx.rollback();
                    }

                    throw new RuntimeException(e);
                }
            } else {
                // нежданчик! ни одной валидной РА в аргументе?
                log.warn("removeExistingAndSave(Collection): NO ONE VALID element was detected in the argument! Nothing will be done!");
            }
        } else {
            log.warn("removeExistingAndSave(Collection): The argument is EMPTY! An empty collection will be returned!");
        }
        log.trace("leaving removeExistingAndSave(Collection). The result size is: {}; it took {} [ms]",
            result.size(), System.currentTimeMillis() - stopWatch);

        return result;
    }

}
