package ru.crystals.pos.loyal.cash.persistence;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.discount.processing.entity.FeedbackAnnulmentStrategy;
import ru.crystals.discount.processing.entity.FeedbackTime;
import ru.crystals.discount.processing.entity.LoyExtProviderFeedback;
import ru.crystals.discount.processing.entity.LoyMultipleFeedbackResolutionStrategy;
import ru.crystals.pos.datasource.jdbc.JDBCMapper;
import ru.crystals.test.DataBase;

import javax.sql.DataSource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by agaydenger on 01.08.16.
 */
@RunWith(MockitoJUnitRunner.class)
public class JdbcBackedLoyFeedbackDaoTest {

    private static final String TEST_SQL_DRIVER_CLASS = "TEST_SQL_DRIVER_CLASS";
    private static final String TEST_DB_URL = "TEST_DB_URL";
    private static final String TEST_LOGIN = "TEST_LOGIN";
    private static final String TEST_PASSWORD = "TEST_PASSWORD";
    private static final int TEST_CONNECTION_POOL_SIZE = 3;
    private static final String DEFAULT_SQL_DRIVER_CLASS = "org.postgresql.Driver";
    private static final String DEFAULT_DB_URL = "jdbc:postgresql://localhost:5432/discount";
    private static final String DEFAULT_LOGIN = "postgres";
    private static final String DEFAULT_PASSWORD = "postgres";
    private static final String GET_TRANSACTIONS_BY_PROVIDER_AND_TIME = "SELECT * FROM loy_feedback WHERE provider_id = ? AND feedback_time = ?";
    private static final String TEST_PROVIDER_NAME = "TEST_PROVIDER_NAME";
    private static final String LIMIT = " LIMIT ?";
    private static final int TEST_MAX_COUNT = 20;
    private static final int DEFAULT_POOL_SIZE = 4;
    private static final long TEST_FEEDBACK_ID = 123;

    @Mock
    private JDBCMapper jdbcMapper;

    @Mock
    private LoyExtProviderFeedback loyFeedback;

    @Mock
    private List<LoyExtProviderFeedback> mockFeedbacks;

    @Spy
    private JdbcBackedLoyFeedbackDao service = new JdbcBackedLoyFeedbackDao();

    @Test
    public void testGetJDBCMapper() throws Exception {
        //given
        service.setJdbcMapper(null);
        service.setConnectionPoolSize(TEST_CONNECTION_POOL_SIZE);
        service.setDbUrl(TEST_DB_URL);
        service.setLogin(TEST_LOGIN);
        service.setPassword(TEST_PASSWORD);
        service.setSqlDriverClass(TEST_SQL_DRIVER_CLASS);
        doReturn(jdbcMapper).when(service).createNewJDBCMapper(anyString(), anyString(), anyString(), anyString(), anyInt());
        //when
        JDBCMapper result = service.getJdbcMapper();
        //then
        assertThat(result).isSameAs(jdbcMapper);
        verify(service).createNewJDBCMapper(TEST_SQL_DRIVER_CLASS, TEST_DB_URL, TEST_LOGIN, TEST_PASSWORD, TEST_CONNECTION_POOL_SIZE);
    }

    @Test
    public void testSaveOrUpdate() throws Exception {
        DataSource dataSource = DataBase.DISCOUNT.getDataSource();
        JdbcBackedLoyFeedbackDao dao = new JdbcBackedLoyFeedbackDao(dataSource);

        LoyExtProviderFeedback feedback = createFeedback("Payload#1", null);

        dao.getJdbcMapper().startTransaction();
        Long id = dao.saveOrUpdate(feedback);
        dao.getJdbcMapper().commitTransaction();
        assertNotNull(id);

        // Уже был фидбек, замещаем.
        feedback = createFeedback("Payload#2", LoyMultipleFeedbackResolutionStrategy.UPDATE_EXISTING);
        dao.getJdbcMapper().startTransaction();
        Long id2 = dao.saveOrUpdate(feedback);
        dao.getJdbcMapper().commitTransaction();
        assertNotNull(id2);
        assertEquals(id, id2);

        dao.getJdbcMapper().startTransaction();
        List<LoyExtProviderFeedback> feedbacks = dao.getFeedbacks(2L, 3L, 1L, 4L, "Provider Id", "ProcessingName", FeedbackTime.AFTER_FISCALIZE);
        assertNotNull(feedback);
        dao.getJdbcMapper().commitTransaction();
        assertEquals(1, feedbacks.size());
        assertEquals("Payload#2", feedbacks.get(0).getPayload());

        // Проверка добавления фидбека
        dao.getJdbcMapper().startTransaction();
        dao.saveOrUpdate(createFeedback("Payload#3", LoyMultipleFeedbackResolutionStrategy.APPEND_NEW));
        dao.saveOrUpdate(createFeedback("Payload#4", LoyMultipleFeedbackResolutionStrategy.APPEND_NEW));
        dao.getJdbcMapper().commitTransaction();

        dao.getJdbcMapper().startTransaction();
        feedbacks = dao.getFeedbacks(2L, 3L, 1L, 4L, "Provider Id", "ProcessingName", FeedbackTime.AFTER_FISCALIZE);
        dao.getJdbcMapper().commitTransaction();
        assertNotNull(feedback);

        assertNotNull(feedbacks);
        assertEquals(3, feedbacks.size());
        // Порядок должен сохраниться
        assertEquals("Payload#2", feedbacks.get(0).getPayload());
        assertEquals("Payload#3", feedbacks.get(1).getPayload());
        assertEquals("Payload#4", feedbacks.get(2).getPayload());

        // Замещаем фидбеки
        dao.getJdbcMapper().startTransaction();
        dao.saveOrUpdate(createFeedback("Payload#5", LoyMultipleFeedbackResolutionStrategy.UPDATE_EXISTING));
        dao.getJdbcMapper().commitTransaction();
        dao.getJdbcMapper().startTransaction();
        feedbacks = dao.getFeedbacks(2L, 3L, 1L, 4L, "Provider Id", "ProcessingName", FeedbackTime.AFTER_FISCALIZE);
        dao.getJdbcMapper().commitTransaction();
        assertNotNull(feedback);
        assertEquals(1, feedbacks.size());
        // Порядок должен сохраниться
        assertEquals("Payload#5", feedbacks.get(0).getPayload());

        // Создаем фидбеки того же провайдера, но с другим processingName
        dao.getJdbcMapper().startTransaction();
        dao.saveOrUpdate(createFeedback("Processing6", "Payload#6", LoyMultipleFeedbackResolutionStrategy.UPDATE_EXISTING));
        dao.saveOrUpdate(createFeedback(null, "Payload#7", LoyMultipleFeedbackResolutionStrategy.UPDATE_EXISTING));
        dao.getJdbcMapper().commitTransaction();
        // фидбеки должны заместиться
        dao.getJdbcMapper().startTransaction();
        feedbacks = dao.getFeedbacks(2L, 3L, 1L, 4L, "Provider Id", "Processing6", FeedbackTime.AFTER_FISCALIZE);
        dao.getJdbcMapper().commitTransaction();
        assertEquals(1, feedbacks.size());
        assertEquals("Processing6", feedbacks.get(0).getProcessingName());
        assertEquals("Payload#6", feedbacks.get(0).getPayload());
        dao.getJdbcMapper().startTransaction();
        feedbacks = dao.getFeedbacks(2L, 3L, 1L, 4L, "Provider Id", null, FeedbackTime.AFTER_FISCALIZE);
        dao.getJdbcMapper().commitTransaction();
        assertEquals(1, feedbacks.size());
        assertEquals(null, feedbacks.get(0).getProcessingName());
        assertEquals("Payload#7", feedbacks.get(0).getPayload());
    }

    private LoyExtProviderFeedback createFeedback(String payload, LoyMultipleFeedbackResolutionStrategy strategy) {
        return createFeedback("ProcessingName", payload, strategy);
    }

    private LoyExtProviderFeedback createFeedback(String processingNmae, String payload, LoyMultipleFeedbackResolutionStrategy strategy) {
        LoyExtProviderFeedback feedback = new LoyExtProviderFeedback();
        feedback.setMultipleFeedbackResolutionStrategy(strategy);
        feedback.setCashNumber(1L);
        feedback.setDateCreate(new Date());
        feedback.setDocNumber(2L);
        feedback.setFeedbackTime(FeedbackTime.AFTER_FISCALIZE);
        feedback.setInn("123");
        feedback.setPayload(payload);
        feedback.setProcessingName(processingNmae);
        feedback.setProviderId("Provider Id");
        feedback.setResendCount(0);
        feedback.setShiftNumber(3L);
        feedback.setShopNumber(4L);
        feedback.setReceiptId(12L);
        return feedback;
    }

    @Test
    public void testGetFeedbackByProviderAndTime() {
        // Просто тестируем сортировку.

        JDBCMapper mapper = Mockito.mock(JDBCMapper.class);

        List<LoyExtProviderFeedback> feedbackList = new ArrayList<>();
        feedbackList.add(createFeedback("10.10.2019 12:48:56"));
        feedbackList.add(createFeedback("11.10.2019 12:48:56"));
        feedbackList.add(createFeedback("10.10.2019 12:48:58"));
        feedbackList.add(createFeedback("09.10.2019 11:48:56"));
        feedbackList.add(createFeedback("09.10.2019 09:48:56"));

        Mockito.when(mapper.selectList(Mockito.eq(LoyExtProviderFeedback.class), Mockito.anyString(), Mockito.anyVararg()))
                .thenReturn(feedbackList);

        JdbcBackedLoyFeedbackDao dao = Mockito.mock(JdbcBackedLoyFeedbackDao.class);
        Mockito.when(dao.getJdbcMapper()).thenReturn(mapper);
        Mockito.when(dao.getFeedbackByProviderAndTime(Mockito.anyString(), Mockito.any(), Mockito.anyInt())).thenCallRealMethod();

        feedbackList = new ArrayList<>(dao.getFeedbackByProviderAndTime("", FeedbackTime.AS_SOON_AS_POSSIBLE, 10));
        assertEquals(5, feedbackList.size());

        assertEquals("09.10.2019 09:48:56", parseDate(feedbackList.get(0).getDateCreate()));
        assertEquals("09.10.2019 11:48:56", parseDate(feedbackList.get(1).getDateCreate()));
        assertEquals("10.10.2019 12:48:56", parseDate(feedbackList.get(2).getDateCreate()));
        assertEquals("10.10.2019 12:48:58", parseDate(feedbackList.get(3).getDateCreate()));
        assertEquals("11.10.2019 12:48:56", parseDate(feedbackList.get(4).getDateCreate()));
    }

    private LoyExtProviderFeedback createFeedback(String date) {
        LoyExtProviderFeedback f = new LoyExtProviderFeedback();
        f.setDateCreate(toDate(date));
        return f;
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

    /**
     * Этот тест проверяет, что удаляются только фидбеки с стратегией удаления {@link ru.crystals.discount.processing.entity.FeedbackAnnulmentStrategy#REMOVE} или null
     */
    @Test
    public void testRemoveFeedbackByChequeId() throws Exception {
        DataSource dataSource = DataBase.DISCOUNT.getDataSource();
        JdbcBackedLoyFeedbackDao dao = new JdbcBackedLoyFeedbackDao(dataSource);

        LoyExtProviderFeedback feedback = createFeedback("payload1", LoyMultipleFeedbackResolutionStrategy.UPDATE_EXISTING);
        feedback.setAnnulmentStrategy(null);

        dao.getJdbcMapper().startTransaction();
        Long id = dao.saveOrUpdate(feedback);
        assertNotNull(id);

        feedback = createFeedback("payload2", LoyMultipleFeedbackResolutionStrategy.APPEND_NEW);
        feedback.setAnnulmentStrategy(FeedbackAnnulmentStrategy.PERSIST);
        id = dao.saveOrUpdate(feedback);
        assertNotNull(id);

        feedback = createFeedback("payload3", LoyMultipleFeedbackResolutionStrategy.APPEND_NEW);
        feedback.setAnnulmentStrategy(FeedbackAnnulmentStrategy.REMOVE);
        id = dao.saveOrUpdate(feedback);
        assertNotNull(id);

        Collection<LoyExtProviderFeedback> feedbacks = dao.getFeedbackByProviderAndChequeId("Provider Id", 12L);
        assertEquals(3, feedbacks.size());

        dao.removeFeedbackByChequeId(12L);

        feedbacks = dao.getFeedbackByProviderAndChequeId("Provider Id", 12L);
        assertEquals(1, feedbacks.size());

        feedback = feedbacks.iterator().next();
        assertEquals("payload2", feedback.getPayload());

        dao.getJdbcMapper().rollbackTransaction();
    }

    @Test
    public void testGetJDBCMapperCreationInvokedWithDefaultParams() throws Exception {
        //given
        service.setJdbcMapper(null);
        doReturn(jdbcMapper).when(service).createNewJDBCMapper(anyString(), anyString(), anyString(), anyString(), anyInt());
        //when
        JDBCMapper result = service.getJdbcMapper();
        //then
        assertThat(result).isSameAs(jdbcMapper);
        verify(service).createNewJDBCMapper(DEFAULT_SQL_DRIVER_CLASS, DEFAULT_DB_URL, DEFAULT_LOGIN, DEFAULT_PASSWORD, DEFAULT_POOL_SIZE);
    }

    @Test
    public void testGetJDBCMapperAlreadyCreated() throws Exception {
        //given
        service.setJdbcMapper(jdbcMapper);
        //when
        JDBCMapper result = service.getJdbcMapper();
        //then
        verify(service, never()).createNewJDBCMapper(anyString(), anyString(), anyString(), anyString(), anyInt());
        assertThat(result).isSameAs(jdbcMapper);
    }

    @Test
    public void testGetJDBCMapperReturnNullWhenMapperCreationThrowsException() throws Exception {
        //given
        service.setJdbcMapper(null);
        doThrow(new RuntimeException()).when(service).createNewJDBCMapper(anyString(), anyString(), anyString(), anyString(), anyInt());
        //when
        JDBCMapper result = service.getJdbcMapper();
        //then
        assertThat(result).isNull();
        verify(service).createNewJDBCMapper(DEFAULT_SQL_DRIVER_CLASS, DEFAULT_DB_URL, DEFAULT_LOGIN, DEFAULT_PASSWORD, DEFAULT_POOL_SIZE);
    }

    @Test
    public void testCreate() throws Exception {
        //given
        doReturn(jdbcMapper).when(service).getJdbcMapper();
        doReturn(TEST_FEEDBACK_ID).when(loyFeedback).getId();
        //when
        Long result = service.saveOrUpdate(loyFeedback);
        //then
        verify(service, times(2)).getJdbcMapper();
        verify(jdbcMapper).replace(loyFeedback);
        verify(loyFeedback).getId();
        assertThat(result).isEqualTo(TEST_FEEDBACK_ID);
    }

    @Test
    public void testCreateReturnNullWhenJDBCMapperNotCreated() throws Exception {
        //given
        doReturn(null).when(service).getJdbcMapper();
        //when
        Long result = service.saveOrUpdate(loyFeedback);
        //then
        verify(jdbcMapper, never()).startTransaction();
        verify(jdbcMapper, never()).replace(loyFeedback);
        verify(jdbcMapper, never()).commitTransaction();
        verify(loyFeedback, never()).getId();
        assertThat(result).isNull();
    }

    @Test
    public void testGetStaleTransactionsWithoutLimit() throws Exception {
        //given
        doReturn(jdbcMapper).when(service).getJdbcMapper();
        doReturn(mockFeedbacks).when(jdbcMapper).selectList(any(Class.class), anyString(), anyString(), any(FeedbackTime.class));
        //when
        Collection<LoyExtProviderFeedback> result = service.getFeedbackByProviderAndTime(TEST_PROVIDER_NAME, FeedbackTime.AFTER_FISCALIZE, null);
        //then
        assertThat(result).isSameAs(mockFeedbacks);
        verify(service).getJdbcMapper();
        verify(jdbcMapper).selectList(LoyExtProviderFeedback.class, GET_TRANSACTIONS_BY_PROVIDER_AND_TIME, TEST_PROVIDER_NAME, FeedbackTime.AFTER_FISCALIZE);
    }

    @Test
    public void testGetStaleTransactionsWithLimit() throws Exception {
        //given
        doReturn(jdbcMapper).when(service).getJdbcMapper();
        doReturn(mockFeedbacks).when(jdbcMapper).selectList(any(Class.class), anyString(), anyString(), any(FeedbackTime.class), anyInt());
        //when
        Collection<LoyExtProviderFeedback> result = service.getFeedbackByProviderAndTime(TEST_PROVIDER_NAME, FeedbackTime.AFTER_FISCALIZE, TEST_MAX_COUNT);
        //then
        assertThat(result).isSameAs(mockFeedbacks);
        verify(service).getJdbcMapper();
        verify(jdbcMapper).selectList(LoyExtProviderFeedback.class, GET_TRANSACTIONS_BY_PROVIDER_AND_TIME + LIMIT, TEST_PROVIDER_NAME, FeedbackTime.AFTER_FISCALIZE, TEST_MAX_COUNT);
    }

    @Test
    public void testGetStaleTransactionsWithNegativeLimit() throws Exception {
        //given
        doReturn(jdbcMapper).when(service).getJdbcMapper();
        doReturn(mockFeedbacks).when(jdbcMapper).selectList(any(Class.class), anyString(), anyString(), any(FeedbackTime.class));
        //when
        Collection<LoyExtProviderFeedback> result = service.getFeedbackByProviderAndTime(TEST_PROVIDER_NAME, FeedbackTime.AFTER_FISCALIZE, -TEST_MAX_COUNT);
        //then
        assertThat(result).isSameAs(mockFeedbacks);
        verify(service).getJdbcMapper();
        verify(jdbcMapper).selectList(LoyExtProviderFeedback.class, GET_TRANSACTIONS_BY_PROVIDER_AND_TIME, TEST_PROVIDER_NAME, FeedbackTime.AFTER_FISCALIZE);
    }

    @Test
    public void testGetStaleTransactionsReturnNullWhenNullReturned() throws Exception {
        //given
        doReturn(jdbcMapper).when(service).getJdbcMapper();
        doReturn(null).when(jdbcMapper).selectList(any(Class.class), anyString(), anyString(), any(FeedbackTime.class));
        //when
        Collection<LoyExtProviderFeedback> result = service.getFeedbackByProviderAndTime(TEST_PROVIDER_NAME, FeedbackTime.AFTER_FISCALIZE, null);
        //then
        assertThat(result).isNull();
        verify(service).getJdbcMapper();
        verify(jdbcMapper).selectList(LoyExtProviderFeedback.class, GET_TRANSACTIONS_BY_PROVIDER_AND_TIME, TEST_PROVIDER_NAME, FeedbackTime.AFTER_FISCALIZE);
    }

    @Test
    public void testGetStaleTransactionsReturnNullWhenNoJdbcMapper() throws Exception {
        //given
        doReturn(null).when(service).getJdbcMapper();
        doReturn(null).when(jdbcMapper).selectList(any(Class.class), anyString(), anyString(), any(FeedbackTime.class));
        //when
        Collection<LoyExtProviderFeedback> result = service.getFeedbackByProviderAndTime(TEST_PROVIDER_NAME, FeedbackTime.AFTER_FISCALIZE, null);
        //then
        assertThat(result).isNull();
        verify(service).getJdbcMapper();
        verify(jdbcMapper, never()).selectList(any(Class.class), anyString(), anyString(), any(FeedbackTime.class));
    }


    @Test
    public void testRemove() {
        //given
        doReturn(jdbcMapper).when(service).getJdbcMapper();
        //when
        service.remove(loyFeedback);
        //then
        verify(service).getJdbcMapper();
        verify(jdbcMapper).deleteFrom(loyFeedback);
    }
}
