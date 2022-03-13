package ru.crystals.pos.fiscalprinter.documentprinter.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.configurator.core.Configurable;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCode;
import ru.crystals.pos.fiscalprinter.datastruct.state.StatusFP;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.ReceiptPrinter;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PageRanges;
import javax.print.attribute.standard.PrinterResolution;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.util.Locale;

@PrototypedComponent
public class DocumentPrinterSystem implements ReceiptPrinter, Printable, Configurable<DocumentPrinterSystemConfig> {

    protected static final Logger LOG = LoggerFactory.getLogger(DocumentPrinterSystem.class);

    private PrintService printService = null;
    private PrintRequestAttributeSet printerSet;
    private int printerWidthMM = 200;
    private int printerHeightMM = 280;
    private final DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;

    private DocumentData documentData;

    private DocumentPrinterSystemConfig config;

    @Override
    public Class<DocumentPrinterSystemConfig> getConfigClass() {
        return DocumentPrinterSystemConfig.class;
    }

    @Override
    public void setConfig(DocumentPrinterSystemConfig config) {
        this.config = config;
    }

    @Override
    public void open() throws FiscalPrinterException {
        if (config.getPort() == null) {
            throw new FiscalPrinterException("Printer port isn't defined");
        }

        LOG.info("Search for system printer");
        for (PrintService ps : PrinterJob.lookupPrintServices()) {
            LOG.info("... {}", ps.getName());
            if (config.getPort().equalsIgnoreCase(ps.getName())) {
                printService = ps;
                LOG.info("Found document printer: {}", config.getPort());
                break;
            }
        }

        if (printService == null) {
            throw new FiscalPrinterException("Printer port not found");
        }
    }

    private PrintRequestAttributeSet getPrinterSet() {
        if (printerSet == null) {
            //создаем обьект настроек принтера
            printerSet = new HashPrintRequestAttributeSet();
            printerSet.add(OrientationRequested.PORTRAIT);
            //количество копий
            printerSet.add(new Copies(1));
            //количество страниц
            printerSet.add(new PageRanges(1));
            //область печати
            printerSet.add(new MediaPrintableArea(0.1f, 0.1f, printerWidthMM, printerHeightMM, MediaPrintableArea.MM));
            printerSet.add(new PrinterResolution(203, 203, PrinterResolution.DPI));
            printerSet.add(new JobName("Document", Locale.getDefault()));
        }
        return printerSet;
    }

    private void checkThatPrinterIsReady() throws FiscalPrinterException {
        if (printService == null) {
            throw new FiscalPrinterException("Printer isn't ready");
        }
    }

    @Override
    public void openDocument() {
        documentData = new DocumentData();
    }

    @Override
    public void closeDocument() throws FiscalPrinterException {
        LOG.info("Print document");
        try {
            DocPrintJob job = printService.createPrintJob();
            job.addPrintJobListener(new PrintJobAdapter() {
                @Override
                public void printJobFailed(PrintJobEvent pje) {
                    LOG.error("", pje);
                }

                @Override
                public void printJobCompleted(PrintJobEvent pje) {
                    LOG.debug("printJobCompleted");
                }

                @Override
                public void printDataTransferCompleted(PrintJobEvent pje) {
                    LOG.debug("printDataTransferCompleted");
                }

                @Override
                public void printJobNoMoreEvents(PrintJobEvent pje) {
                    LOG.debug("printJobNoMoreEvents");
                }
            });
            Doc doc = new SimpleDoc(this, flavor, null);
            job.print(doc, getPrinterSet());
        } catch (Exception ex) {
            throw new FiscalPrinterException("Print document fail", ex);
        }

    }

    @Override
    public void printLine(String text) throws FiscalPrinterException {
        checkThatPrinterIsReady();
        documentData.appendRow(text);
    }

    public void print(String text) throws IOException, FiscalPrinterException {
        checkThatPrinterIsReady();
        documentData.appendText(text);
    }

    @Override
    public int getMaxCharRow(Font font) {
        switch (font) {
            case DOUBLEWIDTH:
                return 22;
            case DOUBLEHEIGHT:
                return 34;
            case SMALL:
                return 42;
            case UNDERLINE:
            case NORMAL:
            default:
                return 34;
        }
    }

    @Override
    public void printBarcode(BarCode barcode) throws FiscalPrinterException {
        checkThatPrinterIsReady();
        documentData.appendBarcode(barcode);
    }

    @Override
    public void setFont(Font font) {
        documentData.changeFont(font);
    }

    @Override
    public void skipAndCut() {
        //unused method
    }

    @Override
    public StatusFP getStatus() {
        StatusFP status = new StatusFP();
        status.setStatus(StatusFP.Status.NORMAL);
        return status;
    }

    @Override
    public int print(Graphics g, PageFormat pageFormat, int page) throws PrinterException {
        if (page > 0) {
            /* We have only one page, and 'page' is zero-based */
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        documentData.print(g2d, pageFormat, 0);
        return PAGE_EXISTS;
    }
}
