package ru.crystals.pos.loyal.cash.service;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.discount.processing.entity.LoyDiscountPositionEntity;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.SentToServerStatus;
import ru.crystals.pos.check.ShiftEntity;
import ru.crystals.pos.loyal.cash.persistence.HibernateBackedLoyTxDao;
import ru.crystals.pos.loyal.cash.persistence.LoyTxDao;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

/**
 * Будет тестировать корректность реализации {@link LoyTxDao} классом {@link HibernateBackedLoyTxDaoTest}.
 *
 * @author aperevozchikov
 */
public class HibernateBackedLoyTxDaoTest {

    /**
     * Черный ящик
     */
    private static Logger log = LoggerFactory.getLogger(HibernateBackedLoyTxDaoTest.class);

    /**
     * the SUT (System Under Test)
     */
    private static LoyTxDao sut;


    /**
     * самая простая проверка на то, что транзакция лояльности будет корректно сохранена в БД.
     * <p/>
     * Implementation note: просто сохраним - узнаем ID'шник - по ID'шнику извлечем и сравним с тем, что было в самом начале.
     *
     * @throws Exception
     */
    @Test
    public void shouldPersistCorrectlyAlways() throws Exception {
        // given
        LoyTransactionEntity originalTx = LoyalServiceTestUtils.buildLoyTx();

        // when
        LoyTransactionEntity persistedTx = sut.saveLoyTx(originalTx);
        LoyTransactionEntity extractedTx = persistedTx == null ? null : sut.getLoyTxById(persistedTx.getId());

        // then
        // пошли сравнения:
        // 1. После сохранения в БД должны иметь ID'шник, вообще-то
        Assert.assertTrue("the TX was not persisted!", persistedTx != null);
        Assert.assertTrue("the ID of the \"persisted\" TX is NULL!", persistedTx.getId() != null);

        // 2. У извлеченной из БД TX тоже ID'шник должен быть
        Assert.assertTrue("the extracted TX is NULL!", extractedTx != null);
        Assert.assertTrue("the ID of the \"extractedTx\" TX is NULL!", extractedTx.getId() != null);

        // 3. И самое главное: оригинальная TX и считанная из БД должны совпасть:
        LoyTxDiffGenerator txDiff = new LoyTxDiffGenerator(originalTx, extractedTx);
        Assert.assertTrue("The original and persisted TXes are not equal!", txDiff.compare().isEmpty());
    }

    // значения полей, иденцифирующие чек в следующем тесте:
    private static final long PURCHASE_NUMBER = 0x5555;
    private static final Date DATE_COMMIT = new Date();
    private static final long SHIFT_NUMBER = 0xaaaa;

    /**
     * проверим, что TX лояльности корректно привязывается к чеку.
     */
    @Test
    public void shouldLinkTxToReceiptCorrectlyAlways() {
        // given
        LoyTransactionEntity originalTx = sut.saveLoyTx(LoyalServiceTestUtils.buildLoyTx());
        originalTx.setStatus(LoyTransactionEntity.STATUS_CANCELED);
        PurchaseEntity receipt = new PurchaseEntity();
        receipt.setNumber(PURCHASE_NUMBER);
        receipt.setDateCommit(DATE_COMMIT);
        ShiftEntity shift = new ShiftEntity();
        shift.setNumShift(SHIFT_NUMBER);
        shift.setCashNum(2L);
        shift.setShopIndex(3L);
        receipt.setShift(shift);

        // when
        sut.linkTxToReceipt(originalTx, receipt);
        LoyTransactionEntity extractedTx = sut.getLoyTxById(originalTx.getId());

        // then
        Assert.assertEquals("purchase number was not updated!", PURCHASE_NUMBER, extractedTx.getPurchaseNumber());
        Assert.assertEquals("date-commit was not updated!", DATE_COMMIT, extractedTx.getSaleTime());
        Assert.assertEquals("shift-number number was not updated!", SHIFT_NUMBER, extractedTx.getShiftNumber());
        Assert.assertEquals("cash-number number was not updated!", shift.getCashNum().longValue(), extractedTx.getCashNumber());
        Assert.assertEquals("shop-number number was not updated!", shift.getShopIndex().longValue(), extractedTx.getShopNumber());
        Assert.assertEquals("status was not updated!", originalTx.getStatus(), extractedTx.getStatus());
    }

    @Test
    public void addCorrectDiscountTest() {
        LoyTransactionEntity loyTx = LoyalServiceTestUtils.buildLoyTx();
        loyTx = sut.saveLoyTx(loyTx);
        Collection<LoyDiscountPositionEntity> correctDiscounts = Arrays.asList(
                LoyalServiceTestUtils.buildLoyDiscountPosition(LoyalServiceTestUtils.buildLoyAdvActionInPurchase()),
                LoyalServiceTestUtils.buildLoyDiscountPosition(LoyalServiceTestUtils.buildLoyAdvActionInPurchase()));
        // when
        sut.discardDiscountsAndSave(loyTx, loyTx.getDiscountPositions(), correctDiscounts);

        // then
        LoyTransactionEntity loyTxById = sut.getLoyTxById(loyTx.getId());
        Assert.assertEquals(correctDiscounts.size(), loyTxById.getDiscountPositions().size());
        Assert.assertEquals(correctDiscounts.iterator().next().getAdvAction(), loyTxById.getDiscountPositions().get(0).getAdvAction());
        Assert.assertEquals(correctDiscounts.stream().mapToLong(LoyDiscountPositionEntity::getDiscountAmount).sum(), loyTxById.getDiscountValueTotal());
    }

    /**
     * Должен проверить корректность того, что TX лояльности корректно маркируются как не отправленные на сервер.
     *
     */
    @Test
    public void shouldMarkTxesForResendingCorrectlyAlways() {
        String[] allFileNames = new String[] {"one", "two", "three"};
        String[] fileNamesToUpdate = new String[] {"one", "three", "four"};
        long[] ids = new long[3];

        // given
        // Создадим 3 TX в БД как уже отправленные:
        for (int i = 0; i < allFileNames.length; i++) {
            String fn = allFileNames[i];
            LoyTransactionEntity tx = LoyalServiceTestUtils.buildLoyTx();
            tx.setFilename(fn);
            tx.setSentToServerStatus(SentToServerStatus.SENT);
            tx = sut.saveLoyTx(tx);
            ids[i] = tx.getId();
        } // for fn

        // when
        // Обновим 1й и 3й файлы:
        int updated = sut.markLoyTxesAsNotSent(Arrays.asList(fileNamesToUpdate));

        // then
        // 1. убедимся, что обновилось 2 строки
        Assert.assertEquals("illegal amont of rows were updated!", 2, updated);

        // 2. убедимся, что у 1й и 3й записи статус == NO_SENT:
        LoyTransactionEntity tx;
        tx = sut.getLoyTxById(ids[0]);
        Assert.assertEquals("Status of the 1st record was not updated!", SentToServerStatus.NO_SENT, tx.getSentToServerStatus());
        tx = sut.getLoyTxById(ids[2]);
        Assert.assertEquals("Status of the 3rd record was not updated!", SentToServerStatus.NO_SENT, tx.getSentToServerStatus());
    }

    /**
     * Инициализация: подготовка БД и SUT к тестированию.
     */
    @BeforeClass
    public static void setUp() throws Exception {
        // Настроим наш SUT:
        HibernateBackedLoyTxDao sutToBe = new HibernateBackedLoyTxDao();
        sutToBe.setDataSource(createDataSource());
        sutToBe.setHibernateProperties(getProperties());
        sutToBe.setMapping(annotatedClasses);
        sutToBe.setThreadStart(false); // да для теста тоже не важно

        // ну и инициализация пошла:
        sutToBe.postConstruct();

        sut = sutToBe;
    }
    /**
     * Чистка после теста: надо прибрать за собой: почистить что натворили в БД.
     */
    @AfterClass
    public static void tearDown() {
        // чистим SUT:
        ((HibernateBackedLoyTxDao) sut).preDestroy();
    }

    private static final String[] annotatedClasses = new String[] {
            "ru.crystals.discount.processing.entity.LoyAdvActionInPurchaseEntity",
            "ru.crystals.discount.processing.entity.LoyBonusPositionEntity",
            "ru.crystals.discount.processing.entity.LoyBonusTransactionEntity",
            "ru.crystals.discount.processing.entity.LoyChequeCouponEntity",
            "ru.crystals.discount.processing.entity.LoyPurchaseCardEntity",
            "ru.crystals.discount.processing.entity.LoyDiscountCardEntity",
            "ru.crystals.discount.processing.entity.LoyDiscountPositionEntity",
            "ru.crystals.discount.processing.entity.LoyQuestionaryEntity",
            "ru.crystals.discount.processing.entity.LoyTransactionEntity",
            "ru.crystals.discount.processing.entity.LoyProcessingCouponEntity",
            "ru.crystals.discount.processing.entity.LoyGiftNoteEnity",
            "ru.crystals.discount.processing.entity.LoyPurchaseEntity",
            "ru.crystals.discount.processing.entity.LoyPurchaseCardEntity",
            "ru.crystals.discount.processing.entity.LoyPurchasePaymentEntity",
            "ru.crystals.discount.processing.entity.LoyPurchasePositionEntity",
            "ru.crystals.discount.processing.entity.LoyBonusDiscountTransactionEntity",
            "ru.crystals.discount.processing.entity.LoyBonusSberbankTransactionEntity",
            "ru.crystals.discount.processing.entity.LoyBonusPlastekTransactionEntity",
            "ru.crystals.discount.processing.entity.LoyGiftNoteEnity",
            "ru.crystals.discount.processing.entity.LoyGiftNoteByConditionEnity",
            "ru.crystals.discount.processing.entity.LoyTokenSiebelTransactionEntity",
            "ru.crystals.discount.processing.entity.LoySetApiPluginTransactionEntity",
            "ru.crystals.discount.processing.entity.LoyChequeAdvertiseEntity",
            "ru.crystals.discount.processing.entity.LoyUpdateCounterEntity"
    };

    private static DataSource createDataSource() throws Exception {
        ComboPooledDataSource ds = new ComboPooledDataSource();
        ds.setDriverClass("org.h2.Driver");
        ds.setJdbcUrl("jdbc:h2:mem:test");
        ds.setUser("sa");
        ds.setPassword("");
        ds.setMaxIdleTime(1800);
        ds.setMinPoolSize(1);
        ds.setMaxPoolSize(5);
        ds.setMaxStatements(10);
        return ds;
    }

    /**
     * Эта штука вернет настройки соединения с БД для нашего SUT'а.
     *
     * @return
     */
    private static Properties getProperties() {
        Properties p = new Properties();
        // note: хрен с ним: сделаем так, чтоб Hibernate сам создавал таблички. на прохождение/провал тестов это не повлияет:
        //  (хотя и повлияет на длительность выполения самих тестов)
        p.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        p.setProperty("hibernate.show_sql", "true");
        p.setProperty("hibernate.format_sql", "true");
        p.setProperty("hibernate.current_session_context_class", "thread");
        p.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        p.setProperty("show_sql", "false");
        p.setProperty("format_sql", "true");
        p.setProperty("transaction.factory_class", "org.hibernate.transaction.JDBCTransactionFactory");
        return p;
    }
}
