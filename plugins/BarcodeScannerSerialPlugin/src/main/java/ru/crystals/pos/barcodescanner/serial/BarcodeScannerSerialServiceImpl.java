package ru.crystals.pos.barcodescanner.serial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.HardwareModule;
import ru.crystals.pos.barcodescanner.BarcodeScannerBeeper;
import ru.crystals.pos.barcodescanner.BarcodeScannerEvent;
import ru.crystals.pos.barcodescanner.barcodeconverter.BarcodeConverter;
import ru.crystals.pos.barcodescanner.exception.BarcodeScannerException;
import ru.crystals.pos.utils.CommonLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BarcodeScannerSerialServiceImpl implements HardwareModule, BarcodeScannerBeeper {
    private static final int MAX_BARCODE_LENGTH = 1500;

    private BarcodeScannerEvent barcodeScannerListener = null;
    private List<BarcodeScannerConfig> devices = new ArrayList<>();
    private String logLevel = "error";
    private List<BarcodeScannerListener> scanners = new ArrayList<>();

    private static final Logger commonLogger = CommonLogger.getCommonLogger();

    public static final Logger LOG = LoggerFactory.getLogger(BarcodeScannerSerialServiceImpl.class);


    @Override
    public void start() {
        for (BarcodeScannerConfig bsConfig : devices) {
            BarcodeScannerListener scanner = new BarcodeScannerListener(bsConfig);
            new Timer(true).schedule(scanner, 0, 2000);
            scanners.add(scanner);
        }
    }

    @Override
    public void stop() {
        scanners.clear();
    }

    @Override
    public void goodScanBeep() {
        for (BarcodeScannerListener scanner : scanners) {
            scanner.goodScanBeep();
        }
    }

    @Override
    public void errorBeep() {
        for (BarcodeScannerListener scanner : scanners) {
            scanner.errorBeep();
        }
    }

    private class BarcodeScannerListener extends TimerTask {

        private BarcodeScannerConfig config;
        private StringBuilder barcode = new StringBuilder();
        private boolean sent = false;
        private BarcodeScannerConnector connector = new BarcodeScannerConnector();

        private BarcodeConverter converter = new BarcodeConverter();

        private long timeOut;

        public BarcodeScannerListener(BarcodeScannerConfig config) {
            this.config = config;
            timeOut = config.getTimeOut();
            initializeBarcodeConverter();
        }

        public void errorBeep() {
            try {
                connector.write('\u0001');
                Thread.sleep(100);
                connector.write('\u0001');
                Thread.sleep(100);
                connector.write('\u0001');
                Thread.sleep(100);
                connector.write('\u0001');
            } catch (Exception e) {
                LOG.error("", e);
            }
        }

        public void goodScanBeep() {
            try {
                connector.write('\u0001');
            } catch (Exception e) {
                LOG.error("", e);
            }
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
                connector.open(config);
                long startTime = 0;
                long timeOutMillis;

                while (!Thread.currentThread().isInterrupted()) {
                    if (connector.available() > 0) {
                        sent = false;
                        log("Start barcode read");
                        startTime = System.currentTimeMillis();
                        for (byte b : connector.readAll()) {
                            //0x1D is a <gs> symbol for GS1
                            if (b == 0x1D || b >= 0x20) {
                                barcode.append((char) b);
                            }
                        }
                        Thread.sleep(2);
                    } else {
                        if (!sent) {
                            timeOutMillis = System.currentTimeMillis() - startTime;
                            if (timeOutMillis > this.timeOut) {
                                log("Finish barcode read - " + timeOutMillis);
                                success();
                            }
                            Thread.sleep(2);
                        } else {
                            Thread.sleep(5);
                        }
                    }

                }
            } catch (Exception e) {
                LOG.error("", e);
            } finally {
                connector.close();
            }
        }
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getLogLevel() {
        return logLevel;
    }

    private void log(String msg) {
        LOG.info(msg);
    }

    public void setBarcodeScannerListener(BarcodeScannerEvent barcodeScannerListener) {
        this.barcodeScannerListener = barcodeScannerListener;
    }

    public BarcodeScannerEvent getBarcodeScannerListener() {
        return barcodeScannerListener;
    }

    public void setDevices(List<BarcodeScannerConfig> devices) {
        this.devices = devices;
    }

    public List<BarcodeScannerConfig> getDevices() {
        return devices;
    }

}
