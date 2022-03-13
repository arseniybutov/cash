package ru.crystals.pos.bank.raiffeisensbp;

import java.time.OffsetDateTime;
import java.util.UUID;

public class SBPTimeSupplier {

    public void sleep(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }

    public OffsetDateTime getNowTime() {
        return OffsetDateTime.now();
    }

    /**
     * Генерация уникального идентификатора для запросов на оплату/возврат
     *
     * @return уникальный идентификатор
     */
    public String getIdForOperation() {
        return UUID.randomUUID().toString();
    }
}
