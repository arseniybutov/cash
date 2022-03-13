package ru.crystals.scales.ncr7872;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.bundles.BundleManager;
import ru.crystals.pos.CashException;
import ru.crystals.pos.scale.AbstractScalePluginImpl;
import ru.crystals.pos.scale.exception.ScaleException;
import ru.crystals.pos.utils.SerialPortAdapter;
import ru.crystals.scanner.ScannerViaScales;
import ru.crystals.utils.time.Timer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class ScalesNCR7872Impl extends AbstractScalePluginImpl {
    private static final Logger log = LoggerFactory.getLogger(ScalesNCR7872Impl.class);

    /**
     * Таймаут чтения данных (вес и ШК) из весов
     */
    private static final Duration READ_DATA_TIMEOUT = Duration.ofMillis(100);

    /**
     * Таймаут получения веса из весов
     */
    private static final Duration RECEIVE_WEIGHT_TIMEOUT = Duration.ofSeconds(3);

    private static final String replySeparator = String.valueOf((char) 0x03);
    private static final byte[] GET_WEIGHT_COMMAND = new byte[]{0x31, 0x34, 0x03, 0x06};
    private static final byte[] ON_ERROR_STATE = new byte[]{0x33, 0x33, 0x46, 0x03, 0x45};
    private static final byte[] ENABLE = new byte[]{0x33, 0x32, 0x33, 0x03, 0x31};
    private static final int START_WEIGHT_INDEX = 3;
    private static final int START_BARCODE_INDEX = 2;
    private static final int BCC_SHIFT = 1;
    private static final String BARCODE_2D_PREFIX = "]";
    private ExecutorService executor = Executors.newCachedThreadPool();
    private static final int ADDRESS_BYTE_NUMBER = 0;
    private static final int SCANNER_ADDRESS = 0x30;
    private static final int SCALES_ADDRESS = 0x31;
    private List<ScannerViaScales> scanners;
    private static final int MIN_WEIGHT_LENGTH = 8;
    private static final int ERROR_BYTE_NUMBER = 2;

    /**
     * Статусы, которые означают, что вес получен без ошибок
     */
    private static final Set<NcrStatus> SUCCESS_STATUSES = EnumSet.of(NcrStatus.NO_ERROR, NcrStatus.STABLE_ZERO_WEIGHT);

    /**
     * Нестиабильные временные статусы, при которых мы не выбрасываем исключение, а просто возвращаем вес 0 (это позволяет избежать ситуации,
     * когда весовой модуль считает, что весы отвалились)
     */
    private static final Set<NcrStatus> UNSTABLE_STATUSES = EnumSet.of(NcrStatus.SCALES_UNSTABLE, NcrStatus.NEGATIVE_WEIGHT, NcrStatus.SCALES_OVERLOAD);

    private SerialPortAdapter serialPortAdapter = new SerialPortAdapter();
    /**
     * weightReceived = true если вес от весов получен, правильно распарсился и вес еще не передан в кассу в ответ на текущий запрос
     */
    protected volatile boolean weightReceived = false;
    protected int weight = 0;
    protected NcrStatus status = NcrStatus.NO_ERROR;

    /**
     * Провайдер таймера получения веса для тестов
     */
    Supplier<Timer> receiveWeightTimerSupplier;

    public void setPort(String port) {
        serialPortAdapter.setPort(port);
    }

    public void setBaudRate(int baudRate) {
        serialPortAdapter.setBaudRate(baudRate);
    }

    public void setDataBits(int dataBits) {
        serialPortAdapter.setDataBits(dataBits);
    }

    public void setStopBits(int stopBits) {
        serialPortAdapter.setStopBits(stopBits);
    }

    public void setParity(int parity) {
        serialPortAdapter.setParity(parity);
    }

    public void initScanners() {
        scanners = new ArrayList<>();
        ScannerViaScales scanner = BundleManager.get(ScannerViaScales.class);
        if (scanner != null) {
            scanners.add(scanner);
            log.debug("ScannerViaScales has been added to scanners");
        } else {
            scanners = null;
        }
    }

    private class BarcodeScannerInvoker implements Runnable {
        private String barCodeString;

        public BarcodeScannerInvoker(String barCodeString) {
            this.barCodeString = barCodeString;
        }

        @Override
        public void run() {
            if (scanners != null) {
                for (ScannerViaScales scanner : scanners) {
                    scanner.fireBarcodeScannerEvent(barCodeString);
                }
            }
        }
    }

    protected class BarcodeScannerListener implements Runnable {
        private StringBuilder readData = new StringBuilder();
        private boolean sent = true;

        private void success() throws ScaleException {
            String data = readData.toString();
            while (!data.isEmpty() && data.indexOf(replySeparator) > 0 && data.indexOf(replySeparator) + BCC_SHIFT + 1 <= data.length()) {
                String answer = data.substring(0, data.indexOf(replySeparator) + BCC_SHIFT + 1);
                data = data.substring(answer.length());
                if (checkBCC(answer)) {
                    if (answer.charAt(ADDRESS_BYTE_NUMBER) == SCANNER_ADDRESS) {
                        parseBarcode(answer);
                    } else if (answer.charAt(ADDRESS_BYTE_NUMBER) == SCALES_ADDRESS) {
                        parseWeight(answer);
                    }
                } else {
                    throw new ScaleException(String.format(ResBundleScalesNCR7872.getString("CHECKSUM_ERROR"), answer));
                }
            }
            sent = true;
            if (readData.length() > 0) {
                readData.delete(0, readData.length());
            }
        }

        private void parseWeight(String data) {
            if (data.length() < MIN_WEIGHT_LENGTH) {
                final char code = data.charAt(ERROR_BYTE_NUMBER);
                status = NcrStatus.getByCode(code);
                if (status == NcrStatus.UNKNOWN) {
                    log.warn("Unknown error received ({})", code);
                }
                weight = 0;
            } else {
                int lastIdx = calcLastIndex(data);
                if (START_WEIGHT_INDEX <= lastIdx) {
                    try {
                        weight = Integer.parseInt(data.substring(START_WEIGHT_INDEX, lastIdx));
                    } catch (Exception e) {
                        weight = 0;
                    }
                    status = NcrStatus.NO_ERROR;
                }
            }
            weightReceived = true;
        }

        private void parseBarcode(String data) {
            int lastIdx = calcLastIndex(data);
            if (START_BARCODE_INDEX <= lastIdx) {
                String barcodeString = convert2DBarcode(data.substring(START_BARCODE_INDEX, lastIdx));
                if (scanners == null) {
                    initScanners();
                }
                executor.execute(new BarcodeScannerInvoker(barcodeString));
            }
        }

        @Override
        public void run() {
            try {
                Timer readDataTimer = Timer.of(READ_DATA_TIMEOUT);
                while (!Thread.currentThread().isInterrupted()) {
                    if (serialPortAdapter.getInputStreamBufferSize() > 0) {
                        sent = false;
                        readDataTimer.restart();
                        for (int b : serialPortAdapter.readAll()) {
                            readData.append((char) b);
                        }
                    } else {
                        if (!sent && readDataTimer.isExpired()) {
                            success();
                        }
                    }
                }
            } catch (Exception e) {
                log.error(ResBundleScalesNCR7872.getString("SCALES_FATAL_ERROR"), e);
            }
        }
    }

    /**
     * Конвертирует 2D ШК из Hex в ASCII, отрезая префиксы
     * <p>
     * Известные префиксы 2D ШК (из документации User Guide NCR RealScanTM 79 (7879))
     * GS1 DataBar-14 5Dh 65h 30h ]e0
     * GS1 DataBar-Expanded 5Dh 65h 30h ]e0
     * PDF417 5Dh 4Ch 32h ]L2
     * Datamatrix 5Dh 64h 30h ]d0
     * Aztec 5Dh 7Ah 30h ]z0
     * MaxiCode 5Dh 55h 30h ]U0
     */
    String convert2DBarcode(String rawBarcode) {
        if (!rawBarcode.startsWith(BARCODE_2D_PREFIX)) {
            return rawBarcode;
        }
        String encodedBarcode = StringUtils.substring(rawBarcode, 3);
        if (StringUtils.isEmpty(encodedBarcode)) {
            log.error("Unexpected length of 2D barcode: '{}'", rawBarcode);
            return rawBarcode;
        }
        try {
            return new String(Hex.decodeHex(encodedBarcode.toCharArray()), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Unable to decode hex for 2D barcode: '{}'", rawBarcode);
            return rawBarcode;
        }
    }

    @Override
    public void start() throws CashException {
        log.debug("start()");
        try {
            serialPortAdapter.openPort();
            serialPortAdapter.setRTS(true);
        } catch (Exception e) {
            throw new CashException(e);
        }
        executor.execute(new BarcodeScannerListener());
    }

    @Override
    public void stop() {
        log.debug("stop()");
        executor.shutdown();
        serialPortAdapter.close();
    }

    @Override
    public int getWeight() throws ScaleException {
        try {
            serialPortAdapter.write(GET_WEIGHT_COMMAND);
            weightReceived = false;
            Timer receiveWeightTimer = getReceiveWeightTimer();
            while (!weightReceived) {
                if (receiveWeightTimer.isExpired()) {
                    throw new ScaleException(NcrStatus.SCALES_NOT_RESPOND.getDescription());
                }
            }
            weightReceived = false;
            if (SUCCESS_STATUSES.contains(status)) {
                return weight;
            }
            if (UNSTABLE_STATUSES.contains(status)) {
                return 0;
            }
            throw new ScaleException(status.getDescription());
        } catch (IOException e) {
            throw new ScaleException(e.getMessage());
        }
    }

    @Override
    public Boolean moduleCheckState() {
        return serialPortAdapter.isConnected();
    }

    private boolean checkBCC(String answer) {
        int result = 0;
        for (char c : answer.substring(0, answer.length() - 1).toCharArray()) {
            result = result ^ c;
        }
        return result == answer.charAt(answer.length() - 1);
    }

    private static int calcLastIndex(String data) {
        if (StringUtils.isEmpty(data)) {
            return -1;
        }
        int idx = data.lastIndexOf(replySeparator);
        if (idx > 0) {
            int idx1 = data.lastIndexOf(replySeparator, idx - 1);
            while (idx1 > 0) {
                idx1 = data.lastIndexOf(replySeparator, (idx = idx1) - 1);
            }
        }
        return idx;
    }

    private Timer getReceiveWeightTimer() {
        if (receiveWeightTimerSupplier == null) {
            return Timer.of(RECEIVE_WEIGHT_TIMEOUT);
        }
        return receiveWeightTimerSupplier.get();
    }

    @Override
    public void toWarningState() {
        try {
            serialPortAdapter.write(ON_ERROR_STATE);
            status = NcrStatus.UNKNOWN;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void toWorkingState() {
        try {
            serialPortAdapter.write(ENABLE);
            status = NcrStatus.NO_ERROR;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
