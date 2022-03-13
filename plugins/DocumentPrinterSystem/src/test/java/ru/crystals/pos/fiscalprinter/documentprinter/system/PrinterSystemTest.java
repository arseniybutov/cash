package ru.crystals.pos.fiscalprinter.documentprinter.system;

import org.junit.Test;
import ru.crystals.pos.check.DocumentNumber;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCode;

import java.util.Date;
import org.junit.Ignore;

@Ignore
public class PrinterSystemTest {

    @Test
    public void tesPrint() throws Exception {


        DocumentPrinterSystem printer = new DocumentPrinterSystem();
        final DocumentPrinterSystemConfig config = new DocumentPrinterSystemConfig();
        config.setPort("PDF");
        printer.setConfig(config);

        printer.open();

        printer.openDocument();
        printer.setFont(Font.SMALL);
        printer.printLine("TEST STRING");
        printer.setFont(Font.NORMAL);
        printer.printLine("TEST STRING");
        printer.setFont(Font.UNDERLINE);
        printer.printLine("TEST STRING");


        BarCode barCode = new BarCode(new DocumentNumber(120L, 2L, 10L, 15L, new Date()));
        printer.printBarcode(barCode);

        printer.print("TEST STRING\nsegnerog\nseiugnieru");
        printer.closeDocument();

        printer.close();
    }
}
