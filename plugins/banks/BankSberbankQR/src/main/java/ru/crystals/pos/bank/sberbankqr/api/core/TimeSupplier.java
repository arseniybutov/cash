package ru.crystals.pos.bank.sberbankqr.api.core;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimeSupplier {

    public ZonedDateTime currentMoscowTime() {
        ZoneId zoneId = ZoneId.of("Europe/Moscow");
        return ZonedDateTime.now(zoneId);
    }

    public void sleep(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }
}
