package ru.crystals.pos.fiscalprinter.pirit.core.connect.fn;

import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.transport.DataPacket;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * @author Tatarinov Eduard
 */
public class StatusOFD {

    private long status;
    private long countDocForOFD;
    private long numFirstNoSentDoc;
    private String dateFistNoSentDocOriginStr;
    private LocalDateTime firstNotSentDocDateTime;

    public StatusOFD(DataPacket dp) throws FiscalPrinterException {
        try {
            if (dp.getLongValue(0) == 7L) {
                status = dp.getLongValue(1);
                try {
                    countDocForOFD = dp.getLongValue(2);
                    numFirstNoSentDoc = dp.getLongValue(3);
                } catch (NumberFormatException pe) {
                    //Ничего не делаем, просто значения этих полей в dp могут быть пустыми строками
                }
                dateFistNoSentDocOriginStr = dp.getStringValue(4);
                try {
                    firstNotSentDocDateTime = dp.getOptionalDateTimeValue(4, 5).orElse(null);
                } catch (DateTimeParseException pe) {
                    //Ничего не делаем, просто значения этих полей в dp могут быть пустыми строками
                }
            }
        } catch (Exception ex) {
            throw new FiscalPrinterException("Error parse DataPacket", ex);
        }
    }

    public long getStatus() {
        return status;
    }

    public long getCountDocForOFD() {
        return countDocForOFD;
    }

    public long getNumFirstNoSentDoc() {
        return numFirstNoSentDoc;
    }

    public String getDateFistNoSentDocOriginStr() {
        return dateFistNoSentDocOriginStr;
    }

    public LocalDateTime getFirstNotSentDocDateTime() {
        return firstNotSentDocDateTime;
    }

    @Override
    public String toString() {
        return "StatusOFD{ " + "status = " + status +
                ", countDocForOFD = " + countDocForOFD +
                ", numFirstNoSentDoc = " + numFirstNoSentDoc +
                ", firstNotSentDocDateTime = " + firstNotSentDocDateTime + "}";
    }
}
