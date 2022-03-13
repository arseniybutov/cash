package ru.crystals.pos.barcodescanner.ps2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.HardwareModule;
import ru.crystals.pos.barcodescanner.BarcodeScannerEvent;
import ru.crystals.pos.barcodescanner.ResBundleScanner;
import ru.crystals.pos.barcodescanner.barcodeconverter.BarcodeConverter;
import ru.crystals.pos.barcodescanner.exception.BarcodeScannerException;
import ru.crystals.pos.keyboard.InterModuleBridge;
import ru.crystals.pos.utils.CommonLogger;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.IntUnaryOperator;

public class BarcodeScannerPs2ServiceImpl implements HardwareModule {

    private static final int MIN_BARCODE_LENGTH = 4;
    /**
     * Преобразуем виртуальный код в код ASCII.
     */
    private static final IntUnaryOperator transformVKtoASCII = keyCode -> KeyEvent.VK_QUOTE == keyCode ? (int) '\'' : keyCode;


    private BarcodeScannerEvent barcodeScannerListener = null;

    private Long keyboardTimeOut = 20L;

    private Long otherTimeOut = 17L;

    private List<Integer> barcodePrefix;

    private List<Integer> barcodeSuffix;

    private boolean ean13LeadingZero;

    private boolean ean13ControlNumber;

    private boolean ean8ControlNumber;

    private boolean upcaLeadingZero;

    private boolean upcaControlNumber;

    private boolean upceLeadingZero;

    private boolean upceControlNumber;

    private static final Logger commonLogger = CommonLogger.getCommonLogger();

    public static final Logger LOG = LoggerFactory.getLogger(BarcodeScannerPs2ServiceImpl.class);

    private InterModuleBridge bridge;

    private ExecutorService executor = Executors.newFixedThreadPool(1);

    private static final long SLEEP_TIME = 5;

    private static final long TIME_OUT = 3000;

    private int maxBarcodeLength = 1500;

    private boolean caseSensitive;

    @Override
    public void start() throws BarcodeScannerException {
        try {
            bridge = InterModuleBridge.getInstance();
            bridge.setBarcodeSuffix(barcodeSuffix);
            bridge.setMaxBarcodeLength(maxBarcodeLength);
            bridge.setBarcodePrefixFirstCode((barcodePrefix != null && !barcodePrefix.isEmpty()) ? barcodePrefix.get(0) : null);
            bridge.setCaseSensitive(caseSensitive);
            executor.execute(new BarcodeScannerListener());
        } catch (Exception e) {
            LOG.error("", e);
            throw new BarcodeScannerException(ResBundleScanner.getString("ERROR_START"));
        }
    }

    @Override
    public void stop() throws BarcodeScannerException {
        try {
            executor.shutdown();
        } catch (Exception e) {
            LOG.error("", e);
            throw new BarcodeScannerException(ResBundleScanner.getString("ERROR_STOP"));
        }
    }

    private class BarcodeScannerListener implements Runnable {

        private BarcodeConverter converter = new BarcodeConverter();

        private long startTime;

        public BarcodeScannerListener() {
            initializeBarcodeConverter();
        }

        private void initializeBarcodeConverter() {
            converter.setEan13LeadingZero(ean13LeadingZero);
            converter.setEan13ControlNumber(ean13ControlNumber);
            converter.setEan8ControlNumber(ean8ControlNumber);
            converter.setUpcaLeadingZero(upcaLeadingZero);
            converter.setUpcaControlNumber(upcaControlNumber);
            converter.setUpceLeadingZero(upceLeadingZero);
            converter.setUpceControlNumber(upceControlNumber);
        }

        @Override
        public void run() {
            try {
                Queue<Integer> scanCodes = bridge.getScannerScanCodes();
                int finalScanCode = barcodeSuffix.get(barcodeSuffix.size() - 1);

                while (!Thread.currentThread().isInterrupted()) {
                    if (scanCodes.peek() != null) {

                        if (commonLogger.isDebugEnabled()) {
                            startTime = System.nanoTime();
                        }

                        List<Integer> scanCodeList = createScanCodeListFromQueue(scanCodes, finalScanCode);
                        if (scanCodeList != null) {
                            process(scanCodeList);
                        }
                    } else {
                        Thread.sleep(SLEEP_TIME);
                    }
                }
            } catch (Exception e) {
                LOG.error("", e);
            }
        }

        private List<Integer> createScanCodeListFromQueue(Queue<Integer> scanCodes, int suffixCode) throws InterruptedException {
            // Очередь scanCodes должна содержать по меньшей мере 2 сканкода
            List<Integer> scanCodeList = null;

            int scanCodesSize = 0;
            boolean queueIsFull = false;

            long startTimeMillis = System.currentTimeMillis();

            while ((System.currentTimeMillis() - startTimeMillis) < TIME_OUT) {
                if ((scanCodes.size() > 1) && (getLastScanCode(scanCodes) == suffixCode)) {
                    scanCodesSize = scanCodes.size();
                    queueIsFull = true;
                    break;
                }
                Thread.sleep(SLEEP_TIME);
            }

            if (queueIsFull) {
                scanCodeList = new ArrayList<>();
                for (int i = 0; i < scanCodesSize; i++) {
                    int scanCode = scanCodes.poll();
                    scanCodeList.add(scanCode);
                    if ((i > 0) && (scanCode == suffixCode)) {
                        break;
                    }
                }
            }

            return scanCodeList;
        }

        private int getLastScanCode(Queue<Integer> scanCodes) {
            int lastScanCode = 0;
            int count = 0;
            for (int scanCode : scanCodes) {
                count++;
                if (count == scanCodes.size()) {
                    lastScanCode = scanCode;
                    break;
                }

            }
            return lastScanCode;
        }

        private void process(List<Integer> scanCodeList) throws BarcodeScannerException {
            String rawBarcode = getBarcode(scanCodeList);
            String barcode = converter.convert(rawBarcode);

            if (barcode != null && barcodeScannerListener != null && barcode.length() <= maxBarcodeLength && (isPrefixSuffixExists() || barcode.length() >
                    MIN_BARCODE_LENGTH)) {
                logBarcode(rawBarcode, barcode);
                barcodeScannerListener.eventBarcodeScanner(barcode);
            } else {
                commonLogger.error("Wrong barcode or wrong barcode length - !(5..{})", maxBarcodeLength);
            }

        }

        private boolean isPrefixSuffixExists() {
            return !barcodePrefix.isEmpty() && !barcodeSuffix.isEmpty();
        }

        private void logBarcode(String rawBarcode, String barcode) {
            if (rawBarcode != null && !rawBarcode.equals(barcode)) {
                commonLogger.info("Raw BarCode::" + rawBarcode);
            }
            commonLogger.info("BarCode::" + barcode);

            if (commonLogger.isDebugEnabled()) {
                long scanTime = System.nanoTime() - startTime;
                commonLogger.debug("Scan time = " + (scanTime / 1000000) + " ms (" + scanTime + " ns)");
            }
        }

        private String getBarcode(List<Integer> scanCodeList) {
            String barcode = null;

            List<Integer> readBarcodePrefix = getSubList(scanCodeList, 0, barcodePrefix.size());
            List<Integer> readBarcodeSuffix = getSubList(scanCodeList, scanCodeList.size() - barcodeSuffix.size(), scanCodeList.size());

            if (barcodePrefix.equals(readBarcodePrefix) && barcodeSuffix.equals(readBarcodeSuffix)) {
                StringBuilder buffer = new StringBuilder();

                for (int i = barcodePrefix.size(); i < scanCodeList.size() - barcodeSuffix.size(); i++) {
                    int scanCode = transformVKtoASCII.applyAsInt(scanCodeList.get(i));
                    scanCode = scanCode & 0x7F;
                    if (scanCode >= 0x20) {
                        buffer.append((char) scanCode);
                    }
                }

                barcode = buffer.toString();
            }
            return barcode;
        }

        private List<Integer> getSubList(List<Integer> integerList, int from, int to) {
            Integer[] integerArray = integerList.toArray(new Integer[0]);
            return Arrays.asList(Arrays.copyOfRange(integerArray, from, to));
        }

    }

    public void setBarcodeScannerListener(BarcodeScannerEvent barcodeScannerListener) {
        this.barcodeScannerListener = barcodeScannerListener;
    }

    public BarcodeScannerEvent getBarcodeScannerListener() {
        return barcodeScannerListener;
    }

    public void setKeyboardTimeOut(Long keyboardTimeOut) {
        this.keyboardTimeOut = keyboardTimeOut;
    }

    public Long getKeyboardTimeOut() {
        return keyboardTimeOut;
    }

    public void setOtherTimeOut(Long otherTimeOut) {
        this.otherTimeOut = otherTimeOut;
    }

    public Long getOtherTimeOut() {
        return otherTimeOut;
    }

    public List<Integer> getBarcodePrefix() {
        return barcodePrefix;
    }

    public void setBarcodePrefix(List<Integer> barcodePrefix) {
        this.barcodePrefix = barcodePrefix;
    }

    public List<Integer> getBarcodeSuffix() {
        return barcodeSuffix;
    }

    public void setBarcodeSuffix(List<Integer> barcodeSuffix) {
        this.barcodeSuffix = barcodeSuffix;
    }

    public boolean isEan13LeadingZero() {
        return ean13LeadingZero;
    }

    public void setEan13LeadingZero(boolean ean13LeadingZero) {
        this.ean13LeadingZero = ean13LeadingZero;
    }

    public boolean isEan13ControlNumber() {
        return ean13ControlNumber;
    }

    public void setEan13ControlNumber(boolean ean13ControlNumber) {
        this.ean13ControlNumber = ean13ControlNumber;
    }

    public boolean isEan8ControlNumber() {
        return ean8ControlNumber;
    }

    public void setEan8ControlNumber(boolean ean8ControlNumber) {
        this.ean8ControlNumber = ean8ControlNumber;
    }

    public boolean isUpcaLeadingZero() {
        return upcaLeadingZero;
    }

    public void setUpcaLeadingZero(boolean upcaLeadingZero) {
        this.upcaLeadingZero = upcaLeadingZero;
    }

    public boolean isUpcaControlNumber() {
        return upcaControlNumber;
    }

    public void setUpcaControlNumber(boolean upcaControlNumber) {
        this.upcaControlNumber = upcaControlNumber;
    }

    public boolean isUpceLeadingZero() {
        return upceLeadingZero;
    }

    public void setUpceLeadingZero(boolean upceLeadingZero) {
        this.upceLeadingZero = upceLeadingZero;
    }

    public boolean isUpceControlNumber() {
        return upceControlNumber;
    }

    public void setUpceControlNumber(boolean upceControlNumber) {
        this.upceControlNumber = upceControlNumber;
    }

    public int getBarcodeLength() {
        return maxBarcodeLength;
    }

    public void setBarcodeLength(int barcodeLength) {
        maxBarcodeLength = barcodeLength;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public void setMaxBarcodeLength(int maxBarcodeLength) {
        this.maxBarcodeLength = maxBarcodeLength;
    }
}
