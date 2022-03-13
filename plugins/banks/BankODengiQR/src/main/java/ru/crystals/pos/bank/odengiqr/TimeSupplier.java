package ru.crystals.pos.bank.odengiqr;

import java.time.Instant;

public class TimeSupplier {

    public Instant now() {
        return Instant.now();
    }

    public void sleep(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }
}
