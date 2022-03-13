package ru.crystals.pos.loyal.cash.transport.persistence;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.loyal.DiscountGUIDEntity;
import ru.crystals.pos.loyal.LastDiscountIDEntity;
import ru.crystals.pos.loyal.cash.persistence.HibernateBackedDaoBaseAsyncInit;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Версия {@link ActionsTransportAuxiliariesDao}, что работает с БД через Hibernate.
 *
 * @author aperevozchikov
 */
public class HibernateBackedActionsTransportAuxiliariesDao extends HibernateBackedDaoBaseAsyncInit implements ActionsTransportAuxiliariesDao {

    /**
     * черный ящик
     */
    private static Logger log = LoggerFactory.getLogger(HibernateBackedActionsTransportAuxiliariesDao.class);

    public HibernateBackedActionsTransportAuxiliariesDao() {
        super(log);
    }

    @SuppressWarnings("unchecked")
    @Override
    public LastDiscountIDEntity getLastDiscountId() {
        LastDiscountIDEntity result = null;
        long stopWatch = System.currentTimeMillis();

        log.trace("entering getLastDiscountId()");

        Transaction tx = null;
        try {
            Session session = getSession();
            tx = session.beginTransaction();

            Criteria criteria = session.createCriteria(LastDiscountIDEntity.class);
            List<LastDiscountIDEntity> objects = criteria.list();

            if (objects != null && objects.size() > 0) {
                if (objects.size() > 1) {
                    log.warn("several [{}] last-discount-ids were detected! The last (arbitrary!) one will be returned as the result", objects.size());
                }
                result = objects.get((objects.size() - 1));
            }

            tx.commit();
        } catch (Exception e) {
            log.error("getLastDiscountId(): failure!", e);

            if (tx != null) {
                tx.rollback();
            }

            throw new RuntimeException("getLastDiscountId(): failure!", e);
        }
        log.trace("leaving getLastDiscountId(). The result is: {}; It took {} [ms]",
            result, System.currentTimeMillis() - stopWatch);

        return result;
    }

    @Override
    public LastDiscountIDEntity saveLastDiscountId(LastDiscountIDEntity lDiscountId) {
        LastDiscountIDEntity result = null;
        long stopWatch = System.currentTimeMillis();

        log.trace("entering saveLastDiscountId(LastDiscountIDEntity). The argument is: lDiscountId [{}]", lDiscountId);
        if (lDiscountId != null) {
            Transaction tx = null;
            try {
                Session session = getSession();
                tx = session.beginTransaction();

                // 1. Удалим все существующие записи
                Query q = session.createQuery("DELETE FROM LastDiscountIDEntity AS l");
                int removed = q.executeUpdate();
                log.trace("[{}] records were removed from loy_LastDiscountsId", removed);

                // 2. и сохраним эту:
                result = lDiscountId;
                session.save(result);

                tx.commit();
            } catch (Exception e) {
                log.error(String.format("saveLastDiscountId(LastDiscountIDEntity): failed to save %s", lDiscountId), e);

                if (tx != null) {
                    tx.rollback();
                }

                throw new RuntimeException("saveLastDiscountId(LastDiscountIDEntity): failure!", e);
            }
        } else {
            log.warn("saveLastDiscountId(LastDiscountIDEntity): the argument is NULL!");
        }
        log.trace("leaving saveLastDiscountId(LastDiscountIDEntity). The result is: {}; it took {} [ms]",
            result, System.currentTimeMillis() - stopWatch);

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Long> getProcessedActionsGuids() {
        List<Long> result = new LinkedList<Long>();
        long stopWatch = System.currentTimeMillis();

        log.trace("entering getProcessedActionsGuids()");

        Transaction tx = null;
        try {
            Session session = getSession();
            tx = session.beginTransaction();

            Criteria criteria = session.createCriteria(DiscountGUIDEntity.class);
            List<DiscountGUIDEntity> discountEntities = criteria.list();

            for (DiscountGUIDEntity discountEntity : discountEntities) {
                log.trace("removing {} from the DB...", discountEntity);
                session.delete(discountEntity);
                result.add(discountEntity.getDiscountGuid());
            }

            tx.commit();
        } catch (Exception e) {
            log.error("getProcessedActionsGuids(): failure!", e);

            if (tx != null) {
                tx.rollback();
            }

            throw new RuntimeException("getProcessedActionsGuids(): failure!", e);
        }
        log.trace("leaving getProcessedActionsGuids(). The result is: {}; it took {} [ms]",
            result, System.currentTimeMillis() - stopWatch);

        return result;
    }

    @Override
    public int saveDiscounts(Collection<Long> actionsGuids) {
        int result = 0;
        long stopWatch = System.currentTimeMillis();

        log.trace("entering saveDiscounts(Collection). The arguemnt is: actionsGuids [{}]", actionsGuids);
        if (actionsGuids != null && !actionsGuids.isEmpty()) {
            Transaction tx = null;
            try {
                Session session = getSession();
                tx = session.beginTransaction();

                for (Long guid : actionsGuids) {
                    if (guid != null) {
                        DiscountGUIDEntity discountEntity;

                        discountEntity = new DiscountGUIDEntity();
                        discountEntity.setVersion(new Integer(1).shortValue());
                        discountEntity.setId(guid);
                        discountEntity.setDiscountGuid(guid);
                        discountEntity.setGuid(guid);

                        session.save(discountEntity);

                        result++;
                    } else {
                        log.error("saveDiscounts(Collection): NULL element was detected in the argument!");
                    }
                } // for guid

                tx.commit();
            } catch (Exception e) {
                log.error(String.format("saveDiscounts(Collection): failed to save: %s", actionsGuids), e);

                if (tx != null) {
                    tx.rollback();
                }

                throw new RuntimeException("saveDiscounts(Collection): failure!", e);
            }
        } else {
            log.warn("saveDiscounts(Collection): the argument is EMPTY!");
        }
        log.trace("leaving saveDiscounts(Collection). The result is: {}; it took {} [ms]",
            result, System.currentTimeMillis() - stopWatch);

        return result;
    }
}
