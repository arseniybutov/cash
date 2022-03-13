package ru.crystals.pos.fiscalprinter.pirit.core.connect;

public class PingStatus {
    private boolean isOnline;
    private boolean isReceivedStx;

    public PingStatus(boolean isOnline, int stx) {
        this(isOnline);
        isReceivedStx = (stx == 0x02);
    }

    public PingStatus(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public boolean isReceivedStx() {
        return isReceivedStx;
    }
}
