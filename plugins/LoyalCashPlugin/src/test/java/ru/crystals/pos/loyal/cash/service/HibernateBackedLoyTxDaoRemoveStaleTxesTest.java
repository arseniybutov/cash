package ru.crystals.pos.loyal.cash.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.postgresql.ds.PGSimpleDataSource;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.pos.loyal.cash.persistence.HibernateBackedLoyTxDao;
import ru.crystals.pos.loyal.cash.persistence.LoyTxDao;
import ru.crystals.test.DataBase;

/**
 * Для проверки корректности сценариев использования hibernate-версии реализации метода {@link LoyTxDao#removeStaleLoyTxes(long, int, long, long, String)}.
 *
 * @author aperevozchikov
 */
public class HibernateBackedLoyTxDaoRemoveStaleTxesTest {

    /**
     * Номер тестовой кассы
     */
    private static final long CASH_NO = 1L;

    /**
     * Номер тестового магазина
     */
    private static final long SHOP_NO = 1L;

    /**
     * ИНН <em>основного</em> юрлица: юрика, которому принадлежит "первый" фискальник
     *
     */
    private static final String INN = "7709726613";

    /**
     * Количество документов, что должны оставаться в оперативном справочнике
     */
    private static final long SHIFTS_TO_KEEP = 10L;

    private static final String COUNT_RECORDS = "SELECT COUNT(id) FROM loy_transaction";

    // @formatter:off
    private static final MessageFormat INSERT_FORMAT = new MessageFormat("INSERT INTO loy_transaction(" +
            "id, cash_number, operation_type, purchase_number, sale_time," +
            "sent_to_server_status, shift_number, shop_number, status, transaction_time," +
            "discountvalue, purchase_amount, need_send_to_erp, need_send_bonus," +
            "need_send_accumulation, inn) " +
            "VALUES ({0}, {1}, {2}, {3}, {4}," +
            "{5}, {6}, {7}, {8}, {9}," +
            "{10}, {11}, {12}, {13}," +
            "{14}, {15});");
    // @formatter:on

    private static final String GET_IDS = "SELECT id FROM loy_transaction";

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
            "ru.crystals.discount.processing.entity.LoyGiftNoteEnity",
            "ru.crystals.discount.processing.entity.LoyProcessingCouponEntity",
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
            "ru.crystals.discount.processing.entity.LoyUpdateCounterEntity",
            "ru.crystals.discount.processing.entity.LoyChequeAdvertiseEntity"
    };


    private static LoyTxDao sut;
    private static PGSimpleDataSource dataSource;

    /**
     * Сценарий:
     * касса возврата - у которой нет "своих" продаж - используется исключительно только для возвратов.
     * после вызова тестируемого метода в БД не должно остаться ни одной записи.
     */
    @Test
    public void shouldRemoveEverythingInCaseOfReturnOnlyCash() throws Exception {
        // given
        // положим в БД 5 скидок для чеков с чужой кассы
        TxGenerator gen = new TxGenerator();
        gen.setCashNo(CASH_NO+1).setDocCount(5);
        String sql = gen.generateTxes();
        DataBase.DISCOUNT.executeScript(sql);

        // убедимся, что в БД они действительно есть. ID'шники с 1го по 5й
        LoyTransactionEntity tx = sut.getLoyTxById(1L);
        Assert.assertNotNull("loy TX was not inserted on the \"given\" step!", tx);
        Assert.assertTrue("wrong loy TX was detected!", tx.getCashNumber() == CASH_NO + 1);

        long recordsCount = countRecords();
        Assert.assertEquals("wrong number of records were created", 5, recordsCount);

        // when
        sut.removeStaleLoyTxes(SHIFTS_TO_KEEP, 1000, CASH_NO, SHOP_NO, INN);

        // then
        // в БД не должно остаться ни одной записи
        recordsCount = countRecords();
        Assert.assertEquals("not all records were removed!", 0, recordsCount);
    }

    /**
     * Сценарий:
     * Новенькая касса - только начала работать (< 10 смен) и возвратов на ней еще не делали.
     * После вызова тестируемого метода из БД ничего не должно пропасть
     */
    @Test
    public void shouldNotRemoveAnythingIfTheCashIsNewAndNoReturns() throws Exception {
        // given
        // положим в БД 5 чеков в текущей смене, 10 - в предыдущей, 3 - в пред-пред-предыдущей:
        Date now = new Date();
        TxGenerator gen = new TxGenerator();
        gen.setCashNo(CASH_NO).setShopNo(SHOP_NO).setInn(INN).setSentStatus(2);
        gen.setDocCount(5).setShiftNo(8).setLastSaleTime(now).setInitialId(5000);
        String sql = gen.generateTxes();
        DataBase.DISCOUNT.executeScript(sql);

        gen.setDocCount(10).setShiftNo(7).setLastSaleTime(new Date(now.getTime() - TimeUnit.DAYS.toMillis(1))).setInitialId(4000);
        sql = gen.generateTxes();
        DataBase.DISCOUNT.executeScript(sql);

        gen.setDocCount(3).setShiftNo(5).setLastSaleTime(new Date(now.getTime() - TimeUnit.DAYS.toMillis(3))).setInitialId(2000);
        sql = gen.generateTxes();
        DataBase.DISCOUNT.executeScript(sql);

        // в БД должно оказаться 18 скидок
        long recordsCount = countRecords();
        Assert.assertEquals("wrong number of records were created", 18, recordsCount);

        // when
        sut.removeStaleLoyTxes(SHIFTS_TO_KEEP, 1000, CASH_NO, SHOP_NO, INN);

        // then
        // в БД должно остаться 18 скидок
        recordsCount = countRecords();
        Assert.assertEquals("not all records were removed!", 18, recordsCount);
    }

    /**
     * Сценарий:
     * На кассе есть не отправленные на сервер TX лояльности - в т.ч. очень старые.
     * После вызова тестируемого метода они должны остаться в БД.
     */
    @Test
    public void shouldNotRemoveNotSentToServerRecords() throws Exception {
        // given
        Date now = new Date();
        TxGenerator gen = new TxGenerator();
        Set<Long> expected = new HashSet<>();
        long lastShiftNo = 500;

        // добавим в БД один актуальный, еще не отправленный на сервер документ (статус отправки == 0)
        gen.setCashNo(CASH_NO).setShopNo(SHOP_NO).setInn(INN).setSentStatus(0);
        gen.setDocCount(1).setShiftNo(lastShiftNo).setLastSaleTime(now).setInitialId(lastShiftNo*10);
        String sql = gen.generateTxes();
        DataBase.DISCOUNT.executeScript(sql);

        // парочку уже отправленных в этой же смене
        gen.setSentStatus(2).setDocCount(2).setLastSaleTime(new Date(now.getTime() - TimeUnit.MINUTES.toMillis(2))).setInitialId(lastShiftNo*10L - 2);
        sql = gen.generateTxes();
        DataBase.DISCOUNT.executeScript(sql);

        // один в этой же смене, что не удалось отправить по причине какой-то ошибки (статус отправки == 5)
        gen.setSentStatus(5).setDocCount(1).setLastSaleTime(new Date(now.getTime() - TimeUnit.MINUTES.toMillis(10))).setInitialId(lastShiftNo*10L - 3);
        sql = gen.generateTxes();
        DataBase.DISCOUNT.executeScript(sql);

        expected.addAll(Arrays.asList(lastShiftNo*10L - 3, lastShiftNo*10L - 2, lastShiftNo*10L - 1, lastShiftNo*10L));

        // добавим несколько (30) отправленных документов в древних сменах
        StringBuilder sb = new StringBuilder();
        gen.setSentStatus(2);
        for (long shift = lastShiftNo - 15; shift <= lastShiftNo-1; shift++) {
            gen.setDocCount(2).setShiftNo(shift).setLastSaleTime(new Date(now.getTime() - TimeUnit.DAYS.toMillis(lastShiftNo - shift))).setInitialId(10L*shift);
            sb.append(gen.generateTxes());
            sb.append("\n");

            // даже отправленные документы последних 10 смен должны отстаться
            if (shift > lastShiftNo - SHIFTS_TO_KEEP) {
                expected.addAll(Arrays.asList(10L*shift, 10L*shift + 1));
            }
        }
        sql = sb.toString();
        DataBase.DISCOUNT.executeScript(sql);

        // и еще один, что не удалось отправить в очень древней смене
        gen.setSentStatus(5);
        gen.setShiftNo(10).setSentStatus(0).setDocCount(1).setLastSaleTime(new Date(now.getTime() - TimeUnit.DAYS.toMillis(486))).setInitialId(2000);
        sql = gen.generateTxes();
        DataBase.DISCOUNT.executeScript(sql);
        expected.add(2000L);

        // в БД должно оказаться 35 записей
        long recordsCount = countRecords();
        Assert.assertEquals("wrong number of records were created", 35, recordsCount);

        // when
        sut.removeStaleLoyTxes(SHIFTS_TO_KEEP, 1000, CASH_NO, SHOP_NO, INN);

        // then
        // Из БД должны уйти только уже отправленные документы из очень древних смен (> 10 смен назад)
        Set<Long> actual = getIds();
        Assert.assertEquals("wrong records were removed!", expected, actual);
    }

    /**
     * Сценарий:
     * только что заменили основной фискальник. Нумерация смен пошла с 1.
     *      До этого по какой-то причине "подрезку" не делали - и там есть очень древние уже отправленные документы.
     * После вызова тестируемого метода часть этих документов должна быть удалена: должны остаться только документы "последних" 10 смен с данной кассы
     */
    @Test
    public void shouldNotRemoveAnythingInEarlyStagesOfFiscalChange() throws Exception {
        // given
        Date now = new Date();
        TxGenerator gen = new TxGenerator();

        // несколько документов (30) в дрених сменах
        StringBuilder sb = new StringBuilder();
        gen.setCashNo(CASH_NO).setShopNo(SHOP_NO).setInn(INN).setSentStatus(2);
        for (int shift = 1; shift <= 15; shift++) {
            gen.setDocCount(2).setShiftNo(shift).setLastSaleTime(new Date(now.getTime() - TimeUnit.DAYS.toMillis(15 - shift))).setInitialId(100L*shift);
            sb.append(gen.generateTxes());
            sb.append("\n");
        }
        String sql = sb.toString();
        DataBase.DISCOUNT.executeScript(sql);

        // пара документов в новой смене:
        gen.setShiftNo(1).setInn(INN).setLastSaleTime(now).setDocCount(2).setInitialId(10100);
        sql = gen.generateTxes();
        DataBase.DISCOUNT.executeScript(sql);

        // в БД должно оказаться 32 записи
        long recordsCount = countRecords();
        Assert.assertEquals("wrong number of records were created", 32, recordsCount);

        // when
        sut.removeStaleLoyTxes(SHIFTS_TO_KEEP, 1000, CASH_NO, SHOP_NO, INN);

        // then
        // В БД должны остаться только документы свежей смены и документы 9 последних смен со старым фискальником
        Set<Long> expected = new HashSet<>();
        expected.add(10100L);
        expected.add(10101L);
        for (int shift = 7; shift <= 15; shift++) {
            expected.add(100L*shift);
            expected.add(100L*shift + 1);
        }

        Set<Long> actual = getIds();
        Assert.assertEquals("wrong records were left", expected, actual);
    }

    /**
     * Сценарий:
     * INN юрлица отсутствует.
     * на кассе есть и свежие документы (отправленные и не отправленные), и старые (отправленные и не отправленные).
     * После вызова тестируемого метода старые уже отправленные документы должны уйти из БД.
     *
     * P.S: Просто отследить, что скрипт выборки даты самой свежей записи, что можно удалить получается валидным (корректным) и случае. когда INN не указан.
     */
    @Test
    public void shouldRemoveStaleIfNoInn() throws Exception {
        // given
        Date now = new Date();
        TxGenerator gen = new TxGenerator();
        Set<Long> expected = new HashSet<>();
        long lastShiftNo = 500;

        // добавим в БД несколько (26) уже отправленных документа
        gen.setCashNo(CASH_NO).setShopNo(SHOP_NO).setInn(INN).setSentStatus(2).setDocCount(2);
        StringBuilder sb = new StringBuilder();
        for (long shift = lastShiftNo - SHIFTS_TO_KEEP - 2; shift <= lastShiftNo; shift++) {
            gen.setShiftNo(shift).setLastSaleTime(new Date(now.getTime() - TimeUnit.DAYS.toMillis(lastShiftNo - shift))).setInitialId(10*shift);
            sb.append(gen.generateTxes());
            sb.append("\n");

            // отправленные документы последних 10 смен должны отстаться
            if (shift > lastShiftNo - SHIFTS_TO_KEEP) {
                expected.addAll(Arrays.asList(10L*shift, 10L*shift + 1));
            }
        }
        String sql = sb.toString();
        DataBase.DISCOUNT.executeScript(sql);

        // добавим еще несколько (13) не отправленных из-за ошибки:
        sb = new StringBuilder();
        gen.setSentStatus(5).setDocCount(1);
        for (long shift = lastShiftNo - SHIFTS_TO_KEEP - 2; shift <= lastShiftNo; shift++) {
            gen.setShiftNo(shift).setLastSaleTime(new Date(now.getTime() - TimeUnit.DAYS.toMillis(lastShiftNo - shift))).setInitialId(10L*shift + 2);
            sb.append(gen.generateTxes());
            sb.append("\n");

            // ВСЕ не отправленные документы должны отстаться
            expected.addAll(Arrays.asList(10L*shift + 2));
        }
        sql = sb.toString();
        DataBase.DISCOUNT.executeScript(sql);

        // в последней смене добавим еще один (1) только формируемый документ (статус == 0):
        gen.setSentStatus(0).setDocCount(1).setShiftNo(lastShiftNo).setLastSaleTime(now).setInitialId(10L*lastShiftNo + 3);
        sql = gen.generateTxes();
        DataBase.DISCOUNT.executeScript(sql);

        // эта TX тоже должна остаться
        expected.add(10L*lastShiftNo + 3);

        // и еще добавим один (1) очень свежий документ с другой кассы (эта TX не должна остаться в БД - даже несмотря на ее "свежесть"):
        gen.setSentStatus(4).setCashNo(CASH_NO + 1).setLastSaleTime(now).setInitialId(10L*lastShiftNo + 4);
        sql = gen.generateTxes();
        DataBase.DISCOUNT.executeScript(sql);

        // в БД должно оказаться 41 запись
        long recordsCount = countRecords();
        Assert.assertEquals("wrong number of records were created", 41, recordsCount);

        // when
        sut.removeStaleLoyTxes(SHIFTS_TO_KEEP, 1000, CASH_NO, SHOP_NO, null);

        // then
        // Из БД должны уйти только уже отправленные документы из очень древних смен (> 10 смен назад)
        Set<Long> actual = getIds();
        Assert.assertEquals("wrong records were removed!", expected, actual);
    }

    /**
     * Вернет ID'шники всех существующих записей из loy_transaction
     *
     * @return не {@code null}
     */
    private Set<Long> getIds() throws Exception {
        Set<Long> result = new HashSet<>();
        try (Connection con = dataSource.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(GET_IDS);) {
            while (rs.next()) {
                result.add(rs.getLong(1));
            }
        }
        return result;
    }

    /**
     * Просто вернет количество записей в loy_transaction
     *
     * @return количество TX лояльности, хранящихся на кассе
     */
    private long countRecords() throws Exception {
        long result = -1;
        try (Connection con = dataSource.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(COUNT_RECORDS);) {
            if (rs.next()) {
                result = rs.getLong(1);
            }
        }
        return result;
    }

    /**
     * Просто для генерации скрипта для вставки TX лояльности в БД
     */
    private class TxGenerator {
        private long initialId = 0L;
        private long cash = 2L;
        private boolean type = true;
        private long firstDocNo = 1L;
        private Date lastSaleTime = new Date();

        private int sentStatus = 4;
        private long shift = 1L;
        private long shop = SHOP_NO;
        private int status = 0;

        private long discountValue = 1L;
        private long purchaseAmount = 1L;
        private boolean toErp = true;
        private boolean toBonus = true;
        private boolean toAcc = true;
        private String inn = null;

        private long docCount = 1;

        private long docsInterval = 1000L;

        public String generateTxes() {
            StringBuilder sb = new StringBuilder();
            for (long i = 0; i < docCount; i++) {
                Date saleTime = new Date(lastSaleTime.getTime() - (docCount - i) * docCount * docsInterval);
                Date txTime = saleTime;
                sb.append(INSERT_FORMAT.format(new Object[] {Long.toString(initialId + i), cash, type, firstDocNo + i, formatDate(saleTime),
                        sentStatus, shift, shop, status, formatDate(txTime),
                        discountValue, purchaseAmount, toErp, toBonus, toAcc, inn == null ? "null" : "\'" + inn + "\'"}));
                sb.append("\n");
            }
            return sb.toString();
        }

        private String formatDate(Date d) {
            return String.format("'%1$tF %1$tT'", d);
        }

        public TxGenerator setInitialId(long id) {
            this.initialId = id;
            return this;
        }

        public TxGenerator setCashNo(long cash) {
            this.cash = cash;
            return this;
        }

        public TxGenerator setType(boolean type) {
            this.type = type;
            return this;
        }

        public TxGenerator setFirstDocNo(long docNo) {
            this.firstDocNo = docNo;
            return this;
        }

        public TxGenerator setLastSaleTime(Date saleTime) {
            this.lastSaleTime = saleTime;
            return this;
        }

        public TxGenerator setSentStatus(int status) {
            this.sentStatus = status;
            return this;
        }

        public TxGenerator setShiftNo(long shift) {
            this.shift = shift;
            return this;
        }

        public TxGenerator setShopNo(long shop) {
            this.shop = shop;
            return this;
        }

        public TxGenerator setStatus(int status) {
            this.status = status;
            return this;
        }

        public TxGenerator setInn(String inn) {
            this.inn = inn;
            return this;
        }

        public TxGenerator setDocCount(long docCount) {
            this.docCount = docCount;
            return this;
        }

        public TxGenerator setDocsInterval(long interval) {
            this.docsInterval = interval;
            return this;
        }
    }


    @BeforeClass
    public static void prepareDB() throws Exception {
        // создаем БД, схему, и все таблицы из кассовой БД DISCOUNT:
        dataSource = (PGSimpleDataSource) DataBase.DISCOUNT.getDataSource();

        HibernateBackedLoyTxDao sutToBe = new HibernateBackedLoyTxDao();
        sutToBe.setDataSource(dataSource);
        sutToBe.setHibernateProperties(getProperties());
        sutToBe.setMapping(annotatedClasses);
        sutToBe.setThreadStart(false); // да для теста тоже не важно

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
        DataBase.DISCOUNT.executeScript("DELETE FROM loy_transaction");
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


}
