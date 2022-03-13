package ru.crystals.pos.fiscalprinter.az.airconn.audit;

import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

public interface AuditConnector {
    void softwareAudit(String author) throws FiscalPrinterException;

    void saveSoftwareChecksum() throws FiscalPrinterException;

    void verifySoftwareChecksum() throws FiscalPrinterException;
}
