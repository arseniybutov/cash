package ru.crystals.pos.loyal.cash.persistence;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.discount.processing.entity.LoyAdvActionInPurchaseEntity;
import ru.crystals.discount.processing.entity.LoyBonusDiscountTransactionEntity;
import ru.crystals.discount.processing.entity.LoyBonusPlastekTransactionEntity;
import ru.crystals.discount.processing.entity.LoyBonusSberbankTransactionEntity;
import ru.crystals.discount.processing.entity.LoyBonusTransactionEntity;
import ru.crystals.discount.processing.entity.LoyChequeAdvertiseEntity;
import ru.crystals.discount.processing.entity.LoyDiscountPositionEntity;
import ru.crystals.discount.processing.entity.LoyGiftNoteEnity;
import ru.crystals.discount.processing.entity.LoySetApiPluginTransactionEntity;
import ru.crystals.discount.processing.entity.LoyTokenSiebelTransactionEntity;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.SentToServerStatus;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Эта реализация {@link LoyTxDao} "работает" с БД через Hibernate.
 *
 * @author aperevozchikov
 */
public class HibernateBackedLoyTxDao extends HibernateBackedDaoBaseAsyncInit implements LoyTxDao {

    /**
     * Этот PSQL-запрос вернет дату и время самой "свежей" TX лояльности, что следует выкинуть из оперативного справочника.
     * Т.е., записи, имеющие {@link LoyTransactionEntity#getSaleTime() дату фискализации} не "свежее" данной признаются уже не нужными для хранения в БД кассы.
     * Параметры запроса:
     * <og>
     * <li> shop - номер магазина, которой принадлежит данная касса;
     * <li> cash - номер данной кассы;
     * <li> shiftCount - максимальное количество последних смен, TX лояльности которых не должны удаляться;
     * </og>
     */
    // @formatter:off
    private static final String GET_FIRST_STALE_RECORD_TIME = "WITH RECURSIVE t AS (\n" +
            "   SELECT id, shift_number, 1  as n\n" +
            "   FROM    loy_transaction\n" +
            "   WHERE id = (SELECT max(id) FROM loy_transaction WHERE shop_number = :shop AND cash_number = :cash)\n" +
            "   UNION ALL\n" +
            "   SELECT (SELECT id\n" +
            "       FROM loy_transaction \n" +
            "       WHERE id < t.id AND shift_number <> t.shift_number AND shop_number = :shop AND cash_number = :cash\n" +
            "       ORDER BY id DESC \n" +
            "       LIMIT 1), \n" +
            "       (SELECT shift_number\n" +
            "       FROM loy_transaction \n" +
            "       WHERE id < t.id AND shift_number <> t.shift_number AND shop_number = :shop AND cash_number = :cash\n" +
            "       ORDER BY id DESC \n" +
            "       LIMIT 1), \n" +
            "       n+1 AS n\n" +
            "   FROM t\n" +
            "   WHERE  n < :shiftCount)\n" +
            "SELECT sale_time\n" +
            "FROM     loy_transaction\n" +
            "WHERE id = (SELECT id FROM t WHERE n = :shiftCount)";
    /**
     * Этот PSQL-запрос вернет дату и время самой "свежей" TX лояльности, что следует выкинуть из оперативного справочника.
     * Т.е., записи, имеющие {@link LoyTransactionEntity#getSaleTime() дату фискализации} не "свежее" данной признаются уже не нужными для хранения в БД кассы.
     * Параметры запроса:
     * <og>
     * <li> shop - номер магазина, которой принадлежит данная касса;
     * <li> cash - номер данной кассы;
     * <li> shiftCount - максимальное количество последних смен, TX лояльности которых не должны удаляться;
     * <li> inn - INN основного юрлица
     * </og>
     */
    // @formatter:off
    private static final String GET_FIRST_STALE_RECORD_TIME_INN = "WITH RECURSIVE t AS (\n" +
            "   SELECT id, shift_number, 1  as n\n" +
            "   FROM    loy_transaction\n" +
            "   WHERE id = (SELECT max(id) FROM loy_transaction WHERE shop_number = :shop AND cash_number = :cash AND (inn IS NULL OR inn = :inn))\n" +
            "   UNION ALL\n" +
            "   SELECT (SELECT id\n" +
            "       FROM loy_transaction\n" +
            "       WHERE id < t.id AND shift_number <> t.shift_number AND shop_number = :shop AND cash_number = :cash AND (inn IS NULL OR inn = :inn)\n" +
            "       ORDER BY id DESC\n" +
            "       LIMIT 1),\n" +
            "       (SELECT shift_number\n" +
            "       FROM loy_transaction\n" +
            "       WHERE id < t.id AND shift_number <> t.shift_number AND shop_number = :shop AND cash_number = :cash AND (inn IS NULL OR inn = :inn)\n" +
            "       ORDER BY id DESC\n" +
            "       LIMIT 1),\n" +
            "       n+1 AS n\n" +
            "   FROM t\n" +
            "   WHERE  n < :shiftCount)\n" +
            "SELECT sale_time\n" +
            "FROM     loy_transaction\n" +
            "WHERE id = (SELECT id FROM t WHERE n = :shiftCount)";
    // @formatter:on
    /**
     * Этот PSQL-запрос удалит устаревшие TX лояльности из БД (если есть и "свои" устаревшие TX).
     * Параметры запроса:
     * <og>
     * <li> staleTime - дата и время самой "свежей" TX лояльности, что можно удалить из БД;
     * <li> limit - максимальное количество записей, что можно удалить за одну итерацию.
     * </og>
     */
    // @formatter:off
    private static final String REMOVE_STALE_TXES_QUERY = "DELETE\n" +
            "FROM loy_transaction\n" +
            "WHERE id IN (SELECT id\n" +
            "   FROM loy_transaction\n" +
            // статус 2 - TX уже отправлена на сервер, 4 - TX была получена с сервера (для чеков возврата)
            "   WHERE sent_to_server_status = 4 OR (sent_to_server_status = 2 AND sale_time <= :staleTime)\n" +
            "   LIMIT :limit)";
    // @formatter:on
    /**
     * Этот PSQL-запрос удалит устаревшие TX лояльности из БД (если "своих" устаревших TX нет, но хочется удалить "чужие" TX (с других касс)).
     * Параметры запроса:
     * <og>
     * <li> limit - максимальное количество записей, что можно удалить за одну итерацию.
     * </og>
     */
    // @formatter:off
    private static final String REMOVE_ALIEN_TXES_QUERY = "DELETE\n" +
            "FROM loy_transaction\n" +
            "WHERE id IN (SELECT id\n" +
            "   FROM loy_transaction\n" +
            // статус 4 - TX была получена с сервера (для чеков возврата)
            "   WHERE sent_to_server_status = 4\n" +
            "   LIMIT :limit)";
    // @formatter:on
    /**
     * Этот ANSI-SQL запрос установит TX лояльности, уже записанные в указанные файлы, статусы в "не отправленные" на сервер
     */
    private static final String MARK_LOY_TXES_FOR_RESENDING = "UPDATE loy_transaction SET sent_to_server_status = 0, filename = NULL WHERE filename IN (:filenames)";
    // @formatter:on
    /**
     * Этот HQL скрипт позволит нам вытащить из БД {@link LoyTransactionEntity TX лояльности} вместе со всеми полями-колелкциями по ID этой TX.
     */
    private static final String SELECT_LOY_TX_BY_ID = "SELECT t FROM LoyTransactionEntity AS t WHERE t.id = :id";
    /**
     * Этот ANSI SQL скрипт позволит нам "привязывать" чек к транзакции лояльности
     */
    private static final String LINK_TX_TO_RECEIPT_ANSI_SQL_QUERY = "UPDATE loy_transaction " +
            "SET purchase_number = :purchaseNumber, sale_time = :saleTime, status = :status, " +
            "sent_to_server_status = :sent_to_server_status, shift_number = :shiftNumber, " +
            "cash_number = :cashNumber, shop_number = :shopNumber " +
            "WHERE id = :id";
    /**
     * Этот HQL-запрос позволит нам удалять из БД {@link LoyTransactionEntity TX лояльности} по их {@link LoyTransactionEntity#getId()
     * идентификаторам}
     */
    private static final String DELETE_LOY_TXES_BY_IDS = "DELETE FROM LoyTransactionEntity t WHERE t.id IN (:ids)";
    /**
     * Этот HQL-запрос позволит нам удалять из БД {@link LoyBonusSberbankTransactionEntity транзакции списания/начисления бонусов "СоСБ"} по их
     * {@link LoyBonusSberbankTransactionEntity#getId() идентификаторам}
     */
    private static final String DELETE_SOSB_LOY_TXES_BY_IDS = "DELETE FROM LoyBonusSberbankTransactionEntity t WHERE t.id IN (:ids)";
    /**
     * Название поля, что хранит {@link LoyTransactionEntity#getSentToServerStatus() статус отправки TX на сервер}
     */
    private static final String SENT_TO_SERVER_STATUS_FIELD_NAME = "sentToServerStatus";
    /**
     * Этот HQL-запрос позволит нам удалять из БД {@link LoyDiscountPositionEntity позиционные скидки} по их {@link LoyDiscountPositionEntity#getId() идентификаторам}
     */
    private static final String DELETE_POSITION_DISCOUNTS_BY_IDS = "DELETE FROM LoyDiscountPositionEntity l WHERE l.id IN (:ids)";


    // injected
    /**
     * Этот HQL-запрос позволит нам обновить размер скидки и сумму чека у TX лояльности
     */
    private static final String UPDATE_LOY_TX_DISCOUNT_VALUE = "UPDATE LoyTransactionEntity l SET l.discountValueTotal = :dv, l.purchaseAmount = :pa WHERE l.id = :id";

    // injected
    /**
     * черный ящик
     */
    private static Logger log = LoggerFactory.getLogger(HibernateBackedLoyTxDao.class);

    public HibernateBackedLoyTxDao() {
        super(log);
    }

    @Override
    public LoyTransactionEntity saveLoyTx(LoyTransactionEntity loyTx) {
        LoyTransactionEntity result = null;
        long stopWatch = System.currentTimeMillis();

        log.trace("entering saveLoyTx(LoyTransactionEntity). The argument is: loyTx [{}]", loyTx);

        if (loyTx != null) {
            Transaction tx = null;
            try {
                Session session = getSession();
                tx = session.beginTransaction();
                result = (LoyTransactionEntity) session.merge(loyTx);
                setupParents(result);
                tx.commit();
            } catch (Exception e) {
                log.error(String.format("Failed to save loy-tx: %s", loyTx), e);

                if (tx != null) {
                    tx.rollback();
                }
            }
        } else {
            log.warn("saveLoyTx: the argument is NULL!");
        }
        log.trace("leaving saveLoyTx(LoyTransactionEntity). The result is: {}; it took {} [ms]",
                result, System.currentTimeMillis() - stopWatch);

        return result;
    }

    /**
     * Хак для следующей ситуации:<br>
     * - создали транзакцию лояльности;<br>
     * - в неё налепили всяких бонусных транзакций;<br>
     * - а родителя этим транзакциям не проставили;<br>
     * - у всякого экземпляра такой транзакции должна быть ссылка на родительскую транзакцию, но её ещё не может быть, потому что транзакция сохранена не была;<br>
     * - подчиненные транзакции так и сохранились без родителя и, что хужее всего, после сохранения обратно в таком виде же и вернулись.
     * Чтобы такого больше не происходило, принудительно расставляем подчиненным транзакциям родителя.
     * Подробности: https://stackoverflow.com/a/15402095
     *
     * @param t транзакиця, подчиненным транзакциям которой необходимо проставить родителя
     */
    private void setupParents(LoyTransactionEntity t) {
        for (LoyBonusTransactionEntity b : t.getBonusTransactions()) {
            if (b.getTransaction() == null) {
                b.setTransaction(t);
            }
        }
        for (LoySetApiPluginTransactionEntity b : t.getSetApiLoyaltyTransactions()) {
            if (b.getTransaction() == null) {
                b.setTransaction(t);
            }
        }
        for (LoyTokenSiebelTransactionEntity b : t.getTokensSiebelTransactions()) {
            if (b.getTransaction() == null) {
                b.setTransaction(t);
            }
        }
        for (LoyBonusDiscountTransactionEntity b : t.getBonusDiscountTransactions()) {
            if (b.getTransaction() == null) {
                b.setTransaction(t);
            }
        }
        for (LoyBonusPlastekTransactionEntity b : t.getBonusPlastekTransactions()) {
            if (b.getTransaction() == null) {
                b.setTransaction(t);
            }
        }
        for (LoyBonusSberbankTransactionEntity b : t.getBonusSberbankTransactions()) {
            if (b.getTransaction() == null) {
                b.setTransaction(t);
            }
        }
        for (LoyGiftNoteEnity b : t.getGiftNotes()) {
            if (b.getTransaction() == null) {
                b.setTransaction(t);
            }
        }
        for (LoyChequeAdvertiseEntity b : t.getChequeAdverts()) {
            if (b.getTransaction() == null) {
                b.setTransaction(t);
            }
        }
    }

    @Override
    public LoyTransactionEntity discardDiscountsAndSave(LoyTransactionEntity loyTx, Collection<LoyDiscountPositionEntity> positionDiscounts,
                                                        Collection<LoyDiscountPositionEntity> correctDiscounts) {

        log.trace("entering discardDiscountsAndSave(LoyTransactionEntity, Collection). The arguments are: loyTx [{}], positionDiscounts [{}]", loyTx, positionDiscounts);

        if (loyTx == null) {
            log.error("leaving discardDiscountsAndSave(LoyTransactionEntity, Collection). The \"loyTx\" argument is NULL");
            return null;
        }

        if (CollectionUtils.isEmpty(positionDiscounts)) {
            log.warn("leaving discardDiscountsAndSave(LoyTransactionEntity, Collection). The \"positionDiscounts\" argument is EMPTY");
            return loyTx;
        }

        // 1. эти скидки на позиции надо физически удалить:
        Set<Long> positionIds = new HashSet<>();
        for (LoyDiscountPositionEntity dpe : positionDiscounts) {
            if (dpe != null && dpe.getId() != null) {
                positionIds.add(dpe.getId());
            }
        }

        if (positionIds.isEmpty()) {
            // видимо эти скидки еще не в БД
            log.warn("leaving discardDiscountsAndSave(LoyTransactionEntity, Collection). The \"positionDiscounts\" argument is INVALID (no IDs)");
            return loyTx;
        }

        Transaction tx = null;
        try {
            Session session = getSession();
            tx = session.beginTransaction();

            Query q = session.createQuery(DELETE_POSITION_DISCOUNTS_BY_IDS);
            q.setParameterList("ids", positionIds);

            int removed = q.executeUpdate();
            log.trace("{} position discounts were removed", removed);

            tx.commit();
        } catch (Exception e) {
            log.error(String.format("discardDiscountsAndSave: failed to remove position-discounts [ids: %s]", positionIds), e);
            if (tx != null) {
                tx.rollback();
            }
            throw new RuntimeException("position discounts removal failed!", e);
        }

        // 2. надо обновить нашу TX лояльности в БД:
        loyTx.getDiscountPositions().removeAll(positionDiscounts);

        // надо откорректировать discountValueTotal и purchaseAmount
        long discountDelta = loyTx.getDiscountValueTotal();
        for (LoyDiscountPositionEntity dpe : loyTx.getDiscountPositions()) {
            if (dpe != null) {
                discountDelta -= dpe.getDiscountAmount();
            }
        }
        if (correctDiscounts != null) {
            discountDelta -= correctDiscounts.stream().mapToLong(LoyDiscountPositionEntity::getDiscountAmount).sum();
        }
        loyTx.setDiscountValueTotal(loyTx.getDiscountValueTotal() - discountDelta);
        loyTx.setPurchaseAmount(loyTx.getPurchaseAmount() + discountDelta);

        // и замержить, что получилось
        tx = null;
        try {
            Session session = getSession();
            tx = session.beginTransaction();

            Query q = session.createQuery(UPDATE_LOY_TX_DISCOUNT_VALUE);
            q.setParameter("dv", loyTx.getDiscountValueTotal());
            q.setParameter("pa", loyTx.getPurchaseAmount());
            q.setParameter("id", loyTx.getId());

            int updated = q.executeUpdate();
            log.trace("{} loy-txs were updated", updated);

            tx.commit();
        } catch (Exception e) {
            log.error(String.format("discardDiscountsAndSave: failed to update loy-tx [%s]", loyTx), e);
            if (tx != null) {
                tx.rollback();
            }
            throw new RuntimeException("position discounts removal failed!", e);
        }

        // добавим корректирующую скидку
        addDiscountPositions(loyTx, correctDiscounts);

        log.trace("leaving discardDiscountsAndSave(LoyTransactionEntity, Collection). The result is: {}", loyTx);

        return loyTx;
    }

    @SuppressWarnings("unchecked")
    private void addDiscountPositions(LoyTransactionEntity loyTx, Collection<LoyDiscountPositionEntity> discountPositions) {
        if (CollectionUtils.isNotEmpty(discountPositions)) {
            Transaction tx = null;
            try {
                Session session = getSession();
                tx = session.beginTransaction();
                Query q = session.createQuery(SELECT_LOY_TX_BY_ID);
                q.setParameter("id", loyTx.getId());
                List<LoyTransactionEntity> extracted = q.list();
                if (extracted != null && !extracted.isEmpty()) {
                    LoyTransactionEntity loyTxById = extracted.get(0);
                    loyTxById.getDiscountPositions().addAll(discountPositions);
                    session.merge(loyTxById);
                }
                tx.commit();
                log.trace("correction loyTx was added {}", discountPositions);
            } catch (Exception e) {
                log.error(String.format("discardDiscountsAndSave: failed to add correction loyTx [%s]", loyTx), e);
                if (tx != null) {
                    tx.rollback();
                }
                throw new RuntimeException("correction loyTx adding failed!", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public LoyTransactionEntity getLoyTxById(long loyTxId) {
        LoyTransactionEntity result = null;
        long stopWatch = System.currentTimeMillis();

        log.trace("entering getLoyTxById(long). The argument is: loyTxId [{}]", loyTxId);

        Transaction tx = null;
        try {
            Session session = getSession();
            tx = session.beginTransaction();

            Query q = session.createQuery(SELECT_LOY_TX_BY_ID);
            q.setParameter("id", loyTxId);
            List<LoyTransactionEntity> extracted = q.list();
            log.trace("{} loy-txes were extracted", extracted == null ? null : extracted.size());
            if (extracted != null && !extracted.isEmpty()) {
                if (extracted.size() > 1) {
                    log.error("getLoyTxById: the DB is INCONSISTENT: several [{}] loy-txes having the same ID [{}] were detected in the DB! " +
                            "The first (arbitrary!) one will be treated as the result...", extracted.size(), loyTxId);
                }

                result = extracted.get(0);

                // а теперь поля-коллекции вытянем. И какого ж хера они все List'ами реализованы? Hibernate не может 2 листа left join fetch сделать
                // для избежания lazy initialization
                pullCollections(result);
            } else {
                // да нормально
                log.trace("Seems, that loy-TX having id [{}] does not exist", loyTxId);
            }

            tx.commit();
        } catch (Exception e) {
            log.error(String.format("failed to retrieve loy-tx by id: %s", loyTxId), e);
            if (tx != null) {
                tx.rollback();
            }
        }

        log.trace("leaving getLoyTxById(long). The result is: {}; it took {} [ms]",
                result, System.currentTimeMillis() - stopWatch);

        return result;
    }

    @Override
    public LoyTransactionEntity linkTxToReceipt(LoyTransactionEntity loyTx, PurchaseEntity receipt) {
        LoyTransactionEntity result = null;
        long stopWatch = System.currentTimeMillis();

        log.trace("entering linkTxToReceipt(LoyTransactionEntity, PurchaseEntity). The arguments are: loyTx [{}], receipt [{}]", loyTx, receipt);

        if (loyTx == null) {
            log.error("leaving linkTxToReceipt(LoyTransactionEntity, PurchaseEntity). The \"loyTx\" argument is NULL");
            return null;
        }

        if (receipt == null) {
            log.error("leaving linkTxToReceipt(LoyTransactionEntity, PurchaseEntity). The \"receipt\" argument is NULL");
            return null;
        }

        if (loyTx.getId() == null) {
            log.error("leaving linkTxToReceipt(LoyTransactionEntity, PurchaseEntity). The \"loyTx\" is NOT persisted (yet)!");
            return null;
        }

        if (!allowsLinkageToLoyTx(receipt)) {
            // неожиданно. Вернем NULL
            log.warn("leaving linkTxToReceipt(LoyTransactionEntity, PurchaseEntity). Not all receipt's fields are set: " +
                    "TX [{}], receipt [{}]", loyTx, receipt);
            return null;
        }

        // аргументы валидны. работаем:
        // 1. Обновим запись TX в БД
        Transaction tx = null;
        try {
            Session session = getSession();
            tx = session.beginTransaction();

            Query q = session.createSQLQuery(LINK_TX_TO_RECEIPT_ANSI_SQL_QUERY);
            q.setParameter("purchaseNumber", receipt.getNumber());
            q.setParameter("saleTime", receipt.getDateCommit());
            q.setParameter("status", loyTx.getStatus());
            q.setParameter("sent_to_server_status", SentToServerStatus.NO_SENT.ordinal());
            q.setParameter("shiftNumber", receipt.getShift().getNumShift());
            q.setParameter("cashNumber", receipt.getShift().getCashNum());
            q.setParameter("shopNumber", receipt.getShift().getShopIndex());
            q.setParameter("id", loyTx.getId());

            int updated = q.executeUpdate();
            if (updated == 1) {
                // успех. эта TX (с этим ID) была в БД и мы ее обновили
                result = loyTx;
                tx.commit();
            } else {
                // неожиданно. неужели этой TX с такой ID нет в БД?! Вернем NULL
                log.warn("linkTxToReceipt: Seems NO ONE loy-tx having id: [{}] was found in the DB, 'cause [{}] rows were updated!",
                        loyTx.getId(), updated);

                // операция не удалась все равно. но откатим
                tx.rollback();
            }
        } catch (Exception e) {
            log.error(String.format("linkTxToReceipt: failed to link TX [%s] to receipt [%s]", loyTx, receipt), e);
            result = null;

            if (tx != null) {
                tx.rollback();
            }
        }

        // 2. в случае успеха отредактируем первый аргумент и вернем его как результат:
        if (result != null) {
            result.setPurchaseNumber(receipt.getNumber());
            result.setSaleTime(receipt.getDateCommit());
            result.setShiftNumber(receipt.getShift().getNumShift());
            result.setCashNumber(receipt.getShift().getCashNum());
            result.setShopNumber(receipt.getShift().getShopIndex());
            result.setInn(receipt.getInn());
            result.setSentToServerStatus(SentToServerStatus.NO_SENT);
        }

        log.trace("leaving linkTxToReceipt(LoyTransactionEntity, PurchaseEntity). " +
                "The result is: {}; it took {} [ms]", result, System.currentTimeMillis() - stopWatch);

        return result;
    }

    /**
     * Вернет {@code true}, если указанный чек можно (т.е. у него есть все необходимые поля для идентификации себя по этим вторичным признакам:
     * номер, дата фискализации, смена, касса, магазин) привязать к TX лояльности.
     *
     * @param receipt чек, что проверяем
     * @return {@code false}, если аргумент невалиден
     */
    private boolean allowsLinkageToLoyTx(PurchaseEntity receipt) {
        if (receipt == null) {
            return false;
        }
        return !(receipt.getNumber() == null || receipt.getShift() == null || receipt.getShift().getNumShift() == null || receipt.getDateCommit() == null);
    }

    @Override
    public LoyAdvActionInPurchaseEntity getLoyAdvActionByGuid(long guid) {
        LoyAdvActionInPurchaseEntity result = null;
        long stopWatch = System.currentTimeMillis();

        log.trace("entering getLoyAdvActionByGuid(long). The argument is: guid [{}]", guid);

        Transaction tx = null;
        try {
            Session session = getSession();
            tx = session.beginTransaction();
            result = (LoyAdvActionInPurchaseEntity) getSession().get(LoyAdvActionInPurchaseEntity.class, guid);
            tx.commit();
        } catch (Exception e) {
            log.error(String.format("Failed to extract loy-adv-action by guid: %s", guid), e);

            if (tx != null) {
                tx.rollback();
            }
        }

        log.trace("leaving getLoyAdvActionByGuid(long). The result is: {}; it took {} [ms]",
                result, System.currentTimeMillis() - stopWatch);

        return result;
    }

    @Override
    public LoyAdvActionInPurchaseEntity saveLoyAdvAction(LoyAdvActionInPurchaseEntity loyAdvAction) {
        LoyAdvActionInPurchaseEntity result = null;
        long stopWatch = System.currentTimeMillis();

        log.trace("entering saveLoyAdvAction(LoyAdvActionInPurchaseEntity). The argumetn is: loyAdvAction [{}]", loyAdvAction);

        if (loyAdvAction != null && loyAdvAction.getGuid() != null) {
            Transaction tx = null;
            try {
                Session session = getSession();
                tx = session.beginTransaction();
                result = (LoyAdvActionInPurchaseEntity) session.merge(loyAdvAction);
                tx.commit();
            } catch (Exception e) {
                log.error(String.format("Failed to save loy-adv-action: %s", loyAdvAction), e);

                if (tx != null) {
                    tx.rollback();
                }
            }
        } else {
            log.warn("saveLoyAdvAction: the argument is INVALID: either NULL, or its \"GUID\" is NULL: {}; NULL will be returned!", loyAdvAction);
        }
        log.trace("leaving saveLoyAdvAction(LoyAdvActionInPurchaseEntity). The result is: {}; it took {} [ms]",
                result, System.currentTimeMillis() - stopWatch);

        return result;
    }

    @Override
    public int removeLoyTxByIds(Collection<Long> ids) {
        int result = 0;
        long stopWatch = System.currentTimeMillis();

        log.trace("entering removeLoyTxByIds(Collection). The argument is: ids [{}]", ids);
        if (ids != null && !ids.isEmpty()) {
            Transaction tx = null;
            try {
                Session session = getSession();
                tx = session.beginTransaction();

                Query q = session.createQuery(DELETE_LOY_TXES_BY_IDS);
                q.setParameterList("ids", ids);

                result = q.executeUpdate();

                tx.commit();
            } catch (Exception e) {
                log.error(String.format("removeLoyTxByIds(Collection): failed to remove loy-txes [ids: %s]", ids), e);

                if (tx != null) {
                    tx.rollback();
                }
            }
        } else {
            log.warn("removeLoyTxByIds(Collection): the argument is EMPTY! nothing will be done!");
        }
        log.trace("leaving removeLoyTxByIds(Collection). The result is: {}; it took {} [ms]",
                result, System.currentTimeMillis() - stopWatch);

        return result;
    }

    @Override
    public int removeStaleLoyTxes(long maxShiftsToKeep, int maxRecordsToRemoveAtOnce, long cashNumber, long shopNumber, String inn) {
        int result = 0;
        long stopWatch = System.currentTimeMillis();

        log.trace("entering removeStaleLoyTxes(long, int, long, long, String). " +
                        "The arguments are: maxShiftsToKeep [{}], maxRecordsToRemoveAtOnce [{}], cashNumber [{}], shopNumber [{}], inn [{}]",
                maxShiftsToKeep, maxRecordsToRemoveAtOnce, cashNumber, shopNumber, inn);

        if (maxShiftsToKeep <= 0) {
            log.error("leaving removeStaleLoyTxes(long, int, long, long, String). The \"maxShiftsToKeep\" [{}] argument is illegal", maxShiftsToKeep);
            return 0;
        }

        if (maxRecordsToRemoveAtOnce <= 1) {
            log.error("leaving removeStaleLoyTxes(long, int, long, long, String). The \"maxRecordsToRemoveAtOnce\" [{}] argument is illegal", maxRecordsToRemoveAtOnce);
            return 0;
        }

        // 1. для начала узнаем время самой свежей TX, что можно удалить:
        Date firstStale = getFirstStaleLoyTxSaleTime(maxShiftsToKeep, cashNumber, shopNumber, inn);

        // 2. а теперь удалим
        Transaction tx = null;
        try {
            Session session = getSession();
            tx = session.beginTransaction();

            Query q;
            if (firstStale == null) {
                // возможно, это касса возврата. Или просто новых устаревших документов не появилось. Но в любом случае удалим TX с "чужих" касс:
                log.trace("removeStaleLoyTxes: aliens-only-query:\n{}", REMOVE_ALIEN_TXES_QUERY);
                q = session.createSQLQuery(REMOVE_ALIEN_TXES_QUERY);
            } else {
                // есть и "свои" уставевшие TX
                log.trace("removeStaleLoyTxes: query:\n{}", REMOVE_STALE_TXES_QUERY);
                q = session.createSQLQuery(REMOVE_STALE_TXES_QUERY);
                q.setParameter("staleTime", firstStale);
            }
            q.setParameter("limit", maxRecordsToRemoveAtOnce);

            result = q.executeUpdate();

            tx.commit();
        } catch (Exception e) {
            log.error(String.format("removeStaleLoyTxes(long, int, long, long, String): failed! arguments were: " +
                            "maxShiftsToKeep [%s], maxRecordsToRemoveAtOnce [%s], cashNumber [%s], shopNumber [%s]",
                    maxShiftsToKeep, maxRecordsToRemoveAtOnce, cashNumber, shopNumber), e);
            if (tx != null) {
                tx.rollback();
            }
        }

        log.trace("leaving removeStaleLoyTxes(long, int, long, long, String). The result is: {}; it took {} [ms]",
                result, System.currentTimeMillis() - stopWatch);

        return result;
    }

    /**
     * Вернет дату самой "свежей" TX лояльности, созданной для основного юрлица данной кассы, что можно уже считать "устаревшей" (валидной для удаления из БД).
     *
     * @param maxShiftsToKeep максимальное количество [последних] смен, оперативная информация по которым должна оставаться на кассе
     *                        (т.е. скидки по этим последним сменам удалять нельзя ни в коем случае)
     * @param cashNumber      номер данной кассы
     * @param shopNumber      номер магазина, которому принадлежит данная касса
     * @param inn             INN основного юрлица, которому принадлежит "первый" фискальник данной кассы
     * @return {@code null}, если нет "устаревших" записей
     */
    private Date getFirstStaleLoyTxSaleTime(long maxShiftsToKeep, long cashNumber, long shopNumber, String inn) {
        Date result = null;
        long stopWatch = System.currentTimeMillis();

        log.trace("entering getFirstStaleLoyTxSaleTime(long, long, long, String). " +
                        "The arguments are: maxShiftsToKeep [{}], cashNumber [{}], shopNumber [{}], inn [{}]",
                maxShiftsToKeep, cashNumber, shopNumber, inn);

        if (maxShiftsToKeep <= 0) {
            log.error("leaving getFirstStaleLoyTxSaleTime(long, long, long, String). The \"maxShiftsToKeep\" [{}] argument is illegal", maxShiftsToKeep);
            return null;
        }

        Transaction tx = null;
        try {
            Session session = getSession();
            tx = session.beginTransaction();

            Query q;
            if (StringUtils.isEmpty(inn)) {
                // на этой кассе работают без INN как-то
                log.trace("getFirstStaleLoyTxSaleTime: no-inn-query:\n{}", GET_FIRST_STALE_RECORD_TIME);
                q = session.createSQLQuery(GET_FIRST_STALE_RECORD_TIME);
            } else {
                log.trace("getFirstStaleLoyTxSaleTime: query:\n{}", GET_FIRST_STALE_RECORD_TIME_INN);
                q = session.createSQLQuery(GET_FIRST_STALE_RECORD_TIME_INN);
                q.setParameter("inn", inn);
            }

            q.setParameter("shop", shopNumber);
            q.setParameter("cash", cashNumber);
            q.setParameter("shiftCount", maxShiftsToKeep + 1);

            result = (Date) q.uniqueResult();

            tx.commit();
        } catch (Exception e) {
            log.error(String.format("getFirstStaleLoyTxSaleTime(long, long, long, String): failed! arguments were: " +
                            "maxShiftsToKeep [%s], cashNumber [%s], shopNumber [%s], inn [%s]",
                    maxShiftsToKeep, cashNumber, shopNumber, inn), e);
            if (tx != null) {
                tx.rollback();
            }
        }

        log.trace("leaving getFirstStaleLoyTxSaleTime(long, long, long, String). The result is: {}; it took {} [ms]",
                result == null ? null : String.format("%1$tF %1$tT", result), System.currentTimeMillis() - stopWatch);

        return result;
    }


    @SuppressWarnings("unchecked")
    @Override
    public LoyTransactionEntity getLoyTxByReceipt(PurchaseEntity purchase) {
        LoyTransactionEntity result = null;
        long stopWatch = System.currentTimeMillis();

        log.trace("entering getLoyTxByReceipt(PurchaseEntity). The argument is: purchase  [{}]", purchase);

        if (purchase == null) {
            log.warn("leaving getLoyTxByReceipt(PurchaseEntity). The argument is NULL");
            return null;
        }

        if (purchase.getLoyalTransactionId() != null) {
            log.trace("loy-tx-id of the receipt is not null: {}", purchase.getLoyalTransactionId());
            result = getLoyTxById(purchase.getLoyalTransactionId());

            log.trace("leaving getLoyTxByReceipt(PurchaseEntity). The result is: {}; it took {} [ms]",
                    result, System.currentTimeMillis() - stopWatch);

            return result;
        }

        // придется TX искать по вторичным признакам:
        log.trace("loy-tx-id of the receipt [{}] IS NULL", purchase);

        Long shopNo = null;
        Long cashNo = null;
        Long shiftNo = null;
        if (purchase.getShift() != null) {
            shopNo = purchase.getShift().getShopIndex();
            cashNo = purchase.getShift().getCashNum();
            shiftNo = purchase.getShift().getNumShift();
        }
        Long docNo = purchase.getNumber();
        Boolean opType = purchase.getOperationType();

        if (!couldIdentifyLoyTx(purchase)) {
            log.warn("leaving getLoyTxByReceipt(PurchaseEntity): at least one of the mandaroty fields " +
                            "(either doc-num: {}, or operation-type: {}, or shop-num: {}, or shift-num: {}, or cash-num: {}) " +
                            "of the receipt [{}] is NULL! So, NULL will be returned!",
                    docNo, opType, shopNo, shiftNo, cashNo, purchase);
            return null;
        }

        // все необходимые поля присутствуют
        Transaction tx = null;
        try {
            Session session = getSession();
            tx = session.beginTransaction();

            Criteria criteria = getSession().createCriteria(LoyTransactionEntity.class);
            criteria.add(Restrictions.eq("shopNumber", shopNo));
            criteria.add(Restrictions.eq("purchaseNumber", docNo));
            criteria.add(Restrictions.eq("shiftNumber", shiftNo));
            criteria.add(Restrictions.eq("cashNumber", cashNo));
            criteria.add(Restrictions.eq("operationType", opType));

            List<LoyTransactionEntity> list = criteria.list();

            if (list != null && list.size() > 1) {
                log.error("several [{}] loy-txes for the receipt [shopNumber = {}; purchaseNumber = {}; " +
                                "shiftNumber = {}; cashNumber = {}; operationType = {}] were detected! " +
                                "The first (arbitrary!) one will be treated as the result...",
                        list.size(), shopNo, docNo, shiftNo, cashNo, opType);
            }

            if (list != null && !list.isEmpty()) {
                // а вот и результат:
                result = list.get(0);

                // а теперь поля-коллекции вытянем. И какого ж хера они все List'ами реализованы? Hibernate не может 2 листа left join fetch сделать
                // для избежания lazy initialization
                pullCollections(result);
            } else {
                log.trace("no on loy-TX was found for receipt [{}]", purchase);
            }

            tx.commit();
        } catch (Exception e) {
            log.error(String.format("failed to retrieve loy-tx for receipt: %s", purchase), e);
            if (tx != null) {
                tx.rollback();
            }
        }

        log.trace("leaving getLoyTxByReceipt(PurchaseEntity). The result is: {}; it took {} [ms]",
                result, System.currentTimeMillis() - stopWatch);

        return result;
    }

    /**
     * Вернет {@code true}, если в указанном чеке есть все необходимые поля для идентификации его TX лоляьности по ВТОРИЧНЫМ признакам (номер чека, смены, кассы и проч.)
     *
     * @param purchase чек, возможность нахождения TX которого по вторичным признакам выясняем
     * @return {@code false}, если аргумент невалиден
     */
    private boolean couldIdentifyLoyTx(PurchaseEntity purchase) {
        if (purchase == null) {
            return false;
        }

        if (purchase.getNumber() == null) {
            return false;
        }

        if (purchase.getOperationType() == null) {
            return false;
        }

        if (purchase.getShift() == null) {
            return false;
        }
        if (purchase.getShift().getShopIndex() == null) {
            return false;
        }
        if (purchase.getShift().getNumShift() == null) {
            return false;
        }
        // все необходимые поля есть
        return purchase.getShift().getCashNum() != null;
    }

    /**
     * Просто подтянет поля-коллекции указанной {@link LoyTransactionEntity TX лояльности} - чтоб в дальнейшем не получать
     * LazyInitializationException.
     * <p/>
     * NOTE: ясно, что этот метод надо вызывать внутри той же самой транзакции, в которой и извлекли эту сущность?
     * <p/>
     * NOTE2: никакой защиты от дурака: аргумент обязан быть вылиден; иначе тупо получите Exception.
     *
     * @param loyTx TX лояльности, чьи поля-коллекции надо подтянуть из БД
     */
    private void pullCollections(LoyTransactionEntity loyTx) {
        loyTx.prepare();
    }

    @Override
    public int deleteSberbankTransactions(Collection<Long> ids) {
        int result = 0;
        long stopWatch = System.currentTimeMillis();

        log.trace("entering deleteSberbankTransactions(Collection). The argument is: ids [{}]", ids);
        if (ids != null && !ids.isEmpty()) {
            Transaction tx = null;
            try {
                Session session = getSession();
                tx = session.beginTransaction();

                Query q = session.createQuery(DELETE_SOSB_LOY_TXES_BY_IDS);
                q.setParameterList("ids", ids);

                result = q.executeUpdate();

                tx.commit();
            } catch (Exception e) {
                log.error(String.format("deleteSberbankTransactions(Collection): failed to remove sosb-loy-txes [ids: %s]", ids), e);

                if (tx != null) {
                    tx.rollback();
                }
            }
        } else {
            log.warn("deleteSberbankTransactions(Collection): the argument is EMPTY!");
        }
        log.trace("leaving deleteSberbankTransactions(Collection). The result is: {}; it took {} [ms]",
                result, System.currentTimeMillis() - stopWatch);

        return result;
    }

    @Override
    public LoyTransactionEntity saveSosbs(Collection<LoyBonusSberbankTransactionEntity> bonusTxes) {
        LoyTransactionEntity result;
        long stopWatch = System.currentTimeMillis();

        log.trace("entering saveSosbs(Collection). The argument is: bonusTxes({}): {}", bonusTxes == null ? null : bonusTxes.size(), bonusTxes);

        if (CollectionUtils.isEmpty(bonusTxes)) {
            log.warn("leaving saveSosbs(Collection). The argument is EMPTY");
            return null;
        }

        // проверим: у всех ли есть ссылка на TX лояльности и все ли они ссылаются на одну и ту же
        // 1. сначала отсеем явный "левак"
        siftOffNulls(bonusTxes);

        // 2. потом проверим. что у всех элементов есть ссылка на TX лояльности
        LoyBonusSberbankTransactionEntity invalidSosb = peekFirstSosbWithoutTxLink(bonusTxes);
        if (invalidSosb != null) {
            log.error("leaving saveSosbs(Collection). sosb-tx [{}] without reference to its loy-tx was detected! Nothing will be done!", invalidSosb);
            return null;
        }

        // 3. а остальные проверим на не-противоречивость
        result = extractLoyTx(bonusTxes);

        // теперь: если здесь result != null - значит можем продолжать: аргумент валиден
        if (result == null) {
            log.error("leaving saveSosbs(Collection). it took {} [ms]", System.currentTimeMillis() - stopWatch);
            return null;
        }

        try {
            // 1. Проверим, а есть ли эта TX уже в БД:
            LoyTransactionEntity existing = result.getId() == null ? null : getLoyTxById(result.getId());
            if (existing == null) {
                // В БД не нашли. Для профилактики прибем идентификатор
                log.trace("the loy-tx was not found by id: {}", result.getId());
                result.setId(null);
            } else {
                // В БД TX лояльности, в рамках которой эти списания делаются уже есть - ее и используем:
                log.trace("loy-tx (by id: {}) was found: {}", result.getId(), existing);
                result = existing;
            }

            // 2. Прислюнявим эти списания/начисления к результату (это из-за того, что его могли вытащить из БД):
            for (LoyBonusSberbankTransactionEntity lbste : bonusTxes) {
                result.getBonusSberbankTransactions().add(lbste);
                lbste.setTransaction(result);
            }

            // 2.1. Не забудем, что если случайно эту TX уже отправили на сервер, то надо будет переотправить:
            if (existing != null && SentToServerStatus.SENT.equals(existing.getSentToServerStatus())) {
                result.setSentToServerStatus(SentToServerStatus.NO_SENT);
            }

            // 3. И сохраним в БД:
            result = saveLoyTx(result);
        } catch (Exception t) {
            log.error(String.format("saveSosbs(Collection): failed to save sosb-loy-txes: %s", bonusTxes), t);
            result = null;
        }

        log.trace("leaving saveSosbs(Collection). The result is: {}; it took {} [ms]",
                result, System.currentTimeMillis() - stopWatch);

        return result;
    }

    private LoyTransactionEntity extractLoyTx(Collection<LoyBonusSberbankTransactionEntity> bonusTxes) {
        LoyTransactionEntity result = null;

        if (bonusTxes == null) {
            return null;
        }

        for (LoyBonusSberbankTransactionEntity lbste : bonusTxes) {
            if (result == null) {
                // это первый СоСБ:
                result = lbste.getTransaction();
            } else if (!result.equals(lbste.getTransaction())) {
                // а вот ссылается ли на тот же, что и предыдущие?
                log.error("saveSosbs(Collection): the argument is INCONSISTENT: this sosb-tx [{}] refers to another loy-tx [{}] " +
                                "than previous elements (those refer to : {}). Nothing will be done & NULL will be returned!",
                        lbste, lbste.getTransaction(), result);
                result = null;
                break;
            }
        }

        return result;
    }

    private LoyBonusSberbankTransactionEntity peekFirstSosbWithoutTxLink(Collection<LoyBonusSberbankTransactionEntity> bonusTxes) {
        if (bonusTxes == null) {
            return null;
        }
        for (LoyBonusSberbankTransactionEntity lbste : bonusTxes) {
            if (lbste.getTransaction() == null) {
                return lbste;
            }
        }
        return null;
    }

    private void siftOffNulls(Collection<LoyBonusSberbankTransactionEntity> bonusTxes) {
        if (bonusTxes == null) {
            return;
        }
        for (Iterator<LoyBonusSberbankTransactionEntity> it = bonusTxes.iterator(); it.hasNext(); ) {
            LoyBonusSberbankTransactionEntity lbste = it.next();
            if (lbste == null) {
                log.error("saveSosbs(Collection): NULL element was detected in the argument. It will be removed from further processing!");
                it.remove();
                continue;
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<LoyTransactionEntity> getLoyTxesByStatus(Collection<SentToServerStatus> statuses, int maxResults) {
        List<LoyTransactionEntity> result = new LinkedList<>();
        long stopWatch = System.currentTimeMillis();

        log.trace("entering getLoyTxesByStatus(Collection, int). The arguments are: statuses [{}], maxResults [{}]", statuses, maxResults);

        if (maxResults <= 0) {
            log.error("leaving getLoyTxesByStatus(Collection, int). The \"maxResults\" argument is non-positive [{}]", maxResults);
            return result;
        }

        if (CollectionUtils.isEmpty(statuses)) {
            log.error("leaving getLoyTxesByStatus(Collection, int). The \"statuses\" argument is EMPTY");
            return result;
        }

        // аргументы валидны - формируем запрос:
        Transaction tx = null;
        try {
            Session session = getSession();
            tx = session.beginTransaction();

            Criteria c = session.createCriteria(LoyTransactionEntity.class);

            // 1. добавим фильтр по статусам
            addStatusesCondition(statuses, c);

            // 2. упорядочивание и макс. размер выборки:
            c.addOrder(Order.desc(SENT_TO_SERVER_STATUS_FIELD_NAME));
            c.addOrder(Order.asc("saleTime"));
            c.setMaxResults(maxResults);

            // 3. ну и сама выборка:
            List<LoyTransactionEntity> extracted = c.list();
            log.trace("[{}] loy-tx records were extracted from the db", extracted == null ? null : extracted.size());

            // 4. запихнем в результат и все (с вытягиванием полей-коллекций, правда):
            if (extracted != null) {
                result = extracted;
                for (LoyTransactionEntity lte : result) {
                    pullCollections(lte);
                }
            }

            tx.commit();
        } catch (Exception e) {
            log.error(String.format("getLoyTxesByStatus(Collection, int): failed! " +
                    "The arguments were: statuses [%s], maxResults [%s]", statuses, maxResults), e);

            if (tx != null) {
                tx.rollback();
            }
        }

        log.trace("leaving getLoyTxesByStatus(Collection, int). The result is: {}; it took {} [ms]",
                result, System.currentTimeMillis() - stopWatch);

        return result;
    }

    /**
     * Добавит в указанное условие выборки (TX лояльности) фильтрацию по указанным статусам отправки TX на сервер.
     *
     * @param statuses статусы отправки TX на сервер, по которым надо делать фильтрацию
     * @param c        условие выборки, в которое добавляем фильтрацию
     */
    private void addStatusesCondition(Collection<SentToServerStatus> statuses, Criteria c) {
        if (CollectionUtils.isEmpty(statuses) || c == null) {
            return;
        }

        // надо не все подряд вернуть. а только с определенными статусами
        Set<SentToServerStatus> statusesAsSet = new HashSet<>(statuses);
        Criterion crt;
        if (statusesAsSet.contains(null)) {
            // NULL немного по-другому в условие добавляется

            statusesAsSet.remove(null);
            crt = Restrictions.isNull(SENT_TO_SERVER_STATUS_FIELD_NAME);

            if (!statusesAsSet.isEmpty()) {
                crt = Restrictions.or(crt, Restrictions.in(SENT_TO_SERVER_STATUS_FIELD_NAME, statusesAsSet));
            }
        } else {
            // просто с указанными статусами:
            crt = Restrictions.in(SENT_TO_SERVER_STATUS_FIELD_NAME, statusesAsSet);
        }

        // и добавим этот фильтр по статусам
        c.add(crt);
    }

    @Override
    public int markLoyTxesAsNotSent(Collection<String> fileNames) {
        int result = 0;
        long stopWatch = System.currentTimeMillis();

        log.trace("entering markLoyTxesAsNotSent(Collection). The argument is: fileNames({}): [{}]",
                fileNames == null ? null : fileNames.size(), fileNames);
        if (fileNames != null && !fileNames.isEmpty()) {
            Transaction tx = null;
            try {
                Session session = getSession();
                tx = session.beginTransaction();

                Query q = session.createSQLQuery(MARK_LOY_TXES_FOR_RESENDING);
                q.setParameterList("filenames", fileNames);

                result = q.executeUpdate();

                tx.commit();
            } catch (Exception e) {
                log.error(String.format("markLoyTxesAsNotSent(Collection): failed! arguments were: fileNames [%s]", fileNames), e);
                if (tx != null) {
                    tx.rollback();
                }
            }
        } else {
            log.warn("markLoyTxesAsNotSent(Collection): the argument is EMPTY! Nothing will be done!");
        }
        log.trace("leaving markLoyTxesAsNotSent(Collection). The result is: {}; it took {} [ms]",
                result, System.currentTimeMillis() - stopWatch);

        return result;
    }

    @Override
    public int setTxStatus(Collection<Long> ids, String fileName, SentToServerStatus status) {
        int result = 0;
        long stopWatch = System.currentTimeMillis();

        log.trace("entering setTxStatus(Collection, String, SentToServerStatus). The argumetns are: ids ({}): {}; fileName: {}; status: {}",
                ids == null ? null : ids.size(), ids, fileName, status);

        if (CollectionUtils.isEmpty(ids)) {
            log.error("leaving setTxStatus(Collection, String, SentToServerStatus). The \"ids\" argument is EMPTY! Nothing will be done");
            return result;
        }

        // 1. сначала сформируем сам запрос
        StringBuilder filter = getWhereClause(ids, fileName);
        String queryAsString = "UPDATE loy_transaction \nSET sent_to_server_status = :status" + filter.toString();
        log.trace("query as string: {}", queryAsString);

        // 2. Заготовим Query:
        Transaction tx = null;
        try {
            Session session = getSession();
            tx = session.beginTransaction();

            Query q = session.createSQLQuery(queryAsString);
            q.setParameter("status", status == null ? null : status.ordinal());
            // есть фильтр по ID'шникам:
            q.setParameterList("ids", ids);
            if (!StringUtils.isEmpty(fileName)) {
                // есть фильтр по имени файла
                q.setParameter("fileName", fileName);
            }

            // 3. Ну и выполним сам UPDATE:
            result = q.executeUpdate();

            tx.commit();
        } catch (Exception e) {
            log.error(String.format("setTxStatus(Collection, String, SentToServerStatus): failed! " +
                            "The arguments were: ids: %s; fileName: %s; status: %s",
                    ids, fileName, status), e);
            if (tx != null) {
                tx.rollback();
            }
        }

        log.trace("leaving setTxStatus(Collection, String, SentToServerStatus). The result is: {}; it took {} [ms]",
                result, System.currentTimeMillis() - stopWatch);

        return result;
    }

    private StringBuilder getWhereClause(Collection<Long> ids, String fileName) {
        StringBuilder filter = new StringBuilder();
        if (ids != null) {
            // есть фильтр по ID'шникам:
            filter.append(" \nWHERE id IN (:ids)");
        }
        if (!StringUtils.isEmpty(fileName)) {
            // есть фильтр по имени файла
            if (filter.length() == 0) {
                filter.append(" \nWHERE filename = :fileName");
            } else {
                filter.append(" AND filename = :fileName");
            }
        }
        return filter;
    }

    @Override
    public int setTxStatusAndFileNames(Collection<Long> ids, String fileName, SentToServerStatus status) {
        int result = 0;
        long stopWatch = System.currentTimeMillis();

        log.trace("entering setTxStatusAndFileNames(Collection, String, SentToServerStatus). The argumetns are: ids ({}): {}; fileName: {}; status: {}",
                ids == null ? "(NULL)" : ids.size(), ids, fileName, status);

        if (CollectionUtils.isEmpty(ids)) {
            log.error("leaving setTxStatusAndFileNames(Collection, String, SentToServerStatus). The \"ids\" argument is EMPTY! Nothing will be done");
            return result;
        }

        // 1. сначала сформируем сам запрос
        // есть фильтр по ID'шникам:
        String queryAsString = "UPDATE loy_transaction SET sent_to_server_status = :status, filename = :fileName WHERE id IN (:ids)";
        log.trace("query as string: {}", queryAsString);

        // 2. Заготовим Query:
        Transaction tx = null;
        try {
            Session session = getSession();
            tx = session.beginTransaction();

            Query q = session.createSQLQuery(queryAsString);
            q.setParameter("status", status == null ? null : status.ordinal());
            q.setParameter("fileName", fileName);
            // есть фильтр по ID'шникам:
            q.setParameterList("ids", ids);

            // 3. Ну и выполним сам UPDATE:
            result = q.executeUpdate();

            tx.commit();
        } catch (Exception e) {
            log.error(String.format("setTxStatusAndFileNames(Collection, String, SentToServerStatus): failed! " +
                    "The arguments were: ids: %s; fileName: %s; status: %s", ids, fileName, status), e);
            if (tx != null) {
                tx.rollback();
            }
        }

        log.trace("leaving setTxStatusAndFileNames(Collection, String, SentToServerStatus). The result is: {}; it took {} [ms]",
                result, System.currentTimeMillis() - stopWatch);

        return result;
    }

}
