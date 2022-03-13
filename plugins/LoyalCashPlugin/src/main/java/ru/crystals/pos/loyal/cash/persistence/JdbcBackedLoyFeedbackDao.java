package ru.crystals.pos.loyal.cash.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import ru.crystals.discount.processing.entity.FeedbackAnnulmentStrategy;
import ru.crystals.discount.processing.entity.FeedbackTime;
import ru.crystals.discount.processing.entity.LoyExtProviderFeedback;
import ru.crystals.discount.processing.entity.LoyMultipleFeedbackResolutionStrategy;
import ru.crystals.pos.datasource.jdbc.JDBCMapper;
import ru.crystals.pos.datasource.jdbc.JDBCMapperDS;
import ru.crystals.pos.datasource.jdbc.JDBCMapperDSImpl;
import ru.crystals.pos.datasource.jdbc.JDBCMapperImpl;
import ru.crystals.pos.datasource.jdbc.TransactionHandler;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * JDBC реализация {@link LoyFeedbackDao}.
 *
 * @author aperevozchikov
 */
public class JdbcBackedLoyFeedbackDao extends AbstractFactoryBean<LoyFeedbackDao> implements LoyFeedbackDao {
    private static final String GET_TRANSACTIONS_BY_PROVIDER_AND_TIME = "SELECT * FROM loy_feedback WHERE provider_id = ? AND feedback_time = ?";
    private static final String LIMIT = " LIMIT ?";
    private static final String GET_TRANSACTION_BY_PROVIDER_AND_CHEQUE = "SELECT * FROM loy_feedback WHERE provider_id = ? AND receipt_id = ?";
    private static final String ORDER_BY_ID_DESC = " ORDER BY receipt_id DESC";
    private static Logger log = LoggerFactory.getLogger(JdbcBackedLoyFeedbackDao.class);

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
    private int connectionPoolSize = 4;

    private JDBCMapper jdbcMapper = null;

    public JdbcBackedLoyFeedbackDao() {
    }

    public JdbcBackedLoyFeedbackDao(DataSource ds) {
        this.jdbcMapper = new JDBCMapperImpl(ds, true);
    }

    /**
     * Создание прокси instance. Значния для коннекта к БД будут взяты дефолтные.
     * @return instance
     * @throws Exception если что-то пошло не так
     */
    @Override
    protected LoyFeedbackDao createInstance() throws Exception {
        return TransactionHandler.buildTransactionHandler(this, getJdbcMapper(), LoyFeedbackDao.class);
    }

    @Override
    public Class<?> getObjectType() {
        return LoyFeedbackDao.class;
    }

    public JDBCMapper getJdbcMapper() {
        if (jdbcMapper != null) {
            return jdbcMapper;
        }
        log.info("creating jdbcMapper...");
        try {
            jdbcMapper = createNewJDBCMapper(sqlDriverClass, dbUrl, login, password, connectionPoolSize);
        } catch (Throwable t) {
            log.error("failed to create jdbc mapper!", t);
        }
        return jdbcMapper;
    }

    JDBCMapper createNewJDBCMapper(String sqlDriverClass, String dbUrl, String login, String password, int connectionPoolSize) {
        JDBCMapperDS ds = new JDBCMapperDSImpl(sqlDriverClass, dbUrl, login, password, connectionPoolSize);
        return new JDBCMapperImpl(ds, true);
    }

    /**
     * Инициализация: то, что в энтерпрайзе аннотируется как @PostConstruct, а в Spring'е указывается значением атрибута init-method:этот метод
     * следует вызывать до вызова любого бизнес-метода
     */
    public void postConstruct() {
        log.trace("entering postConstruct()");

        log.trace("leaving postConstruct()");
    }

    @Override
    public synchronized Long saveOrUpdate(LoyExtProviderFeedback feedback) {
        log.trace("entering saveOrUpdate(LoyExtProviderFeedback). The argument is: {}", feedback);

        JDBCMapper jm = getJdbcMapper();
        if (jm == null) {
            log.error("leaving saveOrUpdate(LoyExtProviderFeedback). Failed to obtain jdbcMapper");
            return null;
        }

        LoyExtProviderFeedback dbFeedback = null;
        if(feedback.getMultipleFeedbackResolutionStrategy() == null || feedback.getMultipleFeedbackResolutionStrategy() == LoyMultipleFeedbackResolutionStrategy.UPDATE_EXISTING) {
            List<LoyExtProviderFeedback> foundFeedbacks = getFeedbacks(
                    feedback.getDocNumber(),
                    feedback.getShiftNumber(),
                    feedback.getCashNumber(),
                    feedback.getShopNumber(),
                    feedback.getProviderId(),
                    feedback.getProcessingName(),
                    feedback.getFeedbackTime()
            );
            if(!foundFeedbacks.isEmpty()) {
                dbFeedback = foundFeedbacks.get(0);
                for(int i = 1; i < foundFeedbacks.size(); ++i) {
                    remove(foundFeedbacks.get(i));
                }
            }
        }

        if (dbFeedback != null) {
            feedback.setId(dbFeedback.getId());
        }

        jm.replace(feedback);
        Long result = feedback.getId();

        log.trace("leaving saveOrUpdate(LoyExtProviderFeedback). The result is: {}", result);

        return result;
    }

    @Override
    public synchronized List<LoyExtProviderFeedback> getFeedbacks(long documentNum, long shiftNum, long cashNum, long shopNum, String providerName, String processingName, FeedbackTime sendingStrategy) {
        log.trace("inside getFeedbacks({}, {}, {}, {}, \"{}\", {})", documentNum, shiftNum, cashNum, shopNum, providerName, sendingStrategy);
        List<LoyExtProviderFeedback> feedbacks = new ArrayList<>();
        JDBCMapper jdbcMapper = getJdbcMapper();
        if(jdbcMapper == null) {
            log.error("Failed to obtain an instance of JDBC mapper. Leaving getFeedbacks()");
            return feedbacks;
        }
        // Сортировка здесь ВАЖНА, потому что внешняя система может быть чувствительна к порядку следования фидбеков.
        // Конечно, это не гарантирует, что фидбеки придут именно в таком порядке, просто как дополнительная помощь.
        // А именно по id сортируем потому что сортировка по времени недостаточно точная: фидбеки могут создаться настолько быстро,
        // что таймстамп не успеет обновиться. А последовательность успеет всегда.
        feedbacks = jdbcMapper.selectListByCondition(LoyExtProviderFeedback.class, "WHERE shop_number=:shopNumber "
                + "AND cash_number=:cashNumber "
                + "AND shift_number=:shiftNumber "
                + "AND doc_number=:docNumber "
                + "AND provider_id=:providerName "
                + (processingName != null ? "AND processing_name=:processingName " : "AND processing_name IS NULL ")
                + "AND feedback_time=:feedbackTime ORDER BY id", shopNum, cashNum, shiftNum, documentNum, providerName, processingName, sendingStrategy);
        log.trace("Leaving getFeedbacks with results {}", feedbacks);
        return feedbacks;
    }

    @Override
    public synchronized LoyExtProviderFeedback getFeedbackByPK(long documentNum, long shiftNum, long cashNum, long shopNum, String providerName, FeedbackTime feedbackTime) {
        LoyExtProviderFeedback result = null;
        log.trace("entering getFeedbackByPK(documentNum = {}, shiftNum = {}, cashNum = {}, shopNum = {}, providerName = {}, feedbackTime = {}).",
                documentNum, shiftNum, cashNum, shopNum, providerName, feedbackTime);
        JDBCMapper jm = getJdbcMapper();
        if (jm != null) {
            result = jm.selectSingleByCondition(LoyExtProviderFeedback.class,  "WHERE shop_number=:shopNumber "
                        + "AND cash_number=:cashNumber "
                        + "AND shift_number=:shiftNumber "
                        + "AND doc_number=:docNumber "
                        + "AND provider_id=:providerName "
                        + "AND feedback_time=:feedbackTime", shopNum, cashNum, shiftNum, documentNum, providerName, feedbackTime);
        } else {
            log.error("leaving getFeedbackByPK({}). Failed to obtain jdbcMapper");
        }
        return result;
    }

    @Override
    public synchronized Collection<LoyExtProviderFeedback> getFeedbackByProviderAndTime(String providerId, FeedbackTime feedbackTime, Integer maxCount) {
        List<LoyExtProviderFeedback> result = null;
        log.trace("entering getFeedbackByProviderAndTime(providerId, maxCount). The arguments are {} {}", providerId, feedbackTime);
        JDBCMapper jm = getJdbcMapper();
        if (jm != null) {
            if (maxCount != null && maxCount.compareTo(0) > 0) {
                result = jm.selectList(LoyExtProviderFeedback.class, GET_TRANSACTIONS_BY_PROVIDER_AND_TIME + LIMIT, providerId, feedbackTime, maxCount);
            } else {
                result = jm.selectList(LoyExtProviderFeedback.class, GET_TRANSACTIONS_BY_PROVIDER_AND_TIME, providerId, feedbackTime);
            }
            log.trace("leaving getFeedbackByProviderAndTime({}). The result is: {}", providerId, result);
        } else {
            log.error("leaving getFeedbackByProviderAndTime({}). Failed to obtain jdbcMapper", providerId);
        }
        if(result != null) {
           result.sort((c1, c2) -> {
               if(c1.getDateCreate() == null || c2.getDateCreate() == null) {
                   return 0;
               }
               return c1.getDateCreate().compareTo(c2.getDateCreate());
           });
        }
        return result;
    }

    @Override
    public synchronized Collection<LoyExtProviderFeedback> getFeedbackByProviderAndChequeId(String providerId, Long chequeId) {
        if (providerId == null || chequeId == null) {
            return null;
        }
        Collection<LoyExtProviderFeedback> result = null;
        log.trace("entering getFeedbackByProviderAndChequeId(providerId, chequeId). The arguments are {} {}", providerId, chequeId);
        JDBCMapper jm = getJdbcMapper();
        if (jm != null) {
            result = jm.selectList(LoyExtProviderFeedback.class, GET_TRANSACTION_BY_PROVIDER_AND_CHEQUE, providerId, chequeId);
            log.trace("leaving getFeedbackByProviderAndChequeId({}, {}). The result is: {}", providerId, chequeId, result);
        } else {
            log.error("leaving getFeedbackByProviderAndChequeId(providerId, chequeId). The arguments are {} {}", providerId, chequeId);
        }
        return result;
    }

    @Override
    public synchronized void remove(LoyExtProviderFeedback loyExtProviderFeedback) {

        log.trace("entering remove(LoyExtProviderFeedback). The argument is: {}", loyExtProviderFeedback);

        JDBCMapper jm = getJdbcMapper();
        if (jm == null) {
            log.error("leaving remove(LoyExtProviderFeedback). Failed to obtain jdbcMapper");
            return;
        }

        jm.deleteFrom(loyExtProviderFeedback);

        log.trace("leaving remove(LoyExtProviderFeedback). The result is: {}");
    }

    @Override
    public void remove(Collection<LoyExtProviderFeedback> feedback) {
        log.trace("Inside JdbcBackedLoyFeedbackDao.remove(Collection<LoyExtProviderFeedback>)] ({} entities given)", feedback == null ? "null!" : feedback.size());
        JDBCMapper jm = getJdbcMapper();
        if(jm == null) {
            log.error("[JdbcBackedLoyFeedbackDao.remove(Collection<LoyExtProviderFeedback>)] Failed to obtain an instance of JDBCMapper");
            return;
        }
        jm.deleteAll(feedback, null);
        log.trace("Leaving JdbcBackedLoyFeedbackDao.remove(Collection<LoyExtProviderFeedback>)] ");
    }

    @Override
    public synchronized void remove(long documentNum, long shiftNum, long cashNum, long shopNum, String providerName, FeedbackTime feedbackTime) {
        log.trace("Entering remove(documentNum = {}, shiftNum = {}, cashNum = {}, shopNum = {}, providerName = {}, feedbackTime = {}).",
                documentNum, shiftNum, cashNum, shopNum, providerName, feedbackTime);
        JDBCMapper jm = getJdbcMapper();
        if (jm == null) {
            log.error("leaving remove(LoyExtProviderFeedback). Failed to obtain jdbcMapper");
            return;
        }

        jm.deleteByCondition(LoyExtProviderFeedback.class,
                "WHERE shop_number=:shopNumber "
                        + "AND cash_number=:cashNumber "
                        + "AND shift_number=:shiftNumber "
                        + "AND doc_number=:docNumber "
                        + "AND provider_id=:providerName "
                        + "AND feedback_time=:feedbackTime", shopNum, cashNum, shiftNum, documentNum, providerName, feedbackTime);

        log.trace("leaving remove(LoyExtProviderFeedback). The result is: {}");
    }

    @Override
    public synchronized void remove(long feedbackId) {
        log.trace("Entering remove(loyExtProviderFeedbackPK). The argument is: {}", feedbackId);
        JDBCMapper jm = getJdbcMapper();
        if (jm == null) {
            log.error("leaving remove(LoyExtProviderFeedback). Failed to obtain jdbcMapper");
            return;
        }

        jm.deleteByCondition(LoyExtProviderFeedback.class, "WHERE id=:feedbackId", feedbackId);

        log.trace("leaving remove(LoyExtProviderFeedback). The result is: {}");
    }

    /**
     * Удаляет фидбеки для чека с указанным идентификатором. Удаляет только те фидбеки,
     * стратегия удаления при аннулировании чека которых {@link FeedbackAnnulmentStrategy#REMOVE} или null.
     * @param id идентификатор чека, фидбеки которого требуется удалить.
     */
    @Override
    public synchronized void removeFeedbackByChequeId(long id) {
        log.trace("Inside removeFeedbackByChequeId({})", id);
        JDBCMapper jm = getJdbcMapper();
        if (jm == null) {
            log.error("leaving removeFeedbackByChequeId. Failed to obtain jdbcMapper");
            return;
        }

        jm.deleteByCondition(
                LoyExtProviderFeedback.class,
                "WHERE receipt_id=:receiptId AND (annulment_strategy IS NULL OR annulment_strategy = :annulmentStrategy)",
                id,
                FeedbackAnnulmentStrategy.REMOVE
        );
        log.trace("leaving removeFeedbackByChequeId");
    }

    @Override
    public synchronized LoyExtProviderFeedback getLastFeedbackByProviderAndTime(String providerName, FeedbackTime feedbackTime) {
        log.trace("Entering getLastFeedbackByProviderAndTime(ProviderName, FeedbackTime), arguments are {}, {}", providerName, feedbackTime);
        JDBCMapper jm = getJdbcMapper();
        if (jm == null) {
            log.error("leaving getLastFeedbackByProviderAndTime. Failed to obtain jdbcMapper");
            return null;
        }

        LoyExtProviderFeedback feedback = jm.selectSingle(LoyExtProviderFeedback.class, GET_TRANSACTIONS_BY_PROVIDER_AND_TIME + ORDER_BY_ID_DESC + LIMIT, providerName, feedbackTime, 1);

        log.trace("leaving getLastFeedbackByProviderAndTime");
        return feedback;
    }

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

    //For TEST
    void setJdbcMapper(JDBCMapper jdbcMapper) {
        this.jdbcMapper = jdbcMapper;
    }

}
