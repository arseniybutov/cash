package ru.crystals.pos.loyal.cash.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.postgresql.ds.PGSimpleDataSource;
import ru.crystals.discount.processing.entity.FeedbackAnnulmentStrategy;
import ru.crystals.discount.processing.entity.FeedbackTime;
import ru.crystals.discount.processing.entity.LoyExtProviderFeedback;
import ru.crystals.discount.processing.entity.LoyMultipleFeedbackResolutionStrategy;
import ru.crystals.test.DataBase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@RunWith(MockitoJUnitRunner.class)
public class HibernateBackedLoyFeedbackDaoTest {

    private static final String PROVIDER_ID = "Provider Id";
    private static final String PROCESSING_NAME = "Processing Name";
    private static final long SHOP_NUMBER = 1L;
    private static final long CASH_NUMBER = 2L;
    private static final long SHIFT_NUMBER = 3L;
    private static final long DOCUMENT_NUMBER = 4L;

    private static final String[] annotatedClasses = new String[] {
            "ru.crystals.discount.processing.entity.LoyExtProviderFeedback",
    };

    /**
     * the SUT (System Under Test)
     */
    private static LoyFeedbackDao sut;

    /**
     * Инициализация: подготовка БД и SUT к тестированию.
     */
    @BeforeClass
    public static void setUp() throws Exception {
        // Настроим наш SUT:
        PGSimpleDataSource dataSource = (PGSimpleDataSource) DataBase.DISCOUNT.getDataSource();
        HibernateBackedLoyFeedbackDao sutToBe = new HibernateBackedLoyFeedbackDao();
        sutToBe.setDataSource(dataSource);
        sutToBe.setHibernateProperties(getProperties());
        sutToBe.setMapping(annotatedClasses);

        // ну и инициализация пошла:
        sutToBe.postConstruct();

        sut = sutToBe;
    }

    /**
     * Чистим данные перед каждым тестом
     *
     * @throws Exception
     */
    @Before
    public void clearDB() throws Exception {
        DataBase.DISCOUNT.executeScript("DELETE FROM loy_feedback");
    }

    /**
     * Чистка после теста: надо прибрать за собой: почистить что натворили в БД.
     */
    @AfterClass
    public static void tearDown() throws Exception {
        // чистим SUT:
        ((HibernateBackedLoyFeedbackDao) sut).preDestroy();
        DataBase.DISCOUNT.clearSchema();
    }

    @Test
    public void testSaveOrUpdate() {
        LoyExtProviderFeedback feedback = createFeedback("Payload#1", null);

        Long id = sut.saveOrUpdate(feedback);
        assertNotNull(id);

        // Уже был фидбек, замещаем.
        feedback = createFeedback("Payload#2", LoyMultipleFeedbackResolutionStrategy.UPDATE_EXISTING);
        Long id2 = sut.saveOrUpdate(feedback);
        assertNotNull(id2);
        assertEquals(id, id2);

        List<LoyExtProviderFeedback> feedbacks = sut.getFeedbacks(DOCUMENT_NUMBER, SHIFT_NUMBER, CASH_NUMBER, SHOP_NUMBER,
                PROVIDER_ID, PROCESSING_NAME, FeedbackTime.AFTER_FISCALIZE);
        assertEquals(1, feedbacks.size());
        assertEquals("Payload#2", feedbacks.get(0).getPayload());

        // Проверка добавления фидбека
        sut.saveOrUpdate(createFeedback("Payload#3", LoyMultipleFeedbackResolutionStrategy.APPEND_NEW));
        sut.saveOrUpdate(createFeedback("Payload#4", LoyMultipleFeedbackResolutionStrategy.APPEND_NEW));

        feedbacks = sut.getFeedbacks(DOCUMENT_NUMBER, SHIFT_NUMBER, CASH_NUMBER, SHOP_NUMBER, PROVIDER_ID, PROCESSING_NAME, FeedbackTime.AFTER_FISCALIZE);
        assertNotNull(feedback);

        assertNotNull(feedbacks);
        assertEquals(3, feedbacks.size());
        // Порядок должен сохраниться
        assertEquals("Payload#2", feedbacks.get(0).getPayload());
        assertEquals("Payload#3", feedbacks.get(1).getPayload());
        assertEquals("Payload#4", feedbacks.get(2).getPayload());

        // Замещаем фидбеки
        sut.saveOrUpdate(createFeedback("Payload#5", LoyMultipleFeedbackResolutionStrategy.UPDATE_EXISTING));
        feedbacks = sut.getFeedbacks(DOCUMENT_NUMBER, SHIFT_NUMBER, CASH_NUMBER, SHOP_NUMBER, PROVIDER_ID, PROCESSING_NAME, FeedbackTime.AFTER_FISCALIZE);
        assertNotNull(feedback);
        assertEquals(1, feedbacks.size());
        // Порядок должен сохраниться
        assertEquals("Payload#5", feedbacks.get(0).getPayload());

        // Создаем фидбеки того же провайдера, но с другим processingName
        sut.saveOrUpdate(createFeedback("Processing6", "Payload#6", LoyMultipleFeedbackResolutionStrategy.UPDATE_EXISTING));
        sut.saveOrUpdate(createFeedback(null, "Payload#7", LoyMultipleFeedbackResolutionStrategy.UPDATE_EXISTING));
        // фидбеки должны заместиться
        feedbacks = sut.getFeedbacks(DOCUMENT_NUMBER, SHIFT_NUMBER, CASH_NUMBER, SHOP_NUMBER, PROVIDER_ID, "Processing6", FeedbackTime.AFTER_FISCALIZE);
        assertEquals(1, feedbacks.size());
        assertEquals("Processing6", feedbacks.get(0).getProcessingName());
        assertEquals("Payload#6", feedbacks.get(0).getPayload());
        feedbacks = sut.getFeedbacks(DOCUMENT_NUMBER, SHIFT_NUMBER, CASH_NUMBER, SHOP_NUMBER, PROVIDER_ID, null, FeedbackTime.AFTER_FISCALIZE);
        assertEquals(1, feedbacks.size());
        assertEquals(null, feedbacks.get(0).getProcessingName());
        assertEquals("Payload#7", feedbacks.get(0).getPayload());
    }

    @Test
    public void testGetFeedbackByProviderAndTime() {
        // Просто тестируем сортировку.
        sut.saveOrUpdate(createFeedback("10.10.2019 12:48:56"));
        sut.saveOrUpdate(createFeedback("11.10.2019 12:48:56"));
        sut.saveOrUpdate(createFeedback("10.10.2019 12:48:58"));
        sut.saveOrUpdate(createFeedback("09.10.2019 11:48:56"));
        sut.saveOrUpdate(createFeedback("09.10.2019 09:48:56"));

        List<LoyExtProviderFeedback> feedbackList = new ArrayList<>(sut.getFeedbackByProviderAndTime(PROVIDER_ID, FeedbackTime.AFTER_FISCALIZE, 10));
        assertEquals(5, feedbackList.size());

        assertEquals("09.10.2019 09:48:56", parseDate(feedbackList.get(0).getDateCreate()));
        assertEquals("09.10.2019 11:48:56", parseDate(feedbackList.get(1).getDateCreate()));
        assertEquals("10.10.2019 12:48:56", parseDate(feedbackList.get(2).getDateCreate()));
        assertEquals("10.10.2019 12:48:58", parseDate(feedbackList.get(3).getDateCreate()));
        assertEquals("11.10.2019 12:48:56", parseDate(feedbackList.get(4).getDateCreate()));
    }

    @Test
    public void testRemoveFeedback() {
        LoyExtProviderFeedback feedback = createFeedback("", LoyMultipleFeedbackResolutionStrategy.APPEND_NEW);
        feedback.setId(sut.saveOrUpdate(feedback));

        assertEquals(feedback.getId(), sut.getLastFeedbackByProviderAndTime(PROVIDER_ID, FeedbackTime.AFTER_FISCALIZE).getId());

        sut.remove(feedback);
        assertNull(sut.getLastFeedbackByProviderAndTime(PROVIDER_ID, FeedbackTime.AFTER_FISCALIZE));
    }

    @Test
    public void testRemoveFeedbacks() {
        List<LoyExtProviderFeedback> feedbacks = new ArrayList<>();
        for (int i = 0; i < 10; ++i) {
            LoyExtProviderFeedback feedback = createFeedback("", LoyMultipleFeedbackResolutionStrategy.APPEND_NEW);
            feedback.setId(sut.saveOrUpdate(feedback));
            feedbacks.add(feedback);
        }

        assertEquals(feedbacks.size(), sut.getFeedbackByProviderAndTime(PROVIDER_ID, FeedbackTime.AFTER_FISCALIZE, null).size());

        sut.remove(feedbacks);
        assertNull(sut.getLastFeedbackByProviderAndTime(PROVIDER_ID, FeedbackTime.AFTER_FISCALIZE));
    }

    @Test
    public void testRemoveFeedbackByFullKey() {
        LoyExtProviderFeedback feedback = createFeedback("", LoyMultipleFeedbackResolutionStrategy.APPEND_NEW);
        Long id = sut.saveOrUpdate(feedback);

        assertEquals(id, sut.getLastFeedbackByProviderAndTime(PROVIDER_ID, FeedbackTime.AFTER_FISCALIZE).getId());

        sut.remove(DOCUMENT_NUMBER, SHIFT_NUMBER, CASH_NUMBER, SHOP_NUMBER, PROVIDER_ID, FeedbackTime.AFTER_FISCALIZE);
        assertNull(sut.getLastFeedbackByProviderAndTime(PROVIDER_ID, FeedbackTime.AFTER_FISCALIZE));
    }

    @Test
    public void testRemoveFeedbackById() {
        LoyExtProviderFeedback feedback = createFeedback("", LoyMultipleFeedbackResolutionStrategy.APPEND_NEW);
        Long id = sut.saveOrUpdate(feedback);

        assertEquals(id, sut.getLastFeedbackByProviderAndTime(PROVIDER_ID, FeedbackTime.AFTER_FISCALIZE).getId());

        sut.remove(id);
        assertNull(sut.getLastFeedbackByProviderAndTime(PROVIDER_ID, FeedbackTime.AFTER_FISCALIZE));
    }

    /**
     * Этот тест проверяет, что удаляются только фидбеки с стратегией удаления {@link FeedbackAnnulmentStrategy#REMOVE} или null
     */
    @Test
    public void testRemoveFeedbackByChequeId() {
        LoyExtProviderFeedback feedback = createFeedback("payload1", LoyMultipleFeedbackResolutionStrategy.UPDATE_EXISTING);
        feedback.setAnnulmentStrategy(null);

        Long id = sut.saveOrUpdate(feedback);
        assertNotNull(id);

        feedback = createFeedback("payload2", LoyMultipleFeedbackResolutionStrategy.APPEND_NEW);
        feedback.setAnnulmentStrategy(FeedbackAnnulmentStrategy.PERSIST);
        id = sut.saveOrUpdate(feedback);
        assertNotNull(id);

        feedback = createFeedback("payload3", LoyMultipleFeedbackResolutionStrategy.APPEND_NEW);
        feedback.setAnnulmentStrategy(FeedbackAnnulmentStrategy.REMOVE);
        id = sut.saveOrUpdate(feedback);
        assertNotNull(id);

        Collection<LoyExtProviderFeedback> feedbacks = sut.getFeedbackByProviderAndChequeId("Provider Id", 12L);
        assertEquals(3, feedbacks.size());

        sut.removeFeedbackByChequeId(12L);

        feedbacks = sut.getFeedbackByProviderAndChequeId("Provider Id", 12L);
        assertEquals(1, feedbacks.size());

        feedback = feedbacks.iterator().next();
        assertEquals("payload2", feedback.getPayload());
    }

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

    private LoyExtProviderFeedback createFeedback(String payload, LoyMultipleFeedbackResolutionStrategy strategy) {
        return createFeedback(PROCESSING_NAME, payload, strategy);
    }

    private LoyExtProviderFeedback createFeedback(String date) {
        LoyExtProviderFeedback f = createFeedback(PROCESSING_NAME, "payload", LoyMultipleFeedbackResolutionStrategy.APPEND_NEW);
        f.setDateCreate(toDate(date));
        return f;
    }

    private LoyExtProviderFeedback createFeedback(String processingName, String payload, LoyMultipleFeedbackResolutionStrategy strategy) {
        LoyExtProviderFeedback feedback = new LoyExtProviderFeedback();
        feedback.setMultipleFeedbackResolutionStrategy(strategy);
        feedback.setCashNumber(CASH_NUMBER);
        feedback.setDateCreate(new Date());
        feedback.setDocNumber(DOCUMENT_NUMBER);
        feedback.setFeedbackTime(FeedbackTime.AFTER_FISCALIZE);
        feedback.setInn("123");
        feedback.setPayload(payload);
        feedback.setProcessingName(processingName);
        feedback.setProviderId(PROVIDER_ID);
        feedback.setResendCount(0);
        feedback.setShiftNumber(SHIFT_NUMBER);
        feedback.setShopNumber(SHOP_NUMBER);
        feedback.setReceiptId(12L);
        return feedback;
    }

    private Date toDate(String ddMMyyyyHHmmss) {
        try {
            return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(ddMMyyyyHHmmss);
        } catch (ParseException pex) {
            fail("\"" + ddMMyyyyHHmmss + "\" is not a valid date time");
            return null;
        }
    }

    private String parseDate(Date date) {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(date);
    }
}
