package ru.crystals.pos.fiscalprinter.de.fcc;

import ru.crystals.pos.fiscalprinter.datastruct.documents.ShiftCounters;

/**
 *
 * @author dalex
 */
public class KPKCountersVO {

    private long kpk;
    private long spnd;
    private boolean shiftOpen = false;
    private ShiftCounters shiftCounters;

    public KPKCountersVO() {
    }

    public long getKpk() {
        return kpk;
    }

    public void setKpk(long kpk) {
        this.kpk = kpk;
    }

    public long getSpnd() {
        return spnd;
    }

    public void setSpnd(long spnd) {
        this.spnd = spnd;
    }

    public boolean isShiftOpen() {
        return shiftOpen;
    }

    public void setShiftOpen(boolean shiftOpen) {
        this.shiftOpen = shiftOpen;
    }

    public ShiftCounters getShiftCounters() {
        return shiftCounters;
    }

    public void setShiftCounters(ShiftCounters shiftCounters) {
        this.shiftCounters = shiftCounters;
    }
}
