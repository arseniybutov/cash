package ru.crystals.pos.keyboard.jpos;

import jpos.JposException;
import jpos.POSKeyboard;
import jpos.events.DataEvent;
import jpos.events.DataListener;
import jpos.events.ErrorEvent;
import jpos.events.ErrorListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.barcodescanner.BarcodeScannerEvent;
import ru.crystals.pos.keyboard.KeyboardEvent;
import ru.crystals.pos.keyboard.KeyboardImpl;
import ru.crystals.pos.keyboard.KeyboardPlugin;
import ru.crystals.pos.keyboard.ResBundleKeyboard;
import ru.crystals.pos.keyboard.ScanCodesProcessor;
import ru.crystals.pos.keyboard.datastruct.AlphaNumericKey;
import ru.crystals.pos.keyboard.datastruct.ControlKey;
import ru.crystals.pos.keyboard.datastruct.FunctionKey;
import ru.crystals.pos.keyboard.datastruct.GoodsKey;
import ru.crystals.pos.keyboard.datastruct.UnmappedKey;
import ru.crystals.pos.keyboard.exception.KeyboardException;
import ru.crystals.pos.keylock.KeyLockEvent;
import ru.crystals.pos.msr.MSREvent;
import ru.crystals.pos.utils.CommonLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JPosKeyboardServiceImpl implements DataListener, ErrorListener, KeyboardPlugin {

    private static final Logger commonLogger = CommonLogger.getCommonLogger();

    private KeyboardImpl keyboardModule = null;
    private BarcodeScannerEvent barcodeScannerListener = null;
    private KeyboardEvent keyboardListener = null;

    private String logLevel = "error";

    private String model = null;
    private String scannerModel = null;

    public static final Logger LOG = LoggerFactory.getLogger(KeyboardImpl.class);

    private List<POSKeyboard> keyboards = new ArrayList<>();

    private StringBuilder barCode = new StringBuilder();
    private StateRead stateRead = StateRead.READ_KEY;

    private KeyListener keyListener = null;
    private static final int TIME_OUT = 20;
    private volatile boolean isKeyPressed = false;
    private volatile List<Long> scanCodes = new ArrayList<>();

    private enum StateRead {
        READ_KEY, READ_PREFIX, READ_BARCODE, READ_SUFFIX;
    }

    @Override
    public void start() throws KeyboardException {
        long time = System.currentTimeMillis();

        try {
            keyboards.clear();
            keyboards.add(new POSKeyboard());

            if ((getScannerModel() != null)
                    && !getScannerModel().isEmpty()
                    && !getScannerModel().equalsIgnoreCase(getModel())) {
                keyboards.add(new POSKeyboard());
            }

            for (byte i = 0; i < keyboards.size(); i++) {
                POSKeyboard keyboard = keyboards.get(i);

                if (i == 0) {
                    keyboard.open(getModel());
                } else {
                    keyboard.open(getScannerModel());
                }

                keyboard.claim(1000);
                if (!keyboard.getClaimed()) {
                    throw new Exception("Keyboard not claimed");
                }

                keyboard.setDeviceEnabled(true);
                keyboard.addDataListener(this);
                keyboard.addErrorListener(this);
                keyboard.setDataEventEnabled(true);
            }

        } catch (Exception e) {
            LOG.error("", e);
            throw new KeyboardException(ResBundleKeyboard.getString("ERROR_JPOS"));
        }

        setKeyListener(new KeyListener());
        getKeyListener().start();

        commonLogger.debug("Time of loading keyboard = " + (System.currentTimeMillis() - time) + " ms");
    }

    @Override
    public void stop() throws KeyboardException {
        try {
            for (POSKeyboard keyboard : keyboards) {
                keyboard.setDataEventEnabled(false);
                keyboard.setDeviceEnabled(false);
                if (keyboard.getClaimed()) {
                    keyboard.release();
                }
                keyboard.close();
            }
        } catch (Exception e) {
            LOG.error("", e);
            throw new KeyboardException(ResBundleKeyboard.getString("ERROR_JPOS"));
        }
        getKeyListener().interrupt();
    }

    private class KeyListener extends Thread {

        public void run() {

            long startTime = 0;

            while (!interrupted()) {
                try {
                    if (isKeyPressed()) {
                        startTime = System.currentTimeMillis();
                        setKeyPressed(false);
                    } else {
                        if ((System.currentTimeMillis() - startTime) > TIME_OUT) {
                            if (!getScanCodes().isEmpty()) {
                                //Защита от случайного нажатия нескольких клавиш
                                if (getScanCodes().size() < 7) {
                                    processScanCode(getScanCodes().get(0));
                                    //Если поймали больше 7 клавиш, то значит это сканер, а не залипание
                                } else {
                                    barCode = new StringBuilder();
                                    for (long scanCode : getScanCodes()) {
                                        scanCode = scanCode & 0x7F;
                                        if (scanCode >= 0x20) {
                                            if (scanCode == 62) {
                                                scanCode = 46;
                                            }
                                            barCode.append((char) scanCode);
                                        }
                                    }

                                    if (barcodeScannerListener != null) {
                                        barcodeScannerListener.eventBarcodeScanner(barCode.toString());
                                    }
                                }
                                getScanCodes().clear();
                            }
                        }
                    }

                    Thread.sleep(10);
                } catch (Exception e) {
                    LOG.error("", e);
                }
            }
        }
    }

    public void processScanCode(long newScanCode) {
        if (keyboardModule.getTableLayout().containsKey(newScanCode)) {
            UnmappedKey key = keyboardModule.getTableLayout().get(newScanCode);
            if (key instanceof AlphaNumericKey) {
                getKeyboardListener().eventAlphaNumericKey((AlphaNumericKey) key);
            } else if (key instanceof FunctionKey) {
                getKeyboardListener().eventFunctionKey((FunctionKey) key);
            } else if (key instanceof ControlKey) {
                getKeyboardListener().eventControlKey((ControlKey) key);
            } else if (key instanceof GoodsKey) {
                getKeyboardListener().eventGoodsKey((GoodsKey) key);
            }
            return;
        }
        getKeyboardListener().eventUnmappedKey(new UnmappedKey(newScanCode));
    }


    @Override
    public void dataOccurred(DataEvent keyboardEvent) {
        try {
            long scanCode = ((POSKeyboard) keyboardEvent.getSource()).getPOSKeyData();
            commonLogger.debug("     Scan code: >>>" + scanCode + "<<<");
            if (scanCode == 0) {
                return;
            }

            if (stateRead == StateRead.READ_KEY) {
                if (keyboardListener != null) {
                    setKeyPressed(true);
                    getScanCodes().add(scanCode);
                    commonLogger.debug("     code: >>>" + scanCode + "<<<");
                }
            } else if (stateRead == StateRead.READ_PREFIX) {
                stateRead = StateRead.READ_KEY;
                setKeyPressed(true);
                getScanCodes().add(scanCode);
            } else if (stateRead == StateRead.READ_BARCODE) {
                char symbol = (char) (scanCode & 0x7F);
                if (symbol == 62) {
                    symbol = 46;
                }
                if (scanCode != 272) {
                    barCode.append(symbol);
                }
                if (barCode.length() > 30) {
                    stateRead = StateRead.READ_KEY;
                }
            } else if (stateRead == StateRead.READ_SUFFIX) {
                stateRead = StateRead.READ_KEY;
            }
        } catch (Exception e) {
            LOG.error("", e);
        } finally {
            try {
                ((POSKeyboard) keyboardEvent.getSource()).setDataEventEnabled(true);
            } catch (JposException e) {
                LOG.error("", e);
            }
        }
    }


    @Override
    public void errorOccurred(ErrorEvent errorEvent) {
        POSKeyboard keyb = ((POSKeyboard) errorEvent.getSource());
        try {
            LOG.error(keyb.getCheckHealthText() + " ErrorCode=" + errorEvent.getErrorCode() + " ExtendedErrorCode=" + errorEvent.getErrorCodeExtended());
        } catch (Exception e) {
            LOG.error("", e);
        }
    }


    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setKeyboardModule(KeyboardImpl keyboardModule) {
        this.keyboardModule = keyboardModule;
    }

    public KeyboardImpl getKeyboardModule() {
        return keyboardModule;
    }

    public void setBarcodeScannerListener(BarcodeScannerEvent barcodeScannerListener) {
        this.barcodeScannerListener = barcodeScannerListener;
    }

    public BarcodeScannerEvent getBarcodeScannerListener() {
        return barcodeScannerListener;
    }

    public void setModule(KeyboardImpl module) {
        this.keyboardModule = module;
    }

    public KeyboardImpl getModule() {
        return keyboardModule;
    }

    public void setKeyListener(KeyListener keyListener) {
        this.keyListener = keyListener;
    }

    public KeyListener getKeyListener() {
        return keyListener;
    }

    public synchronized void setKeyPressed(boolean isKeyPressed) {
        this.isKeyPressed = isKeyPressed;
    }

    public synchronized boolean isKeyPressed() {
        return isKeyPressed;
    }

    public synchronized void setScanCodes(List<Long> scanCodes) {
        this.scanCodes = scanCodes;
    }

    public synchronized List<Long> getScanCodes() {
        return scanCodes;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getModel() {
        return model;
    }

    public void setScannerModel(String scannerModel) {
        this.scannerModel = scannerModel;
    }

    public String getScannerModel() {
        return scannerModel;
    }

    public void setKeyboardListener(KeyboardEvent keyboardListener) {
        this.keyboardListener = keyboardListener;
    }

    public KeyboardEvent getKeyboardListener() {
        return keyboardListener;
    }

    @Override
    public void setCardPrefix(List<Integer> cardPrefix) {
        // Not implemented

    }

    @Override
    public void setCardPrefix2(List<Integer> cardPrefix2) {
        // Not implemented

    }

    @Override
    public void setCardPrefix3(List<Integer> cardPrefix3) {
    }

    @Override
    public void setCardSufix(List<Integer> cardSuffix) {
        // Not implemented
    }

    @Override
    public void setCardSufix2(List<Integer> cardSuffix2) {
        // Not implemented
    }

    @Override
    public void setCardSufix3(List<Integer> cardSuffix3) {
        // Not implemented
    }

    @Override
    public void setKeyLockPrefix(List<Integer> keyLockPrefix) {
        // Not implemented
    }

    @Override
    public void setKeyLockSufix(List<Integer> keyLockSufix) {
        // Not implemented
    }

    @Override
    public void setUseKeyLockMap(boolean useKeyLockMap) {
        // Not implemented
    }

    @Override
    public boolean isUseKeyLockMap() {
        return false;
    }

    @Override
    public void setKeyLockMap(Map<String, Integer> keyLockMap) {
        // Not implemented
    }

    @Override
    public Map<String, Integer> getKeyLockMap() {
        return new HashMap<>();
    }

    @Override
    public void setKeyboardTimeOut(Long keyboardTimeOut) {
        // Not implemented
    }

    @Override
    public void setOtherTimeOut(Long otherTimeOut) {
        // Not implemented
    }

    @Override
    public void setMsrListener(MSREvent msrListener) {
        // Not implemented
    }

    @Override
    public void setKeyLockListener(KeyLockEvent keyLockListener) {
        // Not implemented
    }

    @Override
    public void setKeyboardModule(ScanCodesProcessor keyboardModule) {
        // Not implemented
    }

    @Override
    public void setKeyboardContactBounceTime(long contactBounceTime) {
        // Not implemented
    }

    @Override
    public void putStringAsKeys(String data) {
        List<Long> codes = new LinkedList<>();
        for (char c : data.toCharArray()) {
            codes.add((long) c);
        }
        scanCodes.addAll(codes);
    }

}
