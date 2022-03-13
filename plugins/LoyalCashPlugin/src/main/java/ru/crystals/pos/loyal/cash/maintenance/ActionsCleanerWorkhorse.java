package ru.crystals.pos.loyal.cash.maintenance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.pos.loyal.cash.service.CashAdvertisingActionDao;

/**
 * Поток, удаляющий устаревшие РА.
 *
 * @author aperevozchikov & aryazantsev
 */
@SuppressWarnings("unused")
public class ActionsCleanerWorkhorse implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ActionsCleanerWorkhorse.class);

    /**
     * Максимальное количество РА, что можно удалить за одну итерацию
     */
    private int maxRecordsToRemoveAtOnce;

    /**
     * Через сколько дней после завершения РА можно удалять эту РА из БД.
     */
    private int daysAfterFinishToRemove;

    /**
     * А через эту штуку будем добывать из БД инфу о существующих активных {@link AdvertisingActionEntity РА}.
     */
    private CashAdvertisingActionDao actionsDao;

    ActionsCleanerWorkhorse(CashAdvertisingActionDao actionsDao, int maxRecordsToRemoveAtOnce, int daysAfterFinishToRemove) {
        this.actionsDao = actionsDao;
        this.maxRecordsToRemoveAtOnce = maxRecordsToRemoveAtOnce;
        this.daysAfterFinishToRemove = daysAfterFinishToRemove;
    }

    @Override
    public void run() {
        log.trace("entering run()");

        int deleted = actionsDao.removeStaleActions(daysAfterFinishToRemove, maxRecordsToRemoveAtOnce);

        log.trace("leaving run(), removed {} stale actions", deleted);
    }
}
