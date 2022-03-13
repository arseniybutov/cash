package ru.crystals.pos.fiscalprinter.uz.fiscaldrive;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class EmulatedCountersVO {

    private long cashAmount;

    @JsonSerialize
    private SoftShift softShift;

    public EmulatedCountersVO() {
    }

    public long getCashAmount() {
        return cashAmount;
    }

    public void setCashAmount(long cashAmount) {
        this.cashAmount = cashAmount;
    }

    public SoftShift getSoftShift() {
        return softShift;
    }

    public void setSoftShift(SoftShift softShift) {
        this.softShift = softShift;
    }

}
