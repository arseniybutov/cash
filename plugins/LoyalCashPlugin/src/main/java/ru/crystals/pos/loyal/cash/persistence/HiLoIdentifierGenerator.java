package ru.crystals.pos.loyal.cash.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Эта версия реализации {@link IdentifierGenerator генератора ключей} является попыткой по-подражать {@link org.hibernate.id.SequenceHiLoGenerator
 * нормальному генератору ключей} - пишем свой велосипед только потому, что готовимся отказаться от hibernate'а.
 * 
 * @author aperevozchikov
 */
public class HiLoIdentifierGenerator implements IdentifierGenerator {
    
    /**
     * черный ящик
     */
    private static Logger log = LoggerFactory.getLogger(HiLoIdentifierGenerator.class);

    /**
     * Запрос, с помощью которого будем получать следующее значение в последовательности - для "резервирования" диапазона ключей
     */
    private static final String SELECT_NEXT_VAL = "select nextval('" + IdentifierGenerator.SEQUENCE_NAME + "')";
    
    /**
     * Нижняя граница "зарезервированного" диапазона ключей
     */
    private long lo = 0L;
    
    /**
     * Верхняя граница "зарезервированного" диапазона ключей
     */
    private long hi = 0L;
    
    /**
     * следующий идентификатор (ключ), что собираемся вернуть по запросу. Должен быть lo <= next <= hi; если нет - значит пора резервировать следующий
     * диапазон ключей
     */
    private long next = 1L;

    /**
     * просто уже заготовленный и скомпилированный запрос для извлечения следующего значения из последовательности. Нам часто придется его выполнять -
     * поэтому будем его хранить.
     */
    private PreparedStatement ps = null;
    
    /**
     * По факту вернет {@link #ps}, создав и скомпилировав его если еще он не готов.
     * 
     * @param connection
     *            коннекция до БД, через который возвращенный Statement будет выполнять запросы
     * @return {@link #ps}
     * @throws SQLException
     *             если пришлось создавать этот {@link #ps} (он был не готов) и потерпели неудачу
     */
    private PreparedStatement getStatement(Connection connection) throws SQLException {
        if (ps == null || ps.isClosed()) {
            ps = connection.prepareStatement(SELECT_NEXT_VAL);
        }
        return ps;
    }

    // TODO: вообще никакого Exception handling'а!
    @Override
    public synchronized long generate(Connection connection) throws SQLException {
        if (lo <= next && next <= hi) {
            // норма: next - и есть результат
        } else {
            // придется лезть в БД
            long stopWatch = System.currentTimeMillis();
            
            PreparedStatement ps = getStatement(connection);
            ResultSet rs = ps.executeQuery();
            rs.next();
            
            long nextVal = rs.getLong(1);
            lo = ALLOCATION_SIZE * (nextVal - 1);
            hi = ALLOCATION_SIZE * nextVal - 1;
            next = lo;
            
            if (log.isTraceEnabled()) {
                log.trace("generate: new interval (of primary keys) reserved: [{}; {}]; it took {} [ms]",
                    new Object[] {lo, hi, System.currentTimeMillis() - stopWatch});
            }
        }
        
        return next++;
    }

}
