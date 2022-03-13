package ru.crystals.pos.bank.commonsbpprovider;

import java.time.OffsetDateTime;

public class SBPTimeSupplier {

    public void sleep(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }

    public OffsetDateTime getNowTime() {
        return OffsetDateTime.now();
    }

    public OffsetDateTime getExpirationTime(Long minutes) {
        return OffsetDateTime.now().plusMinutes(minutes);
    }
}
