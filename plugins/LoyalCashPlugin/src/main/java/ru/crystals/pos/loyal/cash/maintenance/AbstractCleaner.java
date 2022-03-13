package ru.crystals.pos.loyal.cash.maintenance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.pos.InternalCashPoolExecutor;

import java.util.concurrent.TimeUnit;

/**
 * Бин отвечает за запуск процесса/потока.
 *
 * @author aperevozchikov & aryazantsev
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class AbstractCleaner {
    private static final Logger log = LoggerFactory.getLogger(AbstractCleaner.class);

    /**
     * Задержка м/у завершением удаления предыдущей "порции" устаревших TX лояльности и запуском процесса удаления следующей "порции",
     * в секундах.
     * <p/>
     * NOTE: не положительное значение эффективно отключает функционал.
     */
    // injected by Spring
    private int cleanerIntervalInSeconds = (int) TimeUnit.HOURS.toSeconds(1);

    /**
     * Задержка [после старта кассы] перед началом удаления первой "порции" устаревших записей, в секундах.
     */
    // injected by Spring
    private int cleanerInitialDelayInSeconds = 30;

    /**
     * Максимальное количество TX лояльности, что можно удалить за одну итерацию
     */
    // injected by Spring
    private int maxRecordsToRemoveAtOnce = 1000;

    @Autowired
    private InternalCashPoolExecutor executor;

    /**
     * Метод типа "@PostConstruct" - будет вызван Spring'ом по завершению создания и инициализации данного объекта.
     */
    public void start() {
        log.trace("entering start()");

        if (cleanerIntervalInSeconds <= 0) {
            log.info("leaving start(): the feature is disabled: cleanerIntervalInSeconds parameter is non-positive [{}]", cleanerIntervalInSeconds);
            return;
        }

        if (maxRecordsToRemoveAtOnce <= 0) {
            log.info("leaving start(): the feature is disabled: maxRecordsToRemoveAtOnce parameter is non-positive [{}]", maxRecordsToRemoveAtOnce);
            return;
        }

        scheduleWork(executor);

        log.info("cleaner thread was scheduled [initial-delay: {}; interval: {}; to-remove-at-once: {}]",
                cleanerInitialDelayInSeconds, cleanerIntervalInSeconds, maxRecordsToRemoveAtOnce);

        log.trace("leaving start()");
    }

    /**
     * Запуск задания по расписанию по очищению чего-либо
     * @param executor пул потоков для запуска задания
     */
    protected abstract void scheduleWork(InternalCashPoolExecutor executor);

    public int getCleanerIntervalInSeconds() {
        return cleanerIntervalInSeconds;
    }

    public void setCleanerIntervalInSeconds(int cleanerIntervalInSeconds) {
        this.cleanerIntervalInSeconds = cleanerIntervalInSeconds;
    }

    public int getCleanerInitialDelayInSeconds() {
        return cleanerInitialDelayInSeconds;
    }

    public void setCleanerInitialDelayInSeconds(int cleanerInitialDelayInSeconds) {
        this.cleanerInitialDelayInSeconds = cleanerInitialDelayInSeconds;
    }

    public int getMaxRecordsToRemoveAtOnce() {
        return maxRecordsToRemoveAtOnce;
    }

    public void setMaxRecordsToRemoveAtOnce(int maxRecordsToRemoveAtOnce) {
        this.maxRecordsToRemoveAtOnce = maxRecordsToRemoveAtOnce;
    }
}
