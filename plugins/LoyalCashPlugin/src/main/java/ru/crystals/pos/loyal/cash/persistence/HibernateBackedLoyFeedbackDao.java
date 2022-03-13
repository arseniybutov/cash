package ru.crystals.pos.loyal.cash.persistence;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.discount.processing.entity.FeedbackAnnulmentStrategy;
import ru.crystals.discount.processing.entity.FeedbackTime;
import ru.crystals.discount.processing.entity.LoyExtProviderFeedback;
import ru.crystals.discount.processing.entity.LoyMultipleFeedbackResolutionStrategy;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Hibernate реализация {@link LoyFeedbackDao}.
 *
 * Note: Реализация с инициализацией в основном потоке, т.к. при определенных комбинациях модулей обращение к DAO происходит раньше,
 * чем успевает проинициализироваться sessionFactory в отдельном потоке.
 */
public class HibernateBackedLoyFeedbackDao extends HibernateBackedDaoBase implements LoyFeedbackDao {
    private static Logger log = LoggerFactory.getLogger(HibernateBackedLoyFeedbackDao.class);

    public HibernateBackedLoyFeedbackDao() {
        super(log);
    }

    @Override
    public Long saveOrUpdate(LoyExtProviderFeedback feedback) {
        log.trace("Entering saveOrUpdate(feedback = {}).", feedback);
        if (feedback == null) {
            log.warn("saveOrUpdate: the argument is EMPTY.");
            log.trace("Leaving saveOrUpdate.");
            return null;
        }
        long stopWatch = System.currentTimeMillis();
        Long result = withTx(session -> {
            LoyExtProviderFeedback dbFeedback = null;
            if (feedback.getMultipleFeedbackResolutionStrategy() == null ||
                    feedback.getMultipleFeedbackResolutionStrategy() == LoyMultipleFeedbackResolutionStrategy.UPDATE_EXISTING) {
                List<LoyExtProviderFeedback> foundFeedbacks = getFeedbacksInternal(feedback.getDocNumber(), feedback.getShiftNumber(),
                        feedback.getCashNumber(), feedback.getShopNumber(), feedback.getProviderId(), feedback.getProcessingName(),
                        feedback.getFeedbackTime());
                if (!foundFeedbacks.isEmpty()) {
                    dbFeedback = foundFeedbacks.get(0);
                    for (int i = 1; i < foundFeedbacks.size(); ++i) {
                        session.delete(foundFeedbacks.get(i));
                    }
                }
            }
            if (dbFeedback != null) {
                feedback.setId(dbFeedback.getId());
            }
            return ((LoyExtProviderFeedback) session.merge(feedback)).getId();
        });
        log.trace("Leaving saveOrUpdate. The result is: {}; it took {} [ms]", result, System.currentTimeMillis() - stopWatch);
        return result;
    }

    @Override
    public List<LoyExtProviderFeedback> getFeedbacks(long documentNum, long shiftNum, long cashNum, long shopNum,
                                                     String providerName, String processingName, FeedbackTime sendingStrategy) {
        log.trace("Entering getFeedbacks(documentNum = {}, shiftNum = {}, cashNum = {}, shopNum = {}, providerName = \"{}\"," +
                "providerName = \"{}\", sendingStrategy = {})", documentNum, shiftNum, cashNum, shopNum, providerName, processingName, sendingStrategy);
        long stopWatch = System.currentTimeMillis();
        List<LoyExtProviderFeedback> result = withTx(session ->
                getFeedbacksInternal(documentNum, shiftNum, cashNum, shopNum, providerName, processingName, sendingStrategy));
        log.trace("Leaving getFeedbacks. The result is: {}; it took {} [ms]", result, System.currentTimeMillis() - stopWatch);
        return result;
    }

    protected List<LoyExtProviderFeedback> getFeedbacksInternal(long documentNum, long shiftNum, long cashNum, long shopNum,
                                              String providerName, String processingName, FeedbackTime sendingStrategy) {
        // Сортировка здесь ВАЖНА, потому что внешняя система может быть чувствительна к порядку следования фидбеков.
        // Конечно, это не гарантирует, что фидбеки придут именно в таком порядке, просто как дополнительная помощь.
        // А именно по id сортируем потому что сортировка по времени недостаточно точная: фидбеки могут создаться настолько быстро,
        // что таймстамп не успеет обновиться. А последовательность успеет всегда.
        Criteria criteria = getSession().createCriteria(LoyExtProviderFeedback.class);
        criteria.add(Restrictions.eq("shopNumber", shopNum));
        criteria.add(Restrictions.eq("cashNumber", cashNum));
        criteria.add(Restrictions.eq("shiftNumber", shiftNum));
        criteria.add(Restrictions.eq("docNumber", documentNum));
        criteria.add(Restrictions.eq("providerId", providerName));
        criteria.add(processingName != null ? Restrictions.eq("processingName", processingName) : Restrictions.isNull("processingName"));
        criteria.add(Restrictions.eq("feedbackTime", sendingStrategy));
        criteria.addOrder(Order.asc("id"));
        return criteria.list();
    }

    @Override
    public LoyExtProviderFeedback getFeedbackByPK(long documentNum, long shiftNum, long cashNum, long shopNum, String providerName, FeedbackTime feedbackTime) {
        log.trace("Entering getFeedbackByPK(documentNum = {}, shiftNum = {}, cashNum = {}, shopNum = {}, providerName = \"{}\"," +
                "feedbackTime = {})", documentNum, shiftNum, cashNum, shopNum, providerName, feedbackTime);
        long stopWatch = System.currentTimeMillis();
        LoyExtProviderFeedback result = withTx(session -> {
            Criteria criteria = session.createCriteria(LoyExtProviderFeedback.class);
            criteria.add(Restrictions.eq("shopNumber", shopNum));
            criteria.add(Restrictions.eq("shiftNumber", shiftNum));
            criteria.add(Restrictions.eq("docNumber", documentNum));
            criteria.add(Restrictions.eq("providerId", providerName));
            criteria.add(Restrictions.eq("feedbackTime", feedbackTime));
            return (LoyExtProviderFeedback) criteria.uniqueResult();
        });
        log.trace("Leaving getFeedbackByPK. The result is: {}; it took {} [ms]", result, System.currentTimeMillis() - stopWatch);
        return result;
    }

    @Override
    public Collection<LoyExtProviderFeedback> getFeedbackByProviderAndTime(String providerId, FeedbackTime feedbackTime, Integer maxCount) {
        log.trace("Entering getFeedbackByProviderAndTime(providerId = {}, feedbackTime = {}, maxCount = {})", providerId, feedbackTime, feedbackTime);
        long stopWatch = System.currentTimeMillis();
        List<LoyExtProviderFeedback> result = withTx(session -> {
            Criteria criteria = session.createCriteria(LoyExtProviderFeedback.class);
            criteria.add(Restrictions.eq("providerId", providerId));
            criteria.add(Restrictions.eq("feedbackTime", feedbackTime));
            if (maxCount != null && maxCount.compareTo(0) > 0) {
                criteria.setMaxResults(maxCount);
            }
            return criteria.list();
        });
        if (CollectionUtils.isNotEmpty(result)) {
            result.sort((c1, c2) -> {
                if (c1.getDateCreate() == null || c2.getDateCreate() == null) {
                    return 0;
                }
                return c1.getDateCreate().compareTo(c2.getDateCreate());
            });
        }
        log.trace("Leaving getFeedbackByProviderAndTime. The result is: {}; it took {} [ms]", result, System.currentTimeMillis() - stopWatch);
        return result;
    }

    @Override
    public Collection<LoyExtProviderFeedback> getFeedbackByProviderAndChequeId(String providerId, Long chequeId) {
        log.trace("Entering getFeedbackByProviderAndChequeId(providerId = {}, chequeId = {})", providerId, chequeId);
        long stopWatch = System.currentTimeMillis();
        List<LoyExtProviderFeedback> result = null;
        if (providerId != null && chequeId != null) {
            result = withTx(session -> {
                Criteria criteria = session.createCriteria(LoyExtProviderFeedback.class);
                criteria.add(Restrictions.eq("providerId", providerId));
                criteria.add(Restrictions.eq("receiptId", chequeId));
                return criteria.list();
            });
        } else {
            log.warn("getFeedbackByProviderAndChequeId: the argument is EMPTY.");
        }
        log.trace("Leaving getFeedbackByProviderAndChequeId. The result is: {}; it took {} [ms]", result, System.currentTimeMillis() - stopWatch);
        return result;
    }

    @Override
    public void remove(LoyExtProviderFeedback feedback) {
        log.trace("Entering remove(feedback = {})", feedback);
        long stopWatch = System.currentTimeMillis();
        if (feedback != null) {
            withTx(session -> {
                session.delete(feedback);
                return null;
            });
        } else {
            log.warn("remove: the argument is NULL.");
        }
        log.trace("Leaving remove. It took {} [ms]", System.currentTimeMillis() - stopWatch);
    }

    @Override
    public void remove(Collection<LoyExtProviderFeedback> feedbacks) {
        log.trace("Entering remove(feedbacks = {})", feedbacks);
        long stopWatch = System.currentTimeMillis();
        if (CollectionUtils.isNotEmpty(feedbacks)) {
            withTx(session -> {
                Query query = session.createQuery("DELETE FROM LoyExtProviderFeedback AS o WHERE o.id IN (:ids)");
                query.setParameterList("ids", feedbacks.stream().map(LoyExtProviderFeedback::getId).collect(Collectors.toList()));
                query.executeUpdate();
                return null;
            });
        } else {
            log.warn("remove: the argument is EMPTY.");
        }
        log.trace("Leaving remove. It took {} [ms]", System.currentTimeMillis() - stopWatch);
    }

    @Override
    public void remove(long documentNum, long shiftNum, long cashNum, long shopNum, String providerName, FeedbackTime feedbackTime) {
        log.trace("Entering remove(documentNum = {}, shiftNum = {}, cashNum = {}, shopNum = {}, providerName = \"{}\"," +
                "feedbackTime = {})", documentNum, shiftNum, cashNum, shopNum, providerName, feedbackTime);
        long stopWatch = System.currentTimeMillis();
        withTx(session -> {
            Query query = session.createQuery("DELETE FROM LoyExtProviderFeedback AS o WHERE o.shopNumber = :shopNumber " +
                    "AND cashNumber = :cashNumber " +
                    "AND shiftNumber = :shiftNumber " +
                    "AND docNumber = :documentNumber " +
                    "AND providerId = :providerName " +
                    "AND feedbackTime = :feedbackTime");
            query.setParameter("cashNumber", cashNum);
            query.setParameter("shopNumber", shopNum);
            query.setParameter("shiftNumber", shiftNum);
            query.setParameter("documentNumber", documentNum);
            query.setParameter("providerName",  providerName);
            query.setParameter("feedbackTime", feedbackTime);
            query.executeUpdate();
            return null;
        });
        log.trace("Leaving remove. It took {} [ms]", System.currentTimeMillis() - stopWatch);
    }

    @Override
    public void remove(long feedbackId) {
        log.trace("Entering remove(feedbackId = {}", feedbackId);
        long stopWatch = System.currentTimeMillis();
        withTx(session -> {
            Query query = session.createQuery("DELETE FROM LoyExtProviderFeedback AS o WHERE o.id = :id");
            query.setParameter("id", feedbackId);
            query.executeUpdate();
            return null;
        });
        log.trace("Leaving remove. It took {} [ms]", System.currentTimeMillis() - stopWatch);
    }

    /**
     * Удаляет фидбеки для чека с указанным идентификатором. Удаляет только те фидбеки,
     * стратегия удаления при аннулировании чека которых {@link FeedbackAnnulmentStrategy#REMOVE} или null.
     * @param id идентификатор чека, фидбеки которого требуется удалить.
     */
    @Override
    public void removeFeedbackByChequeId(long id) {
        log.trace("Entering removeFeedbackByChequeId(id = {}", id);
        long stopWatch = System.currentTimeMillis();
        withTx(session -> {
            Query query = session.createQuery("DELETE FROM LoyExtProviderFeedback AS o WHERE o.receiptId = :receiptId " +
                    "AND (annulmentStrategy IS NULL OR annulmentStrategy = :annulmentStrategy)");
            query.setParameter("receiptId", id);
            query.setParameter("annulmentStrategy", FeedbackAnnulmentStrategy.REMOVE);
            query.executeUpdate();
            return null;
        });
        log.trace("Leaving removeFeedbackByChequeId. It took {} [ms]", System.currentTimeMillis() - stopWatch);
    }

    @Override
    public LoyExtProviderFeedback getLastFeedbackByProviderAndTime(String providerName, FeedbackTime feedbackTime) {
        log.trace("Entering getLastFeedbackByProviderAndTime(providerName = {}, feedbackTime = {})", providerName, feedbackTime);
        long stopWatch = System.currentTimeMillis();
        LoyExtProviderFeedback result = null;
        if (providerName != null && feedbackTime != null) {
            result = withTx(session -> {
                Criteria criteria = session.createCriteria(LoyExtProviderFeedback.class);
                criteria.add(Restrictions.eq("providerId", providerName));
                criteria.add(Restrictions.eq("feedbackTime", feedbackTime));
                criteria.addOrder(Order.desc("receiptId"));
                criteria.setMaxResults(1);
                return (LoyExtProviderFeedback) criteria.uniqueResult();
            });
        } else {
            log.warn("getLastFeedbackByProviderAndTime: the argument is EMPTY.");
        }
        log.trace("Leaving getLastFeedbackByProviderAndTime. The result is: {}; it took {} [ms]", result, System.currentTimeMillis() - stopWatch);
        return result;
    }
}
