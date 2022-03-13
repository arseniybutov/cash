package ru.crystals.pos.fiscalprinter.pirit.vikiprint.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ShiftCounters;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.FiscalPrinterData;

public class VikiPrintFM15Envd extends AbstractVikiPrint {
    protected final Logger log = LoggerFactory.getLogger(VikiPrintFM15Envd.class);
    private FiscalPrinterData fiscalData;

    @Override
    public String getEklzNum() throws FiscalPrinterException {
        return null;
    }

    @Override
    public String getINN() throws FiscalPrinterException {
        return pa.getINN();
    }

    @Override
    public long getShiftNumber() throws FiscalPrinterException {
        return pa.getShiftNumber();
    }

    private void incKPK() throws FiscalPrinterException {
        fiscalData.incKPK();
        ShiftCounters counters = pa.getCountOfSalesAndReturns();
        fiscalData.setCountReturn(counters.getCountReturn());
        fiscalData.setCountSale(counters.getCountSale());
        fiscalData.setShiftNum(counters.getShiftNum());
        fiscalData.updateState();
    }

    @Override
    public long getLastKpk() throws FiscalPrinterException {
        incrementLastKpkIfInterrupted();
        return fiscalData.getKPK();
    }

    private void incrementLastKpkIfInterrupted() throws FiscalPrinterException {
        ShiftCounters counters = pa.getCountOfSalesAndReturns();
        if (counters.getCountSale().equals(fiscalData.getCountSale() + 1) ||
                counters.getCountReturn().equals(fiscalData.getCountReturn() + 1) ||
                counters.getShiftNum().equals(fiscalData.getShiftNum() + 1)) {
            incKPK();
        }
    }

    @Override
    public void initFpCounters() {
        try {
            fiscalData = new FiscalPrinterData();
            fiscalData.loadState();
        } catch (Exception e) {
            log.error("", e);
        }
    }
}
