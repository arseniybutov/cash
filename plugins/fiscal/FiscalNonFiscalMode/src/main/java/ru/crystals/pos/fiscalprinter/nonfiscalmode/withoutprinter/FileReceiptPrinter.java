package ru.crystals.pos.fiscalprinter.nonfiscalmode.withoutprinter;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.configurator.core.Constants;
import ru.crystals.pos.fiscalprinter.FiscalPrinter;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.ReceiptPrinter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileReceiptPrinter implements ReceiptPrinter {

    private static final Logger LOG = LoggerFactory.getLogger(FiscalPrinter.class);

    private static final Path FILE = Paths.get("modules", Constants.FISCAL_PRINTER, "check.printer.txt");
    private final int maxCharRow;

    private final String cutter;

    public FileReceiptPrinter(int maxCharRow) {
        cutter = StringUtils.repeat("~", maxCharRow);
        this.maxCharRow = maxCharRow;
        if (!Files.exists(FILE)) {
            try {
                Files.createFile(FILE);
            } catch (IOException e) {
                LOG.error("Unable to create {} file: {}", FILE, e.getMessage());
            }
        }
    }

    @Override
    public void printLine(String text) throws FiscalPrinterException {
        try {
            Files.write(FILE, (text + "\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new FiscalPrinterException(e.getMessage());
        }
        LOG.info(text);
    }

    @Override
    public void skipAndCut() throws FiscalPrinterException {
        printLine(cutter);
    }

    @Override
    public int getMaxCharRow(Font font) {
        return maxCharRow;
    }

}
