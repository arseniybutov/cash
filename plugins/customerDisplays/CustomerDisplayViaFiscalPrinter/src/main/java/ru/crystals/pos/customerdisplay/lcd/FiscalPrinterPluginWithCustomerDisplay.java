package ru.crystals.pos.customerdisplay.lcd;

import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

public interface FiscalPrinterPluginWithCustomerDisplay {

    void displayLines(String[] lines) throws FiscalPrinterException;
}
