package ru.crystals.pos.bank.ucs.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Timer {

    private static Logger log = LoggerFactory.getLogger(Timer.class);

    private long timeout;
    private long startTime;
    private String timerName;

    public Timer(long timeout) {
        this.timeout = timeout;
        start();
    }

    public Timer() {

    }

    public Timer(String timerName) {
        this.timerName = timerName;
    }

    public Timer(String timerName, long timeout) {
        this.timeout = timeout;
        this.timerName = timerName;
        start();
    }


    public boolean isNotExpired() {
        return !isExpired();
    }

    public boolean isExpired() {
        if (System.currentTimeMillis() - startTime > timeout) {
            logActionString("expired");
            return true;
        }
        return false;
    }

    public long getRemainTime() {
        return timeout - (System.currentTimeMillis() - startTime);
    }

    public Timer start() {
        startTime = System.currentTimeMillis();
        logActionString("started");
        return this;
    }

    public Timer restart(long timeout) {
        this.timeout = timeout;
        startTime = System.currentTimeMillis();
        logActionString("restarted");
        return this;
    }

    public Timer restart() {
        startTime = System.currentTimeMillis();
        logActionString("restarted");
        return this;
    }

    private void logActionString(String action) {
        if (timerName != null && log.isTraceEnabled()) {
            log.trace("Timer {} ({}) {}", timerName, timeout, action);
        }
    }
}
