package ru.crystals.pos.fiscalprinter.atol3.highlevel.results;

import java.util.Calendar;
import java.util.Date;
import ru.crystals.pos.fiscalprinter.atol3.ResBundleFiscalPrinterAtol;
import ru.crystals.pos.fiscalprinter.atol3.StateMode;
import ru.crystals.pos.fiscalprinter.atol3.highlevel.types.ValueDecoder;
import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.Response;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

public class Status extends Result {
    // 44h
    public final long cashier;
    public final int number;
    public final Date dateTime;
    private final int flags;
    public final String serialNumber;
    public final int model;
    public final String protocolVersion;
    private final int mode;
    //        public final int checkNumber;
    private final long shiftNumber;
    private final int checkStatus;
    //        public final int checkSumma;
    public final int decimalPoint;
    public final int port;

    public Status(Response response) {
        byte[] data = response.getData();
        int index = response.getDataOffset() + 1;
        cashier = ValueDecoder.LONG.decode(data, index, 1); index += 1;
        number = data[index]; index += 1;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, ValueDecoder.LONG.decode(data, index, 1).intValue());
        calendar.set(Calendar.MONTH, ValueDecoder.LONG.decode(data, index + 1,1).intValue() - 1);
        calendar.set(Calendar.DAY_OF_MONTH, ValueDecoder.LONG.decode(data, index + 2, 1).intValue());
        index += 3;

        calendar.set(Calendar.HOUR_OF_DAY, ValueDecoder.LONG.decode(data, index, 1).intValue());
        calendar.set(Calendar.MINUTE, ValueDecoder.LONG.decode(data, index + 1, 1).intValue());
        calendar.set(Calendar.SECOND, ValueDecoder.LONG.decode(data, index + 2, 1).intValue());
        index += 3;

        dateTime = calendar.getTime();

        flags = data[index]; index += 1;
        serialNumber = ValueDecoder.LONG.decode(data, index, 4).toString(); index += 4;
        model = data[index]; index += 1;
        protocolVersion = ValueDecoder.ATOL_STRING.decode(data, index, 1) + "." +
                ValueDecoder.ATOL_STRING.decode(data, index + 1, 1); index += 2; // protocol version
        mode = data[index]; index += 1;
        index += 2; // check number
        shiftNumber = ValueDecoder.LONG.decode(data, index, 2); index += 2; // shift number
        checkStatus = data[index]; index += 1;
        index += 5; // check summa
        decimalPoint = data[index]; index += 1;
        port = data[index];
    }

    public StateMode getMode() throws FiscalPrinterException {
        int mode = this.mode & 0xF;

        StateMode result;
        if (mode < StateMode.values().length) {
            result = StateMode.values()[mode];
        } else {
            throw new FiscalPrinterException(ResBundleFiscalPrinterAtol.getString("REBOOT_PRINTER"));
        }
        return result;
    }

    public boolean isDrawerOpen(boolean inverted) {
        boolean result = (flags & 4) == 0;

        if (inverted) {
            result = !result;
        }

        return result;
    }

    public boolean isShiftOpen() {
        return (flags & 2) == 2;
    }

    public boolean isCapOpen() {
        return (flags & 32) == 32;
    }

    public boolean isFiscalMode() {
        return (flags & 1) == 1;
    }

    public boolean isDocOpen() {
        return checkStatus != 0;
    }

    /**
     * Возвращает номер смены по следующему Z-отчету
     * Так этого ожидает TechProcessShift
     *
     * @see ru.crystals.pos.techprocess.TechProcessShift#synchronizeShifts()
     */
    public long getShiftNumber() {
        return shiftNumber + 1;
    }
}
