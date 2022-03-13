package ru.crystals.pos.bank.translink;

import ru.crystals.utils.time.Timer;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimerSupplier {

    private TranslinkConfig config;

    public void configure(TranslinkConfig config) {
        this.config = config;
    }

    public Timer getCloseDayTimer() {
        return Timer.of(Duration.ofSeconds(config.getCloseDayTimeout()));
    }

    public void sleep(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }

    public Timer getCardReadTimer() {
        return Timer.of(Duration.ofSeconds(config.getCardReadTimeout()));
    }

    public LocalDateTime getNowTime() {
        return LocalDateTime.now();
    }
}
