package ru.crystals.pos.bank.pbf;

import ru.crystals.pos.bank.TerminalConfiguration;

import java.util.concurrent.TimeUnit;

public class PBFTerminalConfig {

    private TerminalConfiguration baseConfiguration;
    private static final long TO1 = TimeUnit.MINUTES.toMillis(2);
    private static final long TO2 = TimeUnit.SECONDS.toMillis(15);
    private long overallTimeOut = TO1;
    private long readByteTimeOut = TO2;

    public TerminalConfiguration getBaseConfiguration() {
        return baseConfiguration;
    }

    public void setBaseConfiguration(TerminalConfiguration baseConfiguration) {
        this.baseConfiguration = baseConfiguration;
    }

    public void setOverallTimeOut(long overallTimeOut) {
        this.overallTimeOut = overallTimeOut;
    }

    public long getOverallTimeOut() {
        return overallTimeOut;
    }

    public void setReadByteTimeOut(long readByteTimeOut) {
        this.readByteTimeOut = readByteTimeOut;
    }

    public long getReadByteTimeOut() {
        return readByteTimeOut;
    }
}
