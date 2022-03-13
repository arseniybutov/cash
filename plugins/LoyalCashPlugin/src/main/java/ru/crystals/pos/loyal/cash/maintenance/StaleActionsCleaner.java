package ru.crystals.pos.loyal.cash.maintenance;

import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.InternalCashPoolExecutor;
import ru.crystals.pos.loyal.cash.service.CashAdvertisingActionDao;

import java.util.concurrent.TimeUnit;

/**
 * Бин отвечает за запуск процесса/потока, что будет непосредственно удалять устаревшие РА.
 *
 * @author aperevozchikov & aryazantsev
 */
@SuppressWarnings("unused")
public class StaleActionsCleaner extends AbstractCleaner {
    /**
     * Через сколько дней после завершения РА можно удалять эту РА из БД.
     */
    // injected by Spring
    private int daysAfterFinishToRemove = 1;

    @Autowired
    private CashAdvertisingActionDao actionsDao;

    @Override
    protected void scheduleWork(InternalCashPoolExecutor executor) {
        Runnable workhorse = new ActionsCleanerWorkhorse(actionsDao, getMaxRecordsToRemoveAtOnce(), getDaysAfterFinishToRemove());

        executor.scheduleWithFixedDelay(workhorse, getCleanerInitialDelayInSeconds(), getCleanerIntervalInSeconds(), TimeUnit.SECONDS);
    }

    public int getDaysAfterFinishToRemove() {
        return daysAfterFinishToRemove;
    }

    public void setDaysAfterFinishToRemove(int daysAfterFinishToRemove) {
        this.daysAfterFinishToRemove = daysAfterFinishToRemove;
    }
}
