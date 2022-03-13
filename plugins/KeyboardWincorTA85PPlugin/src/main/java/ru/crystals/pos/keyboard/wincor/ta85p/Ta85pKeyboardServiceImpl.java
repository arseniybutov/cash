package ru.crystals.pos.keyboard.wincor.ta85p;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.bundles.BundleManager;
import ru.crystals.pos.CashEventSource;
import ru.crystals.pos.InternalCashPoolExecutor;
import ru.crystals.pos.keyboard.IInterModuleBridge;
import ru.crystals.pos.keyboard.InterModuleBridge;
import ru.crystals.pos.keyboard.Key;
import ru.crystals.pos.keyboard.KeyboardImpl;
import ru.crystals.pos.keyboard.KeyboardPlugin;
import ru.crystals.pos.keyboard.ResBundleKeyboard;
import ru.crystals.pos.keyboard.ScanCodesProcessor;
import ru.crystals.pos.keyboard.exception.KeyboardException;
import ru.crystals.pos.keylock.KeyLockEvent;
import ru.crystals.pos.msr.MSREvent;
import ru.crystals.pos.utils.CommonLogger;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class Ta85pKeyboardServiceImpl implements KeyboardPlugin, KeyListener {

    private static final Logger commonLogger = CommonLogger.getCommonLogger();
    public static final Logger LOG = LoggerFactory.getLogger(KeyboardImpl.class);
    private static final int SCANNER_TIME_OUT = 1000;
    private static final int SLEEP_TIME = 25;
    private static final int SCANNER_THRESHOLD = 4;
    private static final String FALSE_SCANNER_EVENT = "False scanner event";
    private static final String WRONG_QUEUE_STATE = "Wrong queue state";
    private static final String KEY_READER_DRIVER = "./lib/keyboard/keyLockReaderDrv_ta85";

    private ScanCodesProcessor keyboardModule;

    private MSREvent msrListener;
    private KeyLockEvent keyLockListener;

    private InterModuleBridge interModuleBridge;

    private Long timeOut = 25L;

    private WNParser parser = new WNParser();

    private List<Integer> cardPrefix;
    private List<Integer> cardPrefix2;
    private List<Integer> cardSufix;
    private List<Integer> keyLockPrefix;
    private List<Integer> keyLockSufix;

    private boolean useKeyLockMap;
    private Map<String, Integer> keyLockMap;

    protected volatile Queue<Key> scanCodes = new LinkedBlockingQueue<>();

    private volatile boolean keyPressed;
    private volatile boolean keyTyped;
    private volatile boolean keyReleased;

    private volatile boolean msrChecked;
    private volatile boolean keyLockChecked;

    private int scanCode;
    private MainListener mainListener;

    @Override
    public void start() throws KeyboardException {
        long time = System.currentTimeMillis();

        interModuleBridge = InterModuleBridge.getInstance();
        BundleManager.add(IInterModuleBridge.class, interModuleBridge);

        cardPrefix = parser.getCardPrefix();
        if (CollectionUtils.isEmpty(cardPrefix2)) {
            cardPrefix2 = parser.getCardPrefix2();
        }
        cardSufix = parser.getCardSufix();

        keyLockPrefix = parser.getKeyLockPrefix();
        keyLockSufix = parser.getKeyLockSufix();
        try {
            CashEventSource.getInstance().addFrameKeyListener(this);

            mainListener = new MainListener();
            mainListener.start();

            //Считывание позиции ключа в отдельном потоке
            BundleManager.addListener(InternalCashPoolExecutor.class, () -> {
                BundleManager.get(InternalCashPoolExecutor.class).execute(() -> {
                    String position = casting();
                    if (keyLockMap != null && keyLockMap.containsKey(position.toLowerCase())) {
                        keyLockListener.eventKeyLock(keyLockMap.get(position.toLowerCase()));
                    } else {
                        keyLockListener.eventKeyLock(1);
                    }
                });
            });

            if (cardPrefix == null || cardPrefix.isEmpty()) {
                throw new Exception("cardPrefix exception");
            }
            if (cardPrefix2 == null || cardPrefix2.isEmpty()) {
                throw new Exception("cardPrefix2 exception");
            }
            if (keyLockPrefix == null || keyLockPrefix.isEmpty()) {
                throw new Exception("keyLockPrefix exception");
            }
        } catch (Exception e) {
            LOG.error("", e);
            throw new KeyboardException(ResBundleKeyboard.getString("ERROR_START"));
        }
        commonLogger.debug("Time of loading keyboard = " + (System.currentTimeMillis() - time) + " ms");
    }

    private String casting() {
        String nameDriver[] = {"sudo", KEY_READER_DRIVER};
        try {
            Process processDriver = Runtime.getRuntime().exec(nameDriver);
            processDriver.waitFor();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(processDriver.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                LOG.debug("get keylock position : {}", sb.toString());
                return sb.toString();
            }
        } catch (Exception e) {
            LOG.error("casting :", e);
        }
        return "0";
    }

    @Override
    public void stop() throws KeyboardException {
        try {
            CashEventSource.getInstance().addFrameKeyListener(null);
            mainListener.interrupt();
        } catch (Exception e) {
            LOG.error("", e);
            throw new KeyboardException(ResBundleKeyboard.getString("ERROR_STOP"));
        }
    }

    private final class MainListener extends Thread {

        private StringBuilder keyLockPosition = null;
        private List<Integer> readKeyLockPrefix = new ArrayList<>();
        private List<Integer> readKeyLockSuffix = new ArrayList<>();
        private int qty4Remove;
        private StringBuilder cardTracks = null;
        private List<Integer> readCardPrefix = new ArrayList<>();
        private List<Integer> readCardSuffix = new ArrayList<>();

        @Override
        public void run() {
            long startTime = 0;
            long lastDispatchScannerTime = 0;
            boolean barcodeScannerEvent = false;
            boolean caseSensitiveForJSON = false;

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (keyReleased || keyTyped) {
                        startTime = System.currentTimeMillis();
                        keyReleased = false;
                        keyTyped = false;
                    } else {
                        if ((System.currentTimeMillis() - startTime) > timeOut) {
                            if (!keyLockChecked) {
                                readKeyLocks();
                                continue;
                            }
                            if (!msrChecked) {
                                checkCard();
                                continue;
                            }

                            if (keyLockChecked && msrChecked && !scanCodes.isEmpty()) {
                                LOG.debug("size = " + scanCodes.size());
                                if (keyReleased || keyTyped) {
                                    Thread.sleep(SLEEP_TIME);
                                    continue;
                                }

                                Queue<Key> queueFragment = getQueueFragment();
                                LOG.trace("Queue fragment:");
                                StringBuilder sb = new StringBuilder();
                                for (Key key : queueFragment) {
                                    sb.append(key != null ? key.getScanCode() : "NULL" + ", ");
                                }
                                LOG.trace("{}", sb);
                                if ((queueFragment.size() < SCANNER_THRESHOLD && !barcodeScannerEvent) &&
                                        (System.currentTimeMillis() - lastDispatchScannerTime) > 50L) {
                                    LOG.trace("processKeyboard");
                                    keyboardModule.processKeyboard(Key.extractKeyCodesLong(queueFragment));
                                } else {
                                    LOG.trace("processScanner");
                                    // помодифицируем с учетом флага "учитывать регистр букв" или принадлежностью к json
                                    LinkedList<Integer> modifiedKeys = new LinkedList<>();
                                    for (Key key : queueFragment) {
                                        if (key != null) {
                                            Integer sourceCode = key.getSourceScanCode();
                                            if (sourceCode != null && isLoverCaseChar.test(sourceCode) &&
                                                    (interModuleBridge.isCaseSensitive() || caseSensitiveForJSON)) {
                                                modifiedKeys.add(sourceCode);
                                            } else {
                                                if (isStartEndJSONChar.test(key.getScanCode())) {
                                                    caseSensitiveForJSON = true;
                                                }
                                                modifiedKeys.add(key.getScanCode());
                                            }
                                        }
                                    }
                                    boolean isDone = keyboardModule.processScanner(modifiedKeys);
                                    lastDispatchScannerTime = System.currentTimeMillis();
                                    barcodeScannerEvent = !isDone;
                                    caseSensitiveForJSON &= barcodeScannerEvent;
                                    LOG.trace("barcodeScannerEvent=" + barcodeScannerEvent);
                                    LOG.trace("caseSensitiveForJSON=" + caseSensitiveForJSON);
                                }
                            }
                        }
                    }

                    if ((keyboardModule.getScannerTimeStamp() != -1) &&
                            (System.currentTimeMillis() - keyboardModule.getScannerTimeStamp() > SCANNER_TIME_OUT)) {
                        barcodeScannerEvent = false;
                        caseSensitiveForJSON = false;
                        throw new KeyboardException(FALSE_SCANNER_EVENT);
                    }

                    Thread.sleep(SLEEP_TIME);
                } catch (Exception e) {
                    processScanCodesError(e);
                }
            }
        }

        private void dispatchKeyLockEvent() {
            if (useKeyLockMap) {
                if (keyLockMap != null && keyLockMap.containsKey(keyLockPosition.toString().toLowerCase())) {
                    keyLockListener.eventKeyLock(keyLockMap.get(keyLockPosition.toString().toLowerCase()));
                }
            } else {
                keyLockListener.eventKeyLock(parser.parseKeyPosition(keyLockPosition.toString()));
            }
            for (int i = 0; i < qty4Remove; i++) {
                scanCodes.poll();
            }
            keyLockChecked = !queueContainsKeyLockPrefix();
        }

        private void readKeyLocks() {
            while (!keyLockChecked) {
                checkKeyLock();
            }
        }

        private void checkKeyLock() {
            if (queueContainsKeyLockPrefix()) {
                qty4Remove = 0;
                keyLockPosition = new StringBuilder();
                int state = 0;
                int previousScanCode = -1;
                int repeatCount = 0;
                for (Key scanCode : scanCodes) {
                    if (scanCode == null) {
                        continue;
                    }
                    int currentScanCode = scanCode.getScanCode();
                    LOG.trace("checkKeyLock : {}", currentScanCode);
                    if (previousScanCode == currentScanCode && repeatCount > 1) {
                        repeatCount++;
                        keyLockChecked = true;
                        break;
                    } else {
                        repeatCount++;
                        previousScanCode = currentScanCode;
                    }
                    if (state == -1) {
                        keyLockChecked = true;
                        break;
                    }
                    switch (state) {
                        case 0:
                            LOG.trace("checkKeyLock STATE0: {}", currentScanCode);
                            if (keyLockPrefix.contains(currentScanCode)) {
                                readKeyLockPrefix.add(currentScanCode);
                                qty4Remove++;
                            } else {
                                keyLockPosition.append((char) currentScanCode);
                                qty4Remove++;
                                if (keyLockPrefix.equals(readKeyLockPrefix) && keyLockSufix != null) {
                                    state = 1;
                                } else if (keyLockPrefix.equals(readKeyLockPrefix) && keyLockSufix == null) {
                                    dispatchKeyLockEvent();
                                    readKeyLockPrefix.clear();
                                    readKeyLockSuffix.clear();
                                    return;
                                } else {
                                    state = -1;
                                }
                            }
                            break;
                        case 1:
                            LOG.trace("checkKeyLock STATE1: {}", currentScanCode);
                            if (keyLockSufix.contains(currentScanCode)) {
                                readKeyLockSuffix.add(currentScanCode);
                                qty4Remove++;
                                if (keyLockSufix.equals(readKeyLockSuffix)) {
                                    dispatchKeyLockEvent();
                                }
                            } else {
                                keyLockPosition.append((char) currentScanCode);
                                qty4Remove++;
                            }
                            break;
                        default:
                            break;
                    }
                }
                readKeyLockPrefix.clear();
                readKeyLockSuffix.clear();
                keyLockChecked = true;
            } else {
                keyLockChecked = true;
            }
            LOG.trace("checkKeyLock keyLockChecked: {}", keyLockChecked);
        }

        public void readCards() {
            while (!msrChecked) {
                checkCard();
            }
        }

        private void checkCard() {
            if (queueContainsCardPrefix()) {
                int qty4Remove = 0;
                cardTracks = new StringBuilder();
                int state = 0;
                for (Key scanCode : scanCodes) {
                    if (scanCode == null) {
                        continue;
                    }
                    int currentScanCode = scanCode.getScanCode();
                    LOG.trace("checkCard : {}", currentScanCode);
                    if (state == -1) {
                        msrChecked = true;
                        break;
                    }
                    switch (state) {
                        case 0:
                            LOG.trace("checkCard STATE0: {}", currentScanCode);
                            if ((cardPrefix.contains(currentScanCode) || cardPrefix2.contains(currentScanCode))
                                    && (readCardPrefix.size() < cardPrefix.size() || readCardPrefix.size() < cardPrefix2.size())) {
                                readCardPrefix.add(currentScanCode);
                                if (readCardPrefix.size() == cardPrefix.size() && cardPrefix.equals(readCardPrefix)) {
                                    state = 1;
                                }
                                LOG.trace("checkCard readCardPrefix = {}, cardPrefix = {}, state = {}", readCardPrefix, cardPrefix, state);
                                qty4Remove++;
                            } else {
                                LOG.trace("checkCard add char0: {}, {}", scanCode, (char) currentScanCode);
                                cardTracks.append((char) currentScanCode);
                                qty4Remove++;
                                if (cardPrefix.equals(readCardPrefix) || cardPrefix2.equals(readCardPrefix)) {
                                    LOG.trace("checkCard found card prefix: readCardPrefix = {}, cardPrefix = {}, cardPrefix2 = {}", readCardPrefix, cardPrefix,
                                            cardPrefix2);
                                    state = 1;
                                } else {
                                    msrChecked = true;
                                    state = -1;
                                }
                            }
                            break;
                        case 1:
                            LOG.trace("checkCard STATE1: {}", currentScanCode);
                            if (cardSufix.contains(currentScanCode)) {
                                readCardSuffix.add(currentScanCode);
                                qty4Remove++;
                                if (cardSufix.equals(readCardSuffix)) {
                                    LOG.trace("checkCard parse cardTracks: {}", cardTracks);
                                    String[] tracks = parser.parseCard(cardTracks.toString());
                                    if (LOG.isTraceEnabled()) {
                                        int i = 0;
                                        for (String t : tracks) {
                                            LOG.trace("checkCard track: {} = >{}<", i, t);
                                            i++;
                                        }
                                    }
                                    msrListener.eventMSR(tracks[0], tracks[1], tracks[2], tracks[3]);
                                    for (int i = 0; i < qty4Remove; i++) {
                                        scanCodes.poll();
                                    }
                                    msrChecked = true;
                                    break;
                                }
                            } else {
                                if (currentScanCode != 16) {
                                    LOG.trace("checkCard add char1: {}, {}", currentScanCode, (char) currentScanCode);
                                    cardTracks.append((char) currentScanCode);
                                }
                                qty4Remove++;
                            }
                            break;
                        default:
                            break;
                    }
                }
                LOG.trace("checkCard cardTracks: {}", cardTracks);
                readCardPrefix.clear();
                readCardSuffix.clear();
            } else {
                msrChecked = true;
            }
        }

        /**
         * Получить фрагмент очереди, исключая keyLock внутри
         *
         * @return
         * @throws KeyboardException
         * @throws InterruptedException
         */
        private Queue<Key> getQueueFragment() throws KeyboardException {
            Queue<Key> queueFragment = new LinkedBlockingQueue<>();
            for (int i = 0; i < scanCodes.size(); i++) {
                if (scanCodes.peek() == null) {
                    throw new KeyboardException(WRONG_QUEUE_STATE);
                }
                if (queueContainsKeyLockPrefix()) {
                    keyLockChecked = false;
                    readKeyLocks();
                    break;
                }
                if (queueContainsCardPrefix()) {
                    msrChecked = false;
                    readCards();
                    break;
                }
                if (!scanCodes.isEmpty()) {
                    queueFragment.offer(scanCodes.poll());
                }
            }
            return queueFragment;
        }

    }

    /**
     * Первые коды из очереди соответствуют prefix
     *
     * @return
     */
    private boolean queueContainsKeyLockPrefix() {
        return queueContainsPrefix(keyLockPrefix);
    }

    /**
     * Первые коды из очереди соответствуют cardPrefix | cardPrefix2
     *
     * @return
     */
    private boolean queueContainsCardPrefix() {
        return queueContainsPrefix(cardPrefix) || queueContainsPrefix(cardPrefix2);
    }

    private boolean queueContainsPrefix(List<Integer> prefix) {
        if (scanCodes.size() == 1) {
            return prefix.get(0).equals(Optional.ofNullable(scanCodes.peek()).map(Key::getScanCode).orElse(null));
        } else if (scanCodes.size() > 1) {
            int i = Math.min(scanCodes.size(), prefix.size());
            int j = 0;
            for (Key code : scanCodes) {
                if (code == null) {
                    continue;
                }
                if (!prefix.get(j).equals(code.getScanCode())) {
                    return false;
                }
                if (++j >= i) {
                    break;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private void processScanCodesError(Exception e) {
        keyboardModule.logError(Key.extractKeyCodes(scanCodes), e);
        scanCodes.clear();
        keyboardModule.clear();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        scanCode = e.getKeyCode();
        if (isIgnoredCodeTA85p.negate().test(scanCode)) {
            keyPressed = true;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        msrChecked = false;
        keyLockChecked = false;

        int typedKeyCode = (int) e.getKeyChar();

        if (e.isControlDown() && this.scanCode != KeyEvent.VK_ENTER) {
            keyTyped = true;
            keyPressed = false;
            LOG.debug("after ctrl key, current key passed: >>> {} <<<", typedKeyCode);
            return;
        }

        if (isUpperCaseChar.test(this.scanCode)) {
            offerScanCode(this.scanCode, typedKeyCode);
        } else if (this.scanCode != typedKeyCode) {
            if (isSpecialChar.or(isStartEndJSONChar).test(typedKeyCode)) {
                offerScanCode(typedKeyCode);
            } else {
                offerScanCode(this.scanCode);
            }
        } else {
            offerScanCode(typedKeyCode);
        }

        keyTyped = true;
        keyPressed = false;
    }

    private void offerScanCode(int sc) {
        LOG.debug("Offer scancode {}", sc);
        scanCodes.offer(Key.of(sc));
    }

    private void offerScanCode(int sc, Integer ssc) {
        LOG.debug("Offer scancode {}, source {}", sc, ssc);
        scanCodes.offer(Key.of(ssc, sc));
    }

    @Override
    public void keyReleased(KeyEvent e) {
        msrChecked = false;
        keyLockChecked = false;

        if (keyPressed) {
            offerScanCode(e.getKeyCode());
        }
        keyReleased = true;
        keyPressed = false;
    }

    public void setMsrListener(MSREvent msrListener) {
        this.msrListener = msrListener;
    }

    public void setKeyLockListener(KeyLockEvent keyLockListener) {
        this.keyLockListener = keyLockListener;
    }

    public void setUseKeyLockMap(boolean useKeyLockMap) {
        this.useKeyLockMap = useKeyLockMap;
    }

    public boolean isUseKeyLockMap() {
        return useKeyLockMap;
    }

    public void setKeyLockMap(Map<String, Integer> keyLockMap) {
        this.keyLockMap = keyLockMap;
    }

    public Map<String, Integer> getKeyLockMap() {
        return keyLockMap;
    }

    public void setKeyboardModule(ScanCodesProcessor keyboardModule) {
        this.keyboardModule = keyboardModule;
    }

    @Override
    public void setCardPrefix(List<Integer> cardPrefix) {
    }

    @Override
    public void setCardPrefix2(List<Integer> cardPrefix2) {
        this.cardPrefix2 = cardPrefix2;
    }

    @Override
    public void setCardPrefix3(List<Integer> cardPrefix3) {
    }

    @Override
    public void setCardSufix(List<Integer> cardSuffix) {
    }

    @Override
    public void setCardSufix2(List<Integer> cardSuffix2) {
    }

    @Override
    public void setCardSufix3(List<Integer> cardSuffix3) {
    }

    @Override
    public void setKeyLockPrefix(List<Integer> keyLockPrefix) {
    }

    @Override
    public void setKeyLockSufix(List<Integer> keyLockSuffix) {
    }

    @Override
    public void setKeyboardTimeOut(Long keyboardTimeOut) {
        this.timeOut = keyboardTimeOut;
    }

    @Override
    public void setOtherTimeOut(Long otherTimeOut) {
    }

    @Override
    public void setKeyboardContactBounceTime(long contactBounceTime) {
    }


    @Override
    public void putStringAsKeys(String data) {
        List<Key> codes = new LinkedList<>();
        for (char c : data.toCharArray()) {
            codes.add(Key.of((int) c));
        }
        scanCodes.addAll(codes);
    }
}
