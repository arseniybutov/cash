package ru.crystals.pos.fiscalprinter.atol3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.fiscalprinter.Report;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FiscalDocument;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

@PrototypedComponent
public class AtolServiceFFD105Impl extends AtolServiceImpl {
    private static final Logger LOG = LoggerFactory.getLogger(AtolServiceFFD105Impl.class);
    private static final Long PRINT_Z_BREAK_ERROR_CODE = 26L;

    @Override
    public void printXReport(Report report) throws FiscalPrinterException {
        fiscalDevice.addCashierName(report.getCashier().getCashierStringForOFDTag1021().trim());
        try {
            fiscalDevice.addCashierInn(report.getCashier().getInn());
        } catch (FiscalPrinterException ex) {
            LOG.error(ex.getMessage(), ex);
            if (PRINT_Z_BREAK_ERROR_CODE.equals(ex.getErrorCode())) {
                fiscalDevice.printZReport();
            }
        }
        fiscalDevice.printXReport();
    }

    @Override
    public void printZReport(Report report) throws FiscalPrinterException {
        fiscalDevice.addCashierName(report.getCashier().getCashierStringForOFDTag1021().trim());
        try {
            fiscalDevice.addCashierInn(report.getCashier().getInn());
        } catch (FiscalPrinterException ex) {
            LOG.error(ex.getMessage(), ex);
            if (!PRINT_Z_BREAK_ERROR_CODE.equals(ex.getErrorCode())) {
                throw ex;
            }
        }
        fiscalDevice.printZReport();
    }

    protected void openDocument(FiscalDocument document) throws FiscalPrinterException {
        if (isDocOpen()) {
            annulCheck();
        }

        if (document instanceof Check) {
            fiscalOperationComplete = false;
            Check check = (Check) document;
            fiscalDevice.openDocument(check.getType());
            fiscalDevice.addClientRequisite(check.getClientRequisites());
            fiscalDevice.addCashierName(check.getCashier().getCashierStringForOFDTag1021().trim());
            fiscalDevice.addCashierInn(check.getCashier().getInn());
        }
    }

}
