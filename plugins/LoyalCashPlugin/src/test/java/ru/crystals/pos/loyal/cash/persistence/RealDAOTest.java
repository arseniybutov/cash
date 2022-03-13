package ru.crystals.pos.loyal.cash.persistence;

import static org.junit.Assert.*;
import javax.sql.DataSource;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.crystals.discount.processing.entity.LoyExtProviderFeedback;
import ru.crystals.pos.datasource.jdbc.TransactionHandler;
import ru.crystals.test.DataBase;

/**
 *
 * @author dalex
 */
public class RealDAOTest {

    private static LoyFeedbackDao loyDAO;
    private static JdbcBackedLoyFeedbackDao dao;

    /**
     * Создаем базу для всех тестов
     *
     * @throws Exception
     */
    @BeforeClass
    public static void prepareDB() throws Exception {
        DataSource dataSource = DataBase.DISCOUNT.getDataSource();
        dao = new JdbcBackedLoyFeedbackDao(dataSource);
        loyDAO = TransactionHandler.buildTransactionHandler(dao, dao.getJdbcMapper(), LoyFeedbackDao.class);
    }

    /**
     * Чистим данные перед каждым тестом
     *
     * @throws Exception
     */
    @Before
    public void clearDB() throws Exception {
        dao.getJdbcMapper().startTransaction();
        dao.getJdbcMapper().execSQL("DELETE FROM loy_feedback");
        dao.getJdbcMapper().commitTransaction();
    }

    @Test
    public void testSaveSingleFeedback() throws Exception {
        // given
        LoyExtProviderFeedback feedback = makeFeedback();
        
        // when
        loyDAO.saveOrUpdate(feedback);

        // then
        LoyExtProviderFeedback dbFeedback = loyDAO.getLastFeedbackByProviderAndTime(feedback.getProviderId(), feedback.getFeedbackTime());
        assertNotNull(dbFeedback);
    }
    
    @Test
    public void testRewriteSingleFeedback() throws Exception {
        // given
        LoyExtProviderFeedback feedback = makeFeedback();
        
        // when
        loyDAO.saveOrUpdate(feedback);
        feedback.setId(null);
        loyDAO.saveOrUpdate(feedback);

        // then
        LoyExtProviderFeedback dbFeedback = loyDAO.getLastFeedbackByProviderAndTime(feedback.getProviderId(), feedback.getFeedbackTime());
        assertNotNull(dbFeedback);
    }
    
    private LoyExtProviderFeedback makeFeedback(){
        LoyExtProviderFeedback f = new LoyExtProviderFeedback();
        f.setDocNumber(1L);
        f.setShiftNumber(50L);
        f.setCashNumber(100L);
        f.setShopNumber(1212L);
        f.setProviderId("TEST_PROVIDER_ID");
        return f;
    }
    

}
