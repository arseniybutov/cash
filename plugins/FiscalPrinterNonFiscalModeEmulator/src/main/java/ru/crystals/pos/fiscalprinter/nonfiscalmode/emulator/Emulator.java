package ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.fiscalprinter.FiscalPrinter;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.datastruct.state.StatusFP;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.ReceiptPrinter;

public class Emulator implements ReceiptPrinter {

    private FontLine CUTTER = new FontLine(StringUtils.repeat("~", 40), Font.NORMAL);

    private final Logger LOG = LoggerFactory.getLogger(FiscalPrinter.class);

    private final int SKIPPED_LINES_QUANTITY = 1;

    private int maxCharRow = 46;

    public static boolean drawerOpened = false;

    private FilePrinterEmulator fpe;

    public Emulator(FilePrinterEmulator fpe) {
        this.fpe = fpe;
    }

    @Override
    public void printLine(String text) throws FiscalPrinterException {
        fpe.writeToFile(new FontLine(text, Font.NORMAL));
        LOG.info(text);
    }

    public void setMaxCharRow(int maxCharRow) {
        this.maxCharRow = maxCharRow;
        CUTTER = new FontLine(StringUtils.repeat("~", maxCharRow), Font.NORMAL);
    }

    @Override
    public int getMaxCharRow(Font font) {
        return maxCharRow;
    }

    @Override
    public boolean isCashDrawerOpen() {
        return drawerOpened;
    }

    @Override
    public void skipAndCut() throws FiscalPrinterException {
        for (int i = 0; i < SKIPPED_LINES_QUANTITY; i++) {
            LOG.info("");
            fpe.writeToFile(CUTTER);
        }
    }

    @Override
    public StatusFP getStatus() {
        return new StatusFP();
    }
}
