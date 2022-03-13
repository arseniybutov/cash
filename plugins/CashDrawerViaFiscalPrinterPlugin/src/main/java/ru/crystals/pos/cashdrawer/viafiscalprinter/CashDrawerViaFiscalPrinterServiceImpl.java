package ru.crystals.pos.cashdrawer.viafiscalprinter;

import ru.crystals.bundles.BundleManager;
import ru.crystals.pos.CashException;
import ru.crystals.pos.cashdrawer.CashDrawer;
import ru.crystals.pos.cashdrawer.exception.CashDrawerException;
import ru.crystals.pos.fiscalprinter.FiscalPrinter;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

public class CashDrawerViaFiscalPrinterServiceImpl implements CashDrawer {

    private FiscalPrinter fiscalPrinter;

    public CashDrawerViaFiscalPrinterServiceImpl() {
        BundleManager.addListener(FiscalPrinter.class, () -> fiscalPrinter = BundleManager.get(FiscalPrinter.class));
    }

    @Override
    public void start() {
        //
    }

    @Override
    public void stop() {
        //
    }

    @Override
    public boolean isOpenDrawer() throws CashDrawerException {
        if (fiscalPrinter == null) {
            throw new CashDrawerException("Dash drawer is not configured");
        }
        try {
            return fiscalPrinter.isMoneyDrawerOpen();
        } catch (FiscalPrinterException e) {
            throw new CashDrawerException(e.getMessage());
        }
    }

    @Override
    public boolean openDrawer(CashDrawerOpenMode openMode) throws CashException {
        if (fiscalPrinter == null) {
            throw new CashDrawerException("Dash drawer is not configured");
        }
        fiscalPrinter.openMoneyDrawer();
        return true;
    }
}
