package ru.crystals.pos.loyal.cash.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.crystals.discount.processing.entity.LoyAdvActionInPurchaseEntity;
import ru.crystals.discount.processing.entity.LoyBonusSberbankTransactionEntity;
import ru.crystals.discount.processing.entity.LoyDiscountPositionEntity;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.SentToServerStatus;


// FIXME: отложим реализацию: по уму нужна реализация "генератора ключей" (наподобие org.hibernate.id.SequenceHiLoGenerator)
//  - что будет генерить ID'шники для сущностей лояльности - чтоб можно было одним скриптом-строкой сразу в 15 таблиц данные 
//  заинзертить.
//  При реализации аналога SequenceHiLoGenerator можно HI-val брать как 50 * SEQ; 
//      где SEQ - то, что будет возвращено по запросу select nextval('hibernate_sequence')
//  (получается в терминах SequenceHiLoGenerator maxLo == 49)
/**
 * Эта реализация {@link LoyTxDao} "работает" с БД через JDBC.
 * 
 * @author aperevozchikov
 */
public class JdbcBackedLoyTxDao implements LoyTxDao {
    
    /**
     * черный ящик
     */
    private static Logger log = LoggerFactory.getLogger(JdbcBackedLoyTxDao.class);
    
    // injected
    /**
     * генератор ключей, что будем использовать
     */
    private IdentifierGenerator idGenerator;
    
    // injected
    /**
     * Настройки для подключения к БД
     */
    private Properties jdbcProperties;
    
    // injected
    /**
     * URL для доступа до базы с сущностями лояльности
     */
    private String jdbcUrl;
    
    /**
     * А вот это уже сама коннекция, через которую будем работать с БД лояльности
     */
    private Connection dbCon;
    
    /**
     * Этот ANSI-SQL скрипт будет вставлять одну запись в таблицу loy_transaction.
     */
    private static final String INSERT_LOY_TX = "INSERT " +
            "INTO loy_transaction(cash_number, operation_type, purchase_number, sale_time, sent_to_server_status, shift_number, " +
            "shop_number, status, transaction_time, discountvalue, filename, purchase_amount, need_send_to_erp, need_send_bonus, " +
            "need_send_accumulation, inn) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
    /**
     * Этот скомпилированный запрос будет отвечать за добавление (insert) записей в таблицу loy_transaction.
     * <p/>
     * NOTE: из этого запроса можно будет узнать какие ID'шники были авто-сгенерены (см. {@link PreparedStatement#getGeneratedKeys()})
     */
    private PreparedStatement insertLoyTxStatement;
    private PreparedStatement getInsertLoyTxStatement() throws SQLException {
        if (insertLoyTxStatement == null || insertLoyTxStatement.isClosed()) {
            Connection con = getDbCon();
            insertLoyTxStatement = con.prepareStatement(INSERT_LOY_TX, Statement.RETURN_GENERATED_KEYS);
        }
        
        return insertLoyTxStatement;
    }

    /**
     * Добавит ("заинзертит") указанную транзакцию лояльности в БД.
     * <p/>
     * NOTE: добавится запись только в loy_transaction - связанные поля коллекции в БД не попадут!
     * <p/>
     * NOTE2: не забыть: транзакцию коммитить снаружи этого метода!
     * 
     * @param loyTx
     *            транзакция лояльности, что надо сохранить в БД (без связанных полей-коллекций)
     * @return "сохраненную" версию аргумента (т.е. с уже заполненным {@link LoyTransactionEntity#getId() ID'шником}); вернет <code>null</code>, если
     *         не удалось выполнить операцию по любой причине
     */
    private LoyTransactionEntity addLoyTxOnly(LoyTransactionEntity loyTx) {
        LoyTransactionEntity result = null;
        long stopWatch = System.currentTimeMillis();
        
        log.trace("entering addLoyTxOnly(LoyTransactionEntity). The argument is: {}", loyTx);
        
        if (loyTx == null) {
            log.error("addLoyTxOnly: the argument is NULL!");
            return null;
        }
        
        // 1. Сначала зальем аргумент в БД
        // если останется NULL - значит не удалось заинзертить:
        Long idGenerated = null;
        try {
            PreparedStatement ps = getInsertLoyTxStatement();
            
            ps.setLong(1, loyTx.getCashNumber());
            ps.setBoolean(2, loyTx.isOperationType());
            ps.setLong(3, loyTx.getPurchaseNumber());
            ps.setTimestamp(4, loyTx.getSaleTime() == null ? null : new Timestamp(loyTx.getSaleTime().getTime()));
            ps.setLong(5, loyTx.getSentToServerStatus() == null ? null : loyTx.getSentToServerStatus().code);
            ps.setLong(6, loyTx.getShiftNumber());
            ps.setLong(7, loyTx.getShopNumber());
            ps.setInt(8, loyTx.getStatus());
            ps.setTimestamp(9, loyTx.getTransactionTime() == null ? null : new Timestamp(loyTx.getTransactionTime().getTime()));
            ps.setLong(10, loyTx.getDiscountValueTotal());
            ps.setString(11, loyTx.getFilename());
            ps.setLong(12, loyTx.getPurchaseAmount());
            ps.setBoolean(13, loyTx.isNeedSendToErp());
            ps.setBoolean(14, loyTx.isNeedSendBonus());
            ps.setBoolean(15, loyTx.isNeedSendAccumulation());
            ps.setString(16, loyTx.getInn());
            
            int updatedRows = ps.executeUpdate();
            if (updatedRows == 1) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    idGenerated = keys.getLong(1);
                    log.trace("ID generated: {}", idGenerated);
                } else {
                    // как так?!
                    log.warn("addLoyTxOnly: seems no ID was generated!");
                }
                keys.close();
            } else {
                log.error("addLoyTxOnly: failed to insert the row into the DB: updated rows [{}] in not 1!", updatedRows);
            }
        } catch (SQLException se) {
            log.error("addLoyTxOnly!", se);
        }
        
        // 2. а потом сформируем результат:
        if (idGenerated != null) {
            // просто перелить поля:
            result = getCopy(loyTx);
            
            // плюс ID'шник откорректировать:
            result.setId(idGenerated);
        }
        log.trace("leaving addLoyTxOnly(LoyTransactionEntity). The result is: {}; it took {} [ms]", 
            result, System.currentTimeMillis() - stopWatch);
        
        return result;
    }
    
    
    /**
     * Этот ANSI-SQL скрипт будет обновлять одну запись в таблицу loy_transaction.
     */
    private static final String UPDATE_LOY_TX = "UPDATE loy_transaction " +
            "SET cash_number=?, operation_type=?, purchase_number=?, sale_time=?, sent_to_server_status=?, shift_number=?, " +
            "shop_number=?, status=?, transaction_time=?, discountvalue=?, filename=?, purchase_amount=?, need_send_to_erp=?, need_send_bonus=?, " +
            "need_send_accumulation=?, inn=? " +
            "WHERE id=?";
    /**
     * Этот скомпилированный запрос будет отвечать за обновление (update) записей в таблицу loy_transaction.
     */
    private PreparedStatement updateLoyTxStatement;
    private PreparedStatement getUpdateLoyTxStatement() throws SQLException {
        if (updateLoyTxStatement == null || updateLoyTxStatement.isClosed()) {
            Connection con = getDbCon();
            updateLoyTxStatement = con.prepareStatement(UPDATE_LOY_TX);
        }
        
        return updateLoyTxStatement;
    }
    
    /**
     * Обновит ("проапдейтит") указанную транзакцию лояльности в БД.
     * <p/>
     * NOTE: обновится запись только в loy_transaction - в связанные поля коллекции операция не прокаскадирует!
     * <p/>
     * NOTE2: не забыть: транзакцию коммитить снаружи этого метода!
     * 
     * @param loyTx
     *            транзакция лояльности, что надо обновить в БД (без связанных полей-коллекций)
     * @return "сохраненную" версию аргумента - без полей-коллекций; вернет <code>null</code>, если не удалось выполнить операцию по любой причине
     */
    private LoyTransactionEntity updateLoyTxOnly(LoyTransactionEntity loyTx) {
        LoyTransactionEntity result = null;
        long stopWatch = System.currentTimeMillis();
        
        log.trace("entering updateLoyTxOnly(LoyTransactionEntity). The argument is: {}", loyTx);
        
        if (loyTx == null || loyTx.getId() == null) {
            log.error("updateLoyTxOnly: eitehr the argument, ot its ID is NULL!");
            return null;
        }
        
        // 1. Сначала зальем аргумент в БД
        boolean updateSuccessfull = false;
        try {
            PreparedStatement ps = getUpdateLoyTxStatement();
            
            ps.setLong(1, loyTx.getCashNumber());
            ps.setBoolean(2, loyTx.isOperationType());
            ps.setLong(3, loyTx.getPurchaseNumber());
            ps.setTimestamp(4, loyTx.getSaleTime() == null ? null : new Timestamp(loyTx.getSaleTime().getTime()));
            ps.setLong(5, loyTx.getSentToServerStatus() == null ? null : loyTx.getSentToServerStatus().code);
            ps.setLong(6, loyTx.getShiftNumber());
            ps.setLong(7, loyTx.getShopNumber());
            ps.setInt(8, loyTx.getStatus());
            ps.setTimestamp(9, loyTx.getTransactionTime() == null ? null : new Timestamp(loyTx.getTransactionTime().getTime()));
            ps.setLong(10, loyTx.getDiscountValueTotal());
            ps.setString(11, loyTx.getFilename());
            ps.setLong(12, loyTx.getPurchaseAmount());
            ps.setBoolean(13, loyTx.isNeedSendToErp());
            ps.setBoolean(14, loyTx.isNeedSendBonus());
            ps.setBoolean(15, loyTx.isNeedSendAccumulation());
            ps.setString(16, loyTx.getInn());
            
            // ID'шник:
            ps.setLong(16, loyTx.getId());
            
            int updatedRows = ps.executeUpdate();
            if (updatedRows == 1) {
                updateSuccessfull = true;
            } else {
                log.error("updateLoyTxOnly: failed to update the row in the DB: updated rows [{}] in not 1!", updatedRows);
            }
        } catch (SQLException se) {
            log.error("updateLoyTxOnly!", se);
        }
        
        // 2. а потом сформируем результат:
        if (updateSuccessfull) {
            // просто перелить поля:
            result = getCopy(loyTx);
        }
        log.trace("leaving updateLoyTxOnly(LoyTransactionEntity). The result is: {}; it took {} [ms]", 
            result, System.currentTimeMillis() - stopWatch);
        
        return result;
    }
    
    /**
     * Вернет "копию" аргумента без полей-коллекций - именно этим и отличается от {@link LoyTransactionEntity#clone()}.
     * 
     * @param loyTx
     *            транзакция лояльности, чью копию надо вернуть
     * @return копию аргумента
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    private LoyTransactionEntity getCopy(LoyTransactionEntity loyTx) {
        // просто перелить поля:
        LoyTransactionEntity result = new LoyTransactionEntity();
        
        // то, что заинзертили
        result.setCashNumber(loyTx.getCashNumber());
        result.setOperationType(loyTx.isOperationType());
        result.setPurchaseNumber(loyTx.getPurchaseNumber());
        result.setSaleTime(loyTx.getSaleTime());
        result.setSentToServerStatus(loyTx.getSentToServerStatus());
        result.setShiftNumber(loyTx.getShiftNumber());
        result.setShopNumber(loyTx.getShopNumber());
        result.setStatus(loyTx.getStatus());
        result.setTransactionTime(loyTx.getTransactionTime());
        result.setDiscountValueTotal(loyTx.getDiscountValueTotal());
        result.setFilename(loyTx.getFilename());
        result.setPurchaseAmount(loyTx.getPurchaseAmount());
        result.setNeedSendToErp(loyTx.isNeedSendToErp());
        result.setNeedSendBonus(loyTx.isNeedSendBonus());
        result.setNeedSendAccumulation(loyTx.isNeedSendAccumulation());
        result.setInn(result.getInn());
        
        // плюс ID'шник:
        result.setId(loyTx.getId());
        
        return result;
    }

    /**
     * Просто {@link Connection#commit() закоммитит текущую транзакцию}, если <code>success</code> == <code>true</code>; иначе -
     * {@link Connection#rollback() откатит ее}.
     * 
     * @param success
     *            флаг-признак: коммитить или откатывать текущую транзакцию
     * @return <code>true</code>, если удалось выполнить задуманное; иначе (если внутри метода отловили ошибку) - вернет <code>false</code>
     */
    private boolean commitTx(boolean success) {
        boolean result = true;
        
        try {
            if (success) {
                getDbCon().commit();
            } else {
                getDbCon().rollback();
            }
        } catch (SQLException se) {
            log.error(String.format("commitTx: failed to %s the current TX!", success ? "COMMIT" : "ROLL-BACK"), se);
            result = false;
        }
        
        return result;
    }

    @Override
    public LoyTransactionEntity saveLoyTx(LoyTransactionEntity loyTx) {
        LoyTransactionEntity result = null;
        
        // Алгоритм
        // 1. Сохранить саму транзакцию без полей-коллекций:
        //  1.1. Если еще не существует ID (у аргумента) - по добавить/"заинзертить" - получить ID и использовать далее в этом методе
        //  1.2. Иначе - обновить уже существующую запись в БД (про-апдейтить);
        // 2. Сохранить ВСЕ LoyAdvActionInPurchaseEntity:
        //  2.1. Пробежать по всем полям-коллекциям аргумента, имеющим обязательную ссылку на "дескриптор" РА 
        //      (позиции, карты, бонусы, купоны...) и собрать эти LoyAdvActionInPurchaseEntity, что упоминаются;
        //  2.2. вытянуть из БД все LoyAdvActionInPurchaseEntity имеющие такие же GUID'ы. что и объекты из 2.1;
        //  2.3. те объекты из 2.1., что уже в БД - проапдейтить, а те, что еще нет - заинзертить;
        //  2.4. эти уже сохраненные LoyAdvActionInPurchaseEntity запомнить в отдельной мапе - будем пользовать при формировании результата;
        // 3. Сохранить поля-коллекции аргумента: их 11 (одиннадцать!) штук (NOTE: использовать ID из 1. и GUID из 2.):
        //  3.1. если ID у текущего элемента поля-коллекции NULL - заинзертить - получить ID и использовать его при формировании результата;
        //  3.2. иначе - обновить уже существующую запись в БД (про-апдейтить).
        // 4. Закоммитить транзакцию (commitTx(result != null));
        // 5. Просто "склеить" все что получилось на предыдущих этапах - это и будет результат.
        
        // TODO
        
        
        // 1. Сохранить саму транзакцию без полей-коллекций:
        //TODO ....
        
        
        
        
        result = addLoyTxOnly(loyTx);
        result = updateLoyTxOnly(result);

        
        
        
        
        
        // TODO Auto-generated method stub
        
        
        commitTx(result != null);
        
        return result;
    }

    @Override
    public LoyTransactionEntity discardDiscountsAndSave(LoyTransactionEntity loyTx, Collection<LoyDiscountPositionEntity> positionDiscounts,
                                                        Collection<LoyDiscountPositionEntity> correctDiscounts) {
        // TODO
        return null;
    }

    @Override
    public LoyTransactionEntity getLoyTxById(long loyTxId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LoyTransactionEntity linkTxToReceipt(LoyTransactionEntity loyTx, PurchaseEntity receipt) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public LoyAdvActionInPurchaseEntity getLoyAdvActionByGuid(long guid) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LoyAdvActionInPurchaseEntity saveLoyAdvAction(LoyAdvActionInPurchaseEntity loyAdvAction) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public int removeLoyTxByIds(Collection<Long> ids) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public LoyTransactionEntity getLoyTxByReceipt(PurchaseEntity purchase) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public int deleteSberbankTransactions(Collection<Long> ids) {
        // TODO Auto-generated method stub
        return 0;
    }
    
    @Override
    public LoyTransactionEntity saveSosbs(Collection<LoyBonusSberbankTransactionEntity> bonusTxes) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public List<LoyTransactionEntity> getLoyTxesByStatus(Collection<SentToServerStatus> statuses, int maxResults) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public int markLoyTxesAsNotSent(Collection<String> fileNames) {
        // TODO Auto-generated method stub
        return 0;
    }
    
    @Override
    public int setTxStatus(Collection<Long> ids, String fileName, SentToServerStatus status) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int setTxStatusAndFileNames(Collection<Long> ids, String fileName, SentToServerStatus status) {
        // TODO Auto-generated method stub
        return 0;
    }
    
    // init-method
    /**
     * Инициализация. Должна быть вызвана сразу же после создания после создания этого бина и DI (dependency injections) - до вызова любого
     * бизнес-метода.
     */
    public void postConstruct() {
        try {
            getDbCon();
        } catch (SQLException se) {
            // неудача! 
            log.error(String.format("failed to create dbCon! connection-properties were: %s; and the dbUrl: %s",
                getJdbcProperties(), getJdbcUrl()), se);
            
            // пробросим дальше: Этот объект не имеет смысла - его нельзя использовать
            throw new RuntimeException("failed to create dbCon!", se);
        }
    }    
    
    // destroy-method
    /**
     * Другой метод жизненного цикла (Lifecycle method): этот следует вызывать перед уничтожением этого объекта.
     */
    public void preDestroy() {
        // надо закрыть коннекцию
        try {
            if (dbCon != null && !dbCon.isClosed()) {
                dbCon.close();
            }
        } catch (SQLException se) {
            // по крайней мере постарались выйти чисто
            log.error("failed to close dbCon!", se);
        }
    }

    /**
     * Сам геттер для получения коннекции к БД. Будем иметь только один и всегда открытый - для повышения производительности.
     * <p/>
     * Note: возвращенная коннекция не будет авто-коммитить транзакции после каждого запроса ({@link Connection#getAutoCommit()} == <code>false</code>
     * ) - надо будет самому ручками коммитить эти транзакции (см. {@link Connection#commit()}).
     * 
     * @return
     * @throws SQLException
     */
    private Connection getDbCon() throws SQLException {
        if (dbCon == null || dbCon.isClosed()) {
            dbCon = DriverManager.getConnection(getJdbcUrl(), getJdbcProperties());
            
            // мы собираемся сохранять "ветвистые" объекты - а это может потребовать сначала сделать
            //  несколько INSERT'ов чтоб просто поиметь авто-сгенеренные ID'шники основных сущностей
            //  поэтому транзакции всегда будем коммитить сами в явном виде
            dbCon.setAutoCommit(false);
        }

        return dbCon;
    }

    public Properties getJdbcProperties() {
        return jdbcProperties;
    }

    public void setJdbcProperties(Properties jdbcProperties) {
        this.jdbcProperties = jdbcProperties;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public IdentifierGenerator getIdGenerator() {
        return idGenerator;
    }

    public void setIdGenerator(IdentifierGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }
}
