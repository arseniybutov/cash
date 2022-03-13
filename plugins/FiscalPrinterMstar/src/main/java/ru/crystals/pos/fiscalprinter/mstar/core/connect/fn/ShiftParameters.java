package ru.crystals.pos.fiscalprinter.mstar.core.connect.fn;

import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.transport.mstar.DataPacket;

/**
 * Параметры текущей смены
 */
public class ShiftParameters {

    /**
     * Состояние смены
     */
    private long shiftState;
    /**
     * Номер смены
     */
    private long shiftNumber;
    /**
     * Номер чека
     */
    private long checkNumber;

    public ShiftParameters(DataPacket dp) throws FiscalPrinterException {
        try {
            shiftState = dp.getLongValue(0);
            shiftNumber = dp.getLongValue(1);
            checkNumber = dp.getLongValue(2);
        } catch (Exception ex) {
            throw new FiscalPrinterException("Error parse DataPacket", ex);
        }
    }

    public long getShiftState() {
        return shiftState;
    }

    public long getShiftNumber() {
        return shiftNumber;
    }

    public long getCheckNumber() {
        return checkNumber;
    }

    public boolean isShiftOpen() {
        return shiftState == 1;
    }
}
