package ru.crystals.pos.fiscalprinter.az.airconn.audit;

import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

public class DebugAuditHandler {

    public static void main(String[] args) throws Exception {
        final AuditHandler auditHandler = new AuditHandler(new AuditConnector() {
            @Override
            public void softwareAudit(String author) throws FiscalPrinterException {

            }

            @Override
            public void saveSoftwareChecksum() throws FiscalPrinterException {

            }

            @Override
            public void verifySoftwareChecksum() throws FiscalPrinterException {

            }
        });
        auditHandler.startWork(6304, "/audit", "324012");
    }

}
