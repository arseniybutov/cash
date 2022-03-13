package ru.crystals.pos.loyal.cash.maintenance;

import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.InternalCashPoolExecutor;
import ru.crystals.pos.loyal.cash.persistence.LoyTxDao;
import ru.crystals.pos.property.Properties;

import java.util.concurrent.TimeUnit;

/**
 * На самом деле данный бин будет только отвечать за запуск процесса/потока, что будет непосредственно удалять устаревшие TX лояльности.
 *
 * @author aperevozchikov
 */
public class StaleLoyTransactionsCleaner extends AbstractCleaner {

    /**
     * DAO, через который и будем вызывать удаление старых TX лояльности
     */
    @Autowired
    private LoyTxDao dao;

    /**
     * Настройки кассы
     */
    @Autowired
    private Properties prop;

    @Override
    protected void scheduleWork(InternalCashPoolExecutor executor) {
        Runnable workhorse = new LoyTxCleanerWorkhorse(dao, prop, getMaxRecordsToRemoveAtOnce());

        executor.scheduleWithFixedDelay(workhorse, getCleanerInitialDelayInSeconds(), getCleanerIntervalInSeconds(), TimeUnit.SECONDS);
    }
}
