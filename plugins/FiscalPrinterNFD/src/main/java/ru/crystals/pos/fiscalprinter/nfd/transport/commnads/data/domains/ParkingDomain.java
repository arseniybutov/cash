package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.domains;

import java.util.Date;

public class ParkingDomain extends CommonDomain {

    /**
     * Дата и время начала.
     */
    private String startDateTimeFieldName = "startDateTime";

    /**
     * ДДата и время окончания.
     */
    private String endDateTimeFieldName = "endDateTime";

    public ParkingDomain(String startDateTimeFieldName, String endDateTimeFieldName) {
        type = "emul:ParkingDomain";
        this.startDateTimeFieldName = startDateTimeFieldName;
        this.endDateTimeFieldName = endDateTimeFieldName;
    }

    public Date getStartDateTime() {
        return (Date) getParam(startDateTimeFieldName);
    }

    public void setStartDateTime(Date startDateTime) {
        addParam(startDateTimeFieldName, startDateTime);
    }

    public Date getEndDateTime() {
        return (Date) getParam(endDateTimeFieldName);
    }

    public void setEndDateTime(Date endDateTime) {
        addParam(endDateTimeFieldName, endDateTime);
    }

}
