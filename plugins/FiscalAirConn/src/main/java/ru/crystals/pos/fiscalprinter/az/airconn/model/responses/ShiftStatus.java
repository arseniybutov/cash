package ru.crystals.pos.fiscalprinter.az.airconn.model.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ShiftStatus {
    @JsonProperty("shift_open")
    private Boolean shiftOpen;
    @JsonProperty("shift_open_time")
    private String shiftOpenTime;

    public Boolean getShiftOpen() {
        return shiftOpen;
    }

    public void setShiftOpen(Boolean shiftOpen) {
        this.shiftOpen = shiftOpen;
    }

    public String getShiftOpenTime() {
        return shiftOpenTime;
    }

    public void setShiftOpenTime(String shiftOpenTime) {
        this.shiftOpenTime = shiftOpenTime;
    }
}
