package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;


/** Состояние фискального накопителя
 * Created by Tatarinov Eduard on 16.12.16.
 */

public class ShtrihFNStateOne {

    private String fnNum;
    private long lastFdNum;
    private boolean isShiftOpen;


    public String getFnNum() {
        return fnNum;
    }

    public void setFnNum(String fnNum) {
        this.fnNum = fnNum;
    }

    public long getLastFdNum() {
        return lastFdNum;
    }

    public void setLastFdNum(long lastFdNum) {
        this.lastFdNum = lastFdNum;
    }

    public boolean isShiftOpen() {
        return isShiftOpen;
    }

    public void setShiftOpen(boolean shiftOpen) {
        isShiftOpen = shiftOpen;
    }

    @Override
    public String toString() {
        return "ShtrihFNStateOne{" +
                "fnNum=" + fnNum +
                ", lastFdNum=" + lastFdNum +
                ", isShiftOpen=" + isShiftOpen +
                '}';
    }
}
