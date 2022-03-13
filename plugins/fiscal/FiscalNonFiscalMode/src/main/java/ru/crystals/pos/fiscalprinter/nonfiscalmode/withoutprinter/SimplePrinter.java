package ru.crystals.pos.fiscalprinter.nonfiscalmode.withoutprinter;

import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.configurator.core.Configurable;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.ReceiptPrinter;

@PrototypedComponent
public class SimplePrinter implements ReceiptPrinter, Configurable<SimplePrinterConfig> {

    private SimplePrinterConfig config;

    private ReceiptPrinter delegated;

    @Override
    public Class<SimplePrinterConfig> getConfigClass() {
        return SimplePrinterConfig.class;
    }

    @Override
    public void setConfig(SimplePrinterConfig config) {
        this.config = config;
    }

    @Override
    public void open() {
        if (config.isNeedToPrint()) {
            delegated = new FileReceiptPrinter(config.getMaxCharRow());
        } else {
            delegated = new EmptyPrinter();
        }
    }

    @Override
    public void printLine(String text) throws FiscalPrinterException {
        delegated.printLine(text);
    }

    @Override
    public void skipAndCut() throws FiscalPrinterException {
        delegated.skipAndCut();
    }

    @Override
    public int getMaxCharRow(Font font) {
        return config.getMaxCharRow();
    }

}
