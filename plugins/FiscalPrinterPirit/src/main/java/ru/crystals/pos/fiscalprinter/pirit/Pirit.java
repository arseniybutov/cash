package ru.crystals.pos.fiscalprinter.pirit;

import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.pirit.core.AbstractPirit;

import java.util.Date;

@PrototypedComponent
public class Pirit extends AbstractPirit {

    @Override
    public void start() throws FiscalPrinterException {
        startInner();
    }

    @Override
    public String getEklzNum() throws FiscalPrinterException {
        return pa.getEklzNum();
    }

    @Override
    public String getINN() throws FiscalPrinterException {
        return pa.getINN();
    }

    @Override
    public long openShift(Cashier cashier) throws FiscalPrinterException {
        return pa.getShiftNumber();
    }

    /**
     * Метод возвращает номер текущей смены, если открытой смены нет - то номер последней закрытой
     */
    @Override
    public long getShiftNumber() throws FiscalPrinterException {
        return pa.getShiftNumber();
    }

    @Override
    public long getLastKpk() throws FiscalPrinterException {
        return pa.getLastKpk();
    }

    @Override
    public Date getEKLZActivizationDate() throws FiscalPrinterException {
        return pa.getEKLZActivizationDate();
    }
}
