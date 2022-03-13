package ru.crystals.pos.fiscalprinter.documentprinter.axiohm;

import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

@PrototypedComponent
public class DocumentPrinterOldAxiohm extends DocumentPrinterAxiohm {

    @Override
    public void skipAndCut() throws FiscalPrinterException {
        feed();
        cut();
    }

}
