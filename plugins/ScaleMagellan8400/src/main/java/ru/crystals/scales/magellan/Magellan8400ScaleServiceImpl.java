package ru.crystals.scales.magellan;

import gnu.io.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.bundles.BundleManager;
import ru.crystals.pos.CashException;
import ru.crystals.pos.barcodescanner.ExtendedScaleScannerFunctions;
import ru.crystals.pos.scale.AbstractScalePluginImpl;
import ru.crystals.pos.scale.Scale;
import ru.crystals.pos.scale.exception.ScaleException;
import ru.crystals.pos.scale.exception.ScaleNegativeException;
import ru.crystals.scanner.ScannerViaScales;
import ru.crystals.utils.time.Timer;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Magellan8400ScaleServiceImpl extends AbstractScalePluginImpl implements ExtendedScaleScannerFunctions {

    private static final Logger LOGGER = LoggerFactory.getLogger(Scale.class);

    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(3);
    private static final EnumSet<DeviceError> NOT_FATAL_ERRORS = EnumSet.of(DeviceError.OK, DeviceError.ZERO_WEIGHT, DeviceError.SCALES_STABILIZING);

    private final byte prefix = 0x53;
    private final byte terminator = 0x0D;
    private final byte[] getWeightCommand = new byte[]{prefix, (byte) 0x31, (byte) 0x34, terminator};
    private final byte[] beepCommand = new byte[]{prefix, (byte) 0x33, (byte) 0x33, (byte) 0x34, terminator};
    private final byte[] scaleSoftReset = new byte[]{prefix, (byte) 0x33, (byte) 0x32, (byte) 0x30, terminator};
    private final byte[] turnOnCommand = new byte[]{prefix, (byte) 0x33, (byte) 0x32, (byte) 0x33, terminator};
    private ExecutorService executor = Executors.newCachedThreadPool();
    private List<ScannerViaScales> scanners;
    private String port = "COM1";
    private int baudRate = 9600;
    private int dataBits = SerialPort.DATABITS_7;
    private int stopBits = SerialPort.STOPBITS_1;
    private int parity = SerialPort.PARITY_ODD;
    private int flowControl = SerialPort.FLOWCONTROL_RTSCTS_IN;
    private ScalesConnector connector;
    private boolean weightReceived = false;
    private int weight = 0;
    private DeviceError error = DeviceError.OK;
    private DataParser parser = new DataParser();

    {
        initScanners();
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setBaudRate(String baudRate) {
        this.baudRate = Integer.parseInt(baudRate);
    }

    public String getBaudRate() {
        return String.valueOf(baudRate);
    }

    public int getDataBits() {
        return dataBits;
    }

    public void setDataBits(int dataBits) {
        if (dataBits > 4 && dataBits < 9) {
            this.dataBits = dataBits;
        } else {
            this.dataBits = SerialPort.DATABITS_7;
        }
    }

    public String getStopBits() {
        switch (stopBits) {
            case SerialPort.STOPBITS_1:
                return "1";
            case SerialPort.STOPBITS_2:
                return "2";
            case SerialPort.STOPBITS_1_5:
                return "1.5";
            default:
                return "1";
        }
    }

    public void setStopBits(String stopBits) {
        if ("1".equalsIgnoreCase(stopBits)) {
            this.stopBits = SerialPort.STOPBITS_1;
        } else if ("2".equalsIgnoreCase(stopBits)) {
            this.stopBits = SerialPort.STOPBITS_2;
        } else if (stopBits.equalsIgnoreCase("1.5")) {
            this.stopBits = SerialPort.STOPBITS_1_5;
        } else if ("1,5".equalsIgnoreCase(stopBits)) {
            this.stopBits = SerialPort.STOPBITS_1_5;
        } else {
            this.stopBits = SerialPort.STOPBITS_1;
        }
    }

    public String getParity() {
        switch (parity) {
            case SerialPort.PARITY_NONE:
                return "NONE";
            case SerialPort.PARITY_EVEN:
                return "EVEN";
            case SerialPort.PARITY_ODD:
                return "ODD";
            case SerialPort.PARITY_MARK:
                return "MARK";
            case SerialPort.PARITY_SPACE:
                return "SPACE";
            default:
                return "NONE";
        }
    }

    public void setParity(String parity) {
        if ("NONE".equalsIgnoreCase(parity)) {
            this.parity = SerialPort.PARITY_NONE;
        } else if ("ODD".equalsIgnoreCase(parity)) {
            this.parity = SerialPort.PARITY_ODD;
        } else if ("EVEN".equalsIgnoreCase(parity)) {
            this.parity = SerialPort.PARITY_EVEN;
        } else if ("MARK".equalsIgnoreCase(parity)) {
            this.parity = SerialPort.PARITY_MARK;
        } else if ("SPACE".equalsIgnoreCase(parity)) {
            this.parity = SerialPort.PARITY_SPACE;
        } else {
            this.parity = SerialPort.PARITY_ODD;
        }
    }

    public String getFlowControl() {
        switch (flowControl) {
            case SerialPort.FLOWCONTROL_NONE:
                return "NONE";
            case SerialPort.FLOWCONTROL_RTSCTS_IN:
                return "RTSCTS_IN";
            case SerialPort.FLOWCONTROL_RTSCTS_OUT:
                return "RTSCTS_OUT";
            case SerialPort.FLOWCONTROL_XONXOFF_IN:
                return "XONXOFF_IN";
            case SerialPort.FLOWCONTROL_XONXOFF_OUT:
                return "XONXOFF_OUT";
            default:
                return "NONE";
        }
    }

    public void setFlowControl(String flowControl) {
        if ("NONE".equalsIgnoreCase(flowControl)) {
            this.flowControl = SerialPort.FLOWCONTROL_NONE;
        } else if ("RTSCTS_IN".equalsIgnoreCase(flowControl)) {
            this.flowControl = SerialPort.FLOWCONTROL_RTSCTS_IN;
        } else if ("RTSCTS_OUT".equalsIgnoreCase(flowControl)) {
            this.flowControl = SerialPort.FLOWCONTROL_RTSCTS_OUT;
        } else if ("XONXOFF_IN".equalsIgnoreCase(flowControl)) {
            this.flowControl = SerialPort.FLOWCONTROL_XONXOFF_IN;
        } else if ("XONXOFF_OUT".equalsIgnoreCase(flowControl)) {
            this.flowControl = SerialPort.FLOWCONTROL_XONXOFF_OUT;
        } else {
            this.flowControl = SerialPort.FLOWCONTROL_RTSCTS_IN;
        }
    }

    public void initScanners() {
        scanners = new ArrayList<>();
        ScannerViaScales scanner = BundleManager.get(ScannerViaScales.class);
        if (scanner != null) {
            LOGGER.debug("obj instanceof ScannerViaScales");
            scanner.setScale(this);
            scanners.add(scanner);
            LOGGER.debug("ScannerViaScales has been added to scanners");
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

    private class BarcodeScannerListener implements Runnable {
        private final Duration readDataTimeout = Duration.ofMillis(25);
        private StringBuilder readData = new StringBuilder();
        private boolean sent = true;

        private void success() {
            String readedString = readData.toString();
            LOGGER.debug("Have received a string: " + readedString);
            List<DeviceResponse> responses = parser.tryParseData(readedString);
            for (DeviceResponse resp : responses) {
                if (resp.isWeight()) {
                    processWeight(resp);
                } else if (resp.isBarcode()) {
                    processBarcode(resp);
                }
            }
            sent = true;
            if (readData.length() > 0) {
                readData.delete(0, readData.length());
            }
        }

        private void processWeight(DeviceResponse data) {
            if (data.getError() != null && data.getError() != DeviceError.OK) {
                error = data.getError();
                weight = 0;
            } else {
                weight = data.getWeigth();
            }
            weightReceived = true;
        }

        private void processBarcode(DeviceResponse data) {
            if (data.getBarcode() == null) {
                return;
            }
            LOGGER.debug("Parsed barcode: {}", data.getBarcode());
            if (scanners == null) {
                initScanners();
            }
            executor.execute(new BarcodeScannerInvoker(data.getBarcode()));
        }

        @Override
        public void run() {
            // TODO SRTZ-301 Переделать на чтение полными пакетами вместо таймаута
            try {
                final Timer timeOut = Timer.of(readDataTimeout);
                while (!Thread.currentThread().isInterrupted()) {
                    if (connector.available() > 0) {
                        sent = false;
                        timeOut.restart();
                        for (byte b : connector.readAll()) {
                            readData.append((char) b);
                        }
                        Thread.sleep(2);
                    } else {
                        if (!sent) {
                            if (timeOut.isExpired()) {
                                success();
                            }
                            Thread.sleep(2);
                        } else {
                            Thread.sleep(5);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }
    }

    @Override
    public void start() throws CashException {
        LOGGER.debug("start()");
        try {
            connector = new ScalesConnector();
            connector.open(port, baudRate, dataBits, stopBits, parity, flowControl);
        } catch (Exception e) {
            throw new CashException(e);
        }
        executor.execute(new BarcodeScannerListener());
    }

    @Override
    public void stop() {
        LOGGER.debug("stop()");
        executor.shutdown();
        connector.close();
    }

    @Override
    public int getWeight() throws ScaleException {
        try {
            weightReceived = false;
            connector.write(getWeightCommand);
            final Timer start = Timer.of(WAIT_TIMEOUT);
            while (!weightReceived) {
                if (start.isExpired()) {
                    throw new ScaleException(DeviceError.SCALES_NOT_RESPOND.getMessage());
                }
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    weightReceived = false;
                    return 0;
                }
            }
            weightReceived = false;
            if (error == DeviceError.NEGATIVE_WEIGHT || error == DeviceError.SCALES_NOT_READY) {
                throw new ScaleNegativeException(error.getMessage());
            } else if (!NOT_FATAL_ERRORS.contains(error)) {
                throw new ScaleException(error.getMessage());
            }
            return weight;
        } catch (IOException e) {
            LOGGER.debug("Error on get weight", e);
            throw new ScaleException(e.getMessage());
        }
    }

    @Override
    public Boolean moduleCheckState() {
        return connector != null && connector.isActive();
    }

    @Override
    public void goodScanBeep() {
        try {
            connector.write(beepCommand);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    @Override
    public void errorBeep() {
        try {
            connector.write(beepCommand);
            Thread.sleep(100);
            connector.write(beepCommand);
            Thread.sleep(100);
            connector.write(beepCommand);
            Thread.sleep(100);
            connector.write(beepCommand);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    @Override
    public void scannerTurnOn() throws IOException {
        connector.write(turnOnCommand);
    }

    @Override
    public void tare() throws IOException {
        connector.write(scaleSoftReset);
    }

    @Override
    public void scaleSoftReset() throws IOException {
        connector.write(scaleSoftReset);
    }

}
