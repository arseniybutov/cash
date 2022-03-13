package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto;

import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.NFDMode;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResultObject;

import java.util.Date;

import static ru.crystals.pos.fiscalprinter.nfd.transport.deserializers.ResponseDeserializerUtils.DTO_PREFIX;


public class State extends BaseResultObject {

    public static final String TYPE_NAME = DTO_PREFIX + "State";

    private NFDMode mode;

    private Date lastDateTime;

    private Long continuousDocumentNumber;

    private Long shiftNumber;

    public NFDMode getMode() {
        return mode;
    }

    public void setMode(NFDMode mode) {
        this.mode = mode;
    }

    public Date getLastDateTime() {
        return lastDateTime;
    }

    public void setLastDateTime(Date lastDateTime) {
        this.lastDateTime = lastDateTime;
    }

    public Long getContinuousDocumentNumber() {
        return continuousDocumentNumber;
    }

    public void setContinuousDocumentNumber(Long continuousDocumentNumber) {
        this.continuousDocumentNumber = continuousDocumentNumber;
    }

    public Long getShiftNumber() {
        // like pirit
        if (mode.equals(NFDMode.SHIFT_CLOSED)) {
            return shiftNumber + 1;
        }
        return shiftNumber;
    }

    public void setShiftNumber(Long shiftNumber) {
        this.shiftNumber = shiftNumber;
    }

    @Override
    public String toString() {
        return "State{" +
                "mode=" + mode +
                ", lastDateTime=" + lastDateTime +
                ", continuousDocumentNumber=" + continuousDocumentNumber +
                ", shiftNumber=" + shiftNumber +
                '}';
    }
}
