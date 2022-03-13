package ru.crystals.pos.customerdisplay.csibarcodescanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.HardwareModule;
import ru.crystals.pos.barcodescanner.BarcodeScannerEvent;
import ru.crystals.pos.barcodescanner.barcodeconverter.BarcodeConverter;
import ru.crystals.pos.barcodescanner.exception.BarcodeScannerException;
import ru.crystals.pos.utils.CommonLogger;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class BarcodeScannerService implements Runnable, HardwareModule {

    private static final Logger commonLogger = CommonLogger.getCommonLogger();
    private static final Logger LOG = LoggerFactory.getLogger(BarcodeScannerService.class);

    private static final int MAX_BARCODE_LENGTH = 1500;

    private BarcodeScannerConfig config;
    private StringBuilder barcode = new StringBuilder();
    private boolean sent = false;
    private InputStream is = null;
    private BarcodeScannerConnector connector = new BarcodeScannerConnector();
    private BarcodeScannerEvent barcodeScannerListener = null;

    private BarcodeConverter converter = new BarcodeConverter();
    ExecutorService executor = Executors.newCachedThreadPool();
    private static final long TIME_OUT = 25;

    public BarcodeScannerService(BarcodeScannerConfig config, InputStream is) {
        this.connector = new BarcodeScannerConnector();
        this.config = config;
        this.is = is;
        initializeBarcodeConverter();
    }

    public void setBarcodeScannerListener(BarcodeScannerEvent barcodeScannerListener) {
        this.barcodeScannerListener = barcodeScannerListener;
    }

    @Override
    public void start() throws BarcodeScannerException {
        executor.execute(this);

    }

    @Override
    public void stop() throws BarcodeScannerException {
        executor.shutdown();
    }

    private void initializeBarcodeConverter() {
        converter.setEan13LeadingZero(config.isEan13LeadingZero());
        converter.setEan13ControlNumber(config.isEan13ControlNumber());
        converter.setEan8ControlNumber(config.isEan8ControlNumber());
        converter.setUpcaLeadingZero(config.isUpcaLeadingZero());
        converter.setUpcaControlNumber(config.isUpcaControlNumber());
        converter.setUpceLeadingZero(config.isUpceLeadingZero());
        converter.setUpceControlNumber(config.isUpceControlNumber());
    }

    private void success() throws BarcodeScannerException {
        if (barcode.length() <= MAX_BARCODE_LENGTH) {
            String rawBarcode = trimBarcodePrefixAndSuffix(this.barcode.toString());
            if (rawBarcode != null && rawBarcode.length() > 0) {
                String barcodeString = converter.convert(rawBarcode);
                logBarcode(rawBarcode, barcodeString);
                barcodeScannerListener.eventBarcodeScanner(barcodeString);
            }
        }
        sent = true;
        if (barcode.length() > 0) {
            barcode.delete(0, barcode.length());
        }
    }

    private void logBarcode(String rawBarcode, String barcode) {
        if (!rawBarcode.equals(barcode)) {
            commonLogger.info("Raw BarCode::" + rawBarcode);
        }
        commonLogger.info("BarCode::" + barcode);
    }

    private void log(String msg) {
        LOG.info(msg);
    }

    private String trimBarcodePrefixAndSuffix(String barcode) throws BarcodeScannerException {
        int beginIndex = config.getBarcodePrefix().length();
        int endIndex = barcode.length() - config.getBarcodeSuffix().length();

        String readBarcodePrefix = barcode.substring(0, beginIndex);
        String readBarcodeSuffix = barcode.substring(endIndex);

        if (!config.getBarcodePrefix().equals(readBarcodePrefix)) {
            throw new BarcodeScannerException("Wrong barcode prefix \"" + readBarcodePrefix + "\"");
        }
        if (!config.getBarcodeSuffix().equals(readBarcodeSuffix)) {
            throw new BarcodeScannerException("Wrong barcode suffix \"" + readBarcodeSuffix + "\"");
        }

        return barcode.substring(beginIndex, endIndex);
    }

    @Override
    public void run() {
        try {
            long startTime = 0;
            long timeOut;
            connector.open(is);
            while (!Thread.currentThread().isInterrupted()) {
                if (connector.available() > 0) {
                    sent = false;
                    log("Start barcode read");
                    startTime = System.currentTimeMillis();
                    for (byte b : connector.readAll()) {
                        if (b >= 0x20) {
                            barcode.append((char) b);
                        }
                    }
                    Thread.sleep(2);
                } else {
                    if (!sent) {
                        timeOut = System.currentTimeMillis() - startTime;
                        if (timeOut > TIME_OUT) {
                            log("Finish barcode read - " + timeOut);
                            success();
                        }
                        Thread.sleep(2);
                    } else {
                        Thread.sleep(5);
                    }
                }

            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            LOG.error("", ie);
        } catch (Exception e) {
            LOG.error("", e);
        } finally {
            connector.close();
        }
    }
}
