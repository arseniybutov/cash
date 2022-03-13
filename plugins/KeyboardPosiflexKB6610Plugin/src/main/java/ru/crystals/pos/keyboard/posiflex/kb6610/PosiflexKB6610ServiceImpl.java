package ru.crystals.pos.keyboard.posiflex.kb6610;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.bundles.BundleManager;
import ru.crystals.pos.CashEventSource;
import ru.crystals.pos.emsr.ExternalMSR;
import ru.crystals.pos.keyboard.KeyboardImpl;
import ru.crystals.pos.keyboard.KeyboardPlugin;
import ru.crystals.pos.keyboard.PosiflexMSRUtils;
import ru.crystals.pos.keyboard.ResBundleKeyboard;
import ru.crystals.pos.keyboard.ScanCodesProcessor;
import ru.crystals.pos.keyboard.exception.KeyboardException;
import ru.crystals.pos.keyboard.plugin.KeyLockProcessor;
import ru.crystals.pos.keyboard.plugin.MSRProcessor;
import ru.crystals.pos.keylock.KeyLockEvent;
import ru.crystals.pos.msr.MSREvent;
import ru.crystals.pos.utils.CommonLogger;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class PosiflexKB6610ServiceImpl implements KeyboardPlugin, KeyListener {
    private static final Logger commonLogger = CommonLogger.getCommonLogger();
    private ScanCodesProcessor keyboardModule = null;
    private MSREvent msrListener = null;
    private KeyLockEvent keyLockListener = null;
    private long keyboardTimeOut = 50L;
    // queueTimeOut не меньше 30! Иначе не успеваем считать все с клавы!
    private long queueTimeOut = 50L;
    private Long otherTimeOut = 30L;
    private List<Integer> cardPrefix = null;
    private List<Integer> cardSufix = null;
    private List<Integer> cardPrefix2 = null;
    private List<Integer> cardSufix2 = null;
    private List<Integer> cardPrefix3;
    private List<Integer> cardSufix3;
    private List<Integer> keyLockPrefix;
    private List<Integer> keyLockSufix;
    private Map<String, Integer> keyLockMap;
    private boolean useKeyLockMap;
    protected final Logger LOG = LoggerFactory.getLogger(KeyboardImpl.class);
    private ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final int SLEEP_TIME = 5;
    private static final int SECOND_TRACK_WAITING_TIME = 50;
    private static final int SCANNER_TIME_OUT = 1000;
    private static final int TIME_OUT = 3000;
    private volatile Queue<Integer> scanCodes = new LinkedBlockingQueue<>();
    private static final int SCANNER_THRESHOLD = 4;
    private volatile boolean keyPressed = false;
    private volatile boolean keyTyped = false;
    private volatile boolean keyReleased = false;
    private int scanCode;
    private final String FALSE_SCANNER_EVENT = "False scanner event";

    @Override
    public void start() throws KeyboardException {
        long time = System.currentTimeMillis();

        CashEventSource.getInstance().addFrameKeyListener(this);

        executor.execute(new KeyboardListener());

        if (!validatePrefix(keyLockPrefix)) {
            LOG.warn("keyLockPrefix is not valid");
        }
        if (!validatePrefix(cardPrefix)) {
            LOG.warn("cardPrefix is not valid");
        }
        if (!validatePrefix(cardPrefix2)) {
            LOG.warn("cardPrefix2 is not valid");
        }

        commonLogger.debug("Time of loading keyboard = " + (System.currentTimeMillis() - time) + " ms");
    }

    private boolean validatePrefix(List<Integer> prefix) {
        return prefix != null && !prefix.isEmpty();
    }

    @Override
    public void stop() throws KeyboardException {
        try {
            executor.shutdown();
        } catch (Exception e) {
            LOG.error("", e);
            throw new KeyboardException(ResBundleKeyboard.getString("ERROR_STOP"));
        }
    }

    private class KeyboardListener implements Runnable {
        @Override
        public void run() {
            KeyLockProcessor keyLockProcessor = new KeyLockProcessor(keyLockPrefix, keyLockSufix, keyLockMap, useKeyLockMap, keyLockListener);
            MSRProcessor msrProcessor = new MSRProcessor(cardPrefix, cardSufix, cardPrefix2, cardSufix2, cardPrefix3, cardSufix3, msrListener);

            long startTime = 0;
            boolean barcodeScannerEvent = false;

            ExternalMSR emsr = null;
            MSRProcessor emsrProcessor = null;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (keyReleased || keyTyped) {
                        startTime = System.currentTimeMillis();
                        keyReleased = false;
                        keyTyped = false;
                    } else {
                        long startTime2 = System.currentTimeMillis();
                        if ((scanCodes.peek() != null) && (startTime2 - startTime > queueTimeOut)) {
                            LOG.debug("size = " + scanCodes.size());

                            if (emsr == null) {
                                emsr = BundleManager.get(ExternalMSR.class);
                                if (emsr != null) {
                                    emsrProcessor = new MSRProcessor(emsr, msrListener);
                                }
                            }

                            if (isEvent(keyLockPrefix)) {
                                int finalScanCode = keyLockSufix.get(keyLockSufix.size() - 1);
                                List<Integer> scanCodeList = createScanCodeListFromQueue(finalScanCode);
                                if (scanCodeList != null) {
                                    keyLockProcessor.process(scanCodeList);
                                } else {
                                    scanCodes.clear();
                                }
                                //Внимание! Используем переменные из утильного класса! Последовательность символов пришедшая с клавы нормализуется в теле условия.
                            } else if (isMSREvent(PosiflexMSRUtils.CARDPREFIX, PosiflexMSRUtils.CARDSUFIX, PosiflexMSRUtils.CARDSUFIX2)) {
                                scanCodes = PosiflexMSRUtils.parseQueue(scanCodes.toArray(new Integer[0]));
                                List<Integer> scanCodeList = createScanCodeListForMSR(cardSufix, cardPrefix2, cardSufix2);
                                if (scanCodeList != null) {
                                    msrProcessor.process(scanCodeList);
                                } else {
                                    scanCodes.clear();
                                }
                                //Внимание! Используем переменные из утильного класса! Последовательность символов пришедшая с клавы нормализуется в теле условия.
                            } else if (isMSREvent(PosiflexMSRUtils.CARDPREFIX2, PosiflexMSRUtils.CARDSUFIX, PosiflexMSRUtils.CARDSUFIX2)) {
                                int finalScanCode = cardSufix2.get(cardSufix2.size() - 1);
                                scanCodes = PosiflexMSRUtils.parseQueue(scanCodes.toArray(new Integer[0]));
                                List<Integer> scanCodeList = createScanCodeListFromQueue(finalScanCode);
                                if (scanCodeList != null) {
                                    msrProcessor.process(scanCodeList);
                                } else {
                                    scanCodes.clear();
                                }
                            } else if (emsr != null && isEvent(emsr.getCardPrefix1())) {
                                List<Integer> scanCodeList =
                                        createScanCodeListForMSR(emsr.getCardSuffix1(), emsr.getCardPrefix2(), emsr.getCardSuffix2());
                                if (scanCodeList != null) {
                                    processExternalMSREvent(emsr, emsrProcessor, scanCodeList);
                                } else {
                                    scanCodes.clear();
                                }
                            } else if (emsr != null && isEvent(emsr.getCardPrefix2())) {
                                int finalScanCode = emsr.getCardSuffix2().get(emsr.getCardSuffix2().size() - 1);
                                List<Integer> scanCodeList = createScanCodeListFromQueue(finalScanCode);
                                if (scanCodeList != null) {
                                    processExternalMSREvent(emsr, emsrProcessor, scanCodeList);
                                } else {
                                    scanCodes.clear();
                                }
                                // Защита от случайного нажатия нескольких
                                // клавиш
                            } else if (scanCodes.size() < SCANNER_THRESHOLD && !barcodeScannerEvent) {
                                keyboardModule.processKeyboard(getQueueCopy(scanCodes));
                                // Если поймали больше SCANNER_THRESHOLD - 1
                                // клавиш, то
                                // значит это сканер, а не залипание
                            } else {
                                boolean isDone = keyboardModule.processScanner(getBarcodeQueueCopy(scanCodes));
                                barcodeScannerEvent = !isDone;
                            }

                            if ((scanCodes.peek() != null) && !isEvent(keyLockPrefix) && !isEvent(PosiflexMSRUtils.CARDPREFIX) && !isEvent(PosiflexMSRUtils.CARDPREFIX2) &&
                                    !barcodeScannerEvent) {
                                scanCodes.clear();
                            }
                        }
                    }

                    if ((keyboardModule.getScannerTimeStamp() != -1) &&
                            (System.currentTimeMillis() - keyboardModule.getScannerTimeStamp() > SCANNER_TIME_OUT)) {
                        throw new KeyboardException(FALSE_SCANNER_EVENT);
                    }

                    Thread.sleep(SLEEP_TIME);
                } catch (Exception e) {
                    processScanCodesError(e);
                    barcodeScannerEvent = false;
                }
            }
        }

        //Без 3,4 проверки цифровая клавиатура попадает в обработку ридера
        private boolean isEvent(List<Integer> prefix) {
            if (validatePrefix(prefix) && scanCodes.size() > prefix.size()) {
                Integer[] codes = scanCodes.toArray(new Integer[0]);
                for (int i = 0; i < prefix.size(); i++) {
                    if (!codes[i].equals(prefix.get(i))) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        private boolean isMSREvent(List<Integer> prefix, List<Integer> suffix1, List<Integer> suffix2) {
            if (validatePrefix(prefix) && scanCodes.size() > 12) {
                Integer[] codes = scanCodes.toArray(new Integer[0]);
                for (int i = 0; i < prefix.size(); i++) {
                    if (!codes[i].equals(prefix.get(i))) {
                        return false;
                    }
                }
                if (codes[codes.length - 1].equals(10)) {
                    codes = Arrays.copyOf(codes, codes.length - 1);
                }
                return (validatePrefix(suffix1) && searchSuffix(codes, suffix1)) || (validatePrefix(suffix2) && searchSuffix(codes, suffix2));
            }
            return false;
        }

        private boolean searchSuffix(Integer[] scancodes, List<Integer> suffix) {
            Integer[] searchArray = new Integer[suffix.size()];
            System.arraycopy(scancodes, scancodes.length - suffix.size(), searchArray, 0, searchArray.length);
            return Arrays.equals(searchArray, suffix.toArray(new Integer[0]));
        }

        private void processExternalMSREvent(ExternalMSR emsr, MSRProcessor emsrProcessor, List<Integer> scanCodeList) {
            String[] tracks = emsr.getTracks(scanCodeList);
            if (tracks != null) {
                msrListener.eventMSR(tracks[0], tracks[1], tracks[2], tracks[3]);
            } else {
                emsrProcessor.process(scanCodeList);
            }
        }

        private List<Integer> createScanCodeListFromQueue(int finalScanCode) throws InterruptedException {
            // Очередь scanCodes должна содержать по меньшей мере 2 символа

            List<Integer> scanCodeList = null;

            int scanCodesSize = 0;
            boolean queueIsFull = false;

            long startTime = System.currentTimeMillis();

            while ((System.currentTimeMillis() - startTime) < TIME_OUT) {
                if ((scanCodes.size() > 1) && (getLastScanCode(scanCodes) == finalScanCode)) {
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
                    if ((i > 0) && (scanCode == finalScanCode)) {
                        break;
                    }
                }
            }

            return scanCodeList;
        }

        private List<Integer> createScanCodeListForMSR(List<Integer> cardSufix, List<Integer> cardPrefix2, List<Integer> cardSufix2)
                throws InterruptedException {
            // Очередь scanCodes должна содержать по меньшей мере 2 символа

            List<Integer> scanCodeList = null;

            int scanCodesSize = 0;
            boolean queueIsFull = false;

            int finalScanCode = cardSufix.get(cardSufix.size() - 1);
            int firstScanCode2 = cardPrefix2.get(0);
            int finalScanCode2 = cardSufix2.get(cardSufix2.size() - 1);

            long startTime = System.currentTimeMillis();

            while ((System.currentTimeMillis() - startTime) < TIME_OUT) {
                if (scanCodes.size() > 1) {
                    int lastScanCode = getLastScanCode(scanCodes);

                    if (lastScanCode == finalScanCode2 && scanCodes.contains(firstScanCode2)) {
                        scanCodesSize = scanCodes.size();
                        queueIsFull = true;
                        break;
                    } else if (lastScanCode == finalScanCode) {
                        Thread.sleep(SECOND_TRACK_WAITING_TIME);
                        if (getLastScanCode(scanCodes) == finalScanCode) {
                            scanCodesSize = scanCodes.size();
                            queueIsFull = true;
                            break;
                        }
                    }
                }
                Thread.sleep(SLEEP_TIME);
            }

            if (queueIsFull) {
                scanCodeList = new ArrayList<>();
                for (int i = 0; i < scanCodesSize; i++) {
                    scanCodeList.add(scanCodes.poll());
                }
            }

            return scanCodeList;
        }

        private Queue<Long> getQueueCopy(Queue<Integer> queue) {
            Queue<Long> queueCopy = new LinkedBlockingQueue<>();
            for (int i = 0; i < queue.size(); i++) {
                queueCopy.offer(queue.poll().longValue());
            }
            return queueCopy;
        }

        private Queue<Integer> getBarcodeQueueCopy(Queue<Integer> queue) {
            int scanCodesSize = queue.size();
            Queue<Integer> queueCopy = new LinkedBlockingQueue<>();
            for (int i = 0; i < scanCodesSize; i++) {
                queueCopy.offer(queue.poll());
            }
            return queueCopy;
        }

        private int getLastScanCode(Queue<Integer> scanCodes) {
            return scanCodes.toArray(new Integer[0])[scanCodes.size() - 1];
        }
    }

    private void processScanCodesError(Exception e) {
        keyboardModule.logError(scanCodes, e);
        scanCodes.clear();
        keyboardModule.clear();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        scanCode = e.getKeyCode();
        if (scanCode > 7000) {
            LOG.debug("keyPressed0: >>>" + this.scanCode + "<<<");
            scanCodes.offer(this.scanCode);
        }
        keyPressed = true;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        keyTyped = true;
        keyPressed = false;

        int scanCode = (int) e.getKeyChar();

        if (this.scanCode >= 65 && this.scanCode <= 90) {
            LOG.debug("keyPressed1: >>>" + this.scanCode + "<<<");
            scanCodes.offer(this.scanCode);
        } else if (this.scanCode != scanCode) {
            if (scanCode >= 33 && scanCode <= 38 || scanCode >= 40 && scanCode <= 43 || scanCode == 58 || scanCode == 94 ||
                    scanCode >= 60 && scanCode <= 64 || scanCode >= 48 && scanCode <= 57) {
                LOG.debug("keyTyped1: >>>" + scanCode + "<<<");
                scanCodes.offer(scanCode);
            } else {
                LOG.debug("keyPressed2: >>>" + this.scanCode + "<<<");
                scanCodes.offer(this.scanCode);
            }
        } else {
            LOG.debug("keyTyped2: >>>" + scanCode + "<<<");
            scanCodes.offer(scanCode);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keyReleased = true;

        int scanCode = e.getKeyCode();
        if (keyPressed) {

            //залипуха сделана для правильной работы картридеров (у многих префикс 37)
            //антизалипуха находится - KeyboardImpl.sendKey
            if (scanCode == 37) {
                scanCode = 3737;
            }
            if (scanCode == 8) {
                scanCode = 3939;
            }
            LOG.error("keyReleased: !!! >>>" + scanCode + "<<<");
            scanCodes.offer(scanCode);
        }
    }

    public void setMsrListener(MSREvent msrListener) {
        this.msrListener = msrListener;
    }

    public MSREvent getMsrListener() {
        return msrListener;
    }

    public void setKeyLockListener(KeyLockEvent keyLockListener) {
        this.keyLockListener = keyLockListener;
    }

    public KeyLockEvent getKeyLockListener() {
        return keyLockListener;
    }

    public void setCardPrefix(List<Integer> cardPrefix) {
        this.cardPrefix = cardPrefix;
    }

    public List<Integer> getCardPrefix() {
        return cardPrefix;
    }

    public void setCardPrefix2(List<Integer> cardPrefix2) {
        this.cardPrefix2 = cardPrefix2;
    }

    public List<Integer> getCardPrefix2() {
        return cardPrefix2;
    }

    public List<Integer> getCardPrefix3() {
        if (cardPrefix3 == null) {
            cardPrefix3 = new ArrayList<>();
            cardPrefix3.add(43);
        }
        return cardPrefix3;
    }

    @Override
    public void setCardPrefix3(List<Integer> cardPrefix3) {
        this.cardPrefix3 = cardPrefix3;
    }

    public void setCardSufix(List<Integer> cardSufix) {
        this.cardSufix = cardSufix;
    }

    public List<Integer> getCardSufix() {
        return cardSufix;
    }

    public void setCardSufix2(List<Integer> cardSufix2) {
        this.cardSufix2 = cardSufix2;
    }

    public List<Integer> getCardSufix2() {
        return cardSufix2;
    }

    public List<Integer> getCardSufix3() {
        if (cardSufix3 == null) {
            cardSufix3 = new ArrayList<>();
            cardSufix3.add(63);
        }
        return cardSufix3;
    }

    @Override
    public void setCardSufix3(List<Integer> cardSufix3) {
        this.cardSufix3 = cardSufix3;
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

    public List<Integer> getKeyLockPrefix() {
        return keyLockPrefix;
    }

    public void setKeyLockPrefix(List<Integer> keyLockPrefix) {
        this.keyLockPrefix = keyLockPrefix;
    }

    public List<Integer> getKeyLockSufix() {
        return keyLockSufix;
    }

    public void setKeyLockSufix(List<Integer> keyLockSufix) {
        this.keyLockSufix = keyLockSufix;
    }

    public boolean isUseKeyLockMap() {
        return useKeyLockMap;
    }

    public void setUseKeyLockMap(boolean useKeyLockMap) {
        this.useKeyLockMap = useKeyLockMap;
    }

    public Map<String, Integer> getKeyLockMap() {
        return keyLockMap;
    }

    public void setKeyLockMap(Map<String, Integer> keyLockMap) {
        this.keyLockMap = keyLockMap;
    }

    public ScanCodesProcessor getKeyboardModule() {
        return keyboardModule;
    }

    public void setKeyboardModule(ScanCodesProcessor keyboardModule) {
        this.keyboardModule = keyboardModule;
    }

    public long getQueueTimeOut() {
        return queueTimeOut;
    }

    public void setQueueTimeOut(long queueTimeOut) {
        this.queueTimeOut = queueTimeOut;
    }

    @Override
    public void setKeyboardContactBounceTime(long contactBounceTime) {

    }

    @Override
    public void putStringAsKeys(String data) {
        List<Integer> codes = new LinkedList<>();
        for (char c : data.toCharArray()) {
            codes.add((int) c);
        }
        scanCodes.addAll(codes);
    }
}
