package ru.crystals.pos.fiscalprinter.pirit.vikiprint.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.fiscalprinter.DocumentSection;
import ru.crystals.pos.fiscalprinter.Report;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FiscalDocument;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.FiscalPrinterData;

import java.util.List;

public class VikiPrintKNonFiscalized extends AbstractVikiPrint {

    private FiscalPrinterData fiscalData;

    protected final Logger log = LoggerFactory.getLogger(VikiPrintKNonFiscalized.class);

    @Override
    public long getShiftNumber() throws FiscalPrinterException {
        return fiscalData.getShiftNum();
    }

    @Override
    public long openShift(Cashier cashier) throws FiscalPrinterException {
        return fiscalData.getShiftNum();
    }

    @Override
    public void printDocumentAfter(List<DocumentSection> sectionList, FiscalDocument document) throws FiscalPrinterException {
        if ((document instanceof Report) && ((Report) document).isZReport()) {
            fiscalData.incShiftNum();
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
