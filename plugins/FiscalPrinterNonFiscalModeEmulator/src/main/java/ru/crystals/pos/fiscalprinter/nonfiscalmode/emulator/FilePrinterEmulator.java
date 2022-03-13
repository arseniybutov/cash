package ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator;

import org.apache.commons.io.input.ReversedLinesFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.configurator.core.Constants;
import ru.crystals.pos.fiscalprinter.CheckLine;
import ru.crystals.pos.fiscalprinter.FiscalPrinter;
import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class FilePrinterEmulator {

    public static final Logger LOGGER = LoggerFactory.getLogger(FiscalPrinter.class);
    private String printerFile;

    public FilePrinterEmulator(long index) {
        printerFile = Constants.PATH_MODULES + Constants.FISCAL_PRINTER + File.separator + "check_" + index + ".printer.txt";
    }

    public String getPrinterFile() {
        return printerFile;
    }

    public void writeToFile(String text) throws FiscalPrinterException {
        writeToFile(new FontLine(text));
    }

    public void writeToFile(CheckLine text) throws FiscalPrinterException {
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(new File(printerFile), true))) {
            bw.write(text.getContent());
            bw.newLine();
            Thread.sleep(Timeouts.PRINT_STRING_INTERVAL);
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage());
        }
    }

    public void writeToFile(List<CheckLine> lines) throws FiscalPrinterException {
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(new File(printerFile), true))) {
            for (CheckLine text : lines) {
                bw.write(text.getContent());
                bw.newLine();
                Thread.sleep(Timeouts.PRINT_STRING_INTERVAL);
            }
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage());
        }
    }

    public void recreateFile() {
        File file = new File(printerFile);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     *
     * Возвращает напечатанный документ с порядковым номером с конца равным offset
     *
     * @param cutter
     * @param offset
     * @return
     * @throws FiscalPrinterException
     */
    public String getPrintedDocumentWithOffset(FontLine cutter, int offset) throws FiscalPrinterException {

        LOGGER.info("FilePrinter.getLastPrinted() invoked");
        File file = new File(printerFile);
        String res = "";
        int count = 0;

        try {
            ReversedLinesFileReader br = new ReversedLinesFileReader(file);
            String line;
            boolean found = false;

            while ((line = br.readLine()) != null) {

                if (line.equals(cutter.getContent())) {

                    if (found) {
                        if (count == offset) {
                            break;
                        }
                        count++;
                    } else {
                        found = true;
                    }
                } else {
                    if (found && count == offset) {
                        if (!"".equals(res)) {
                            res = "\n" + res;
                        }
                        res = line + res;
                    }
                }
            }

            br.close();

        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage());
        }

        return res;
    }
}
