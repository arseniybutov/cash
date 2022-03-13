package ru.crystals.pos.keyboard.wincor.ta85p;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import ru.crystals.pos.keyboard.ScanCodesProcessor;
import ru.crystals.pos.keyboard.exception.KeyboardException;
import ru.crystals.pos.keylock.KeyLockEvent;
import ru.crystals.pos.msr.MSREvent;

import javax.swing.JPanel;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

/**
 * Тесты плагина клавиатуры Wincor-TA85. Нужны были перед рефакторингом. Проверяют, что при поступлении событий от клавиатуры будут генерироваться
 * соответствующие события.
 *
 * @author aksenov
 */
@Ignore
public class WincorTA85KeyboardServiceImplTest {

    private ScanCodesProcessor keyboardModule;

    private KeyLockEvent keyLockListener;

    private MSREvent msrListener;

    private Ta85pKeyboardServiceImpl sut;

    private WNParser parser;

    private List<Integer> receivedKeyLocks;

    private List<Integer> receivedBarcode;

    private List<Integer> receivedKeys;

    private int barcodeSuffix = '!';
    private JPanel source = new JPanel();

    public static void main(String[] args) throws Exception {
        WincorTA85KeyboardServiceImplTest test = new WincorTA85KeyboardServiceImplTest();
        test.init();
        test.testKeyLock();
    }

    @SuppressWarnings("unchecked")
    @Before
    public void init() throws KeyboardException {
        sut = new Ta85pKeyboardServiceImpl();
        keyboardModule = Mockito.mock(ScanCodesProcessor.class);
        keyLockListener = Mockito.mock(KeyLockEvent.class);
        msrListener = Mockito.mock(MSREvent.class);
        parser = new WNParser();
        receivedKeyLocks = Collections.synchronizedList(new ArrayList<>());
        receivedBarcode = Collections.synchronizedList(new ArrayList<>());
        receivedKeys = Collections.synchronizedList(new ArrayList<>());
        Mockito.when(keyboardModule.getScannerTimeStamp()).thenReturn(-1L);
        sut.setKeyLockListener(keyLockListener);
        sut.setMsrListener(msrListener);
        sut.setKeyboardModule(keyboardModule);

        Mockito.doAnswer((Answer<Void>) invocation -> {
            receivedKeyLocks.add((Integer) invocation.getArguments()[0]);
            return null;
        }).when(keyLockListener).eventKeyLock(Mockito.anyInt());

        Mockito.doAnswer((Answer<Boolean>) invocation -> {
            Queue<Integer> queue = (Queue<Integer>) invocation.getArguments()[0];
            boolean isDone = false;
            for (Integer code : queue) {
                if (code.equals(barcodeSuffix)) {
                    isDone = true;
                }
                receivedBarcode.add(code);
            }
            return isDone;
        }).when(keyboardModule).processScanner(Mockito.any(Queue.class));

        Mockito.doAnswer((Answer<Boolean>) invocation -> {
            Queue<Integer> queue = (Queue<Integer>) invocation.getArguments()[0];
            for (Integer code : queue) {
                receivedKeys.add(code);
            }
            return true;
        }).when(keyboardModule).processKeyboard(Mockito.any(Queue.class));

        sut.start();
    }

    @After
    public void stop() throws KeyboardException {
        sut.stop();
    }

    @Test
    public void testKeyLock() throws InterruptedException {
        // given
        Integer[] keyLocksFlow = new Integer[]{1, 2, 1, 2, 1, 1, 2, 2, 2, 2, 1};
        int count1 = 0, count2 = 0;
        // when
        for (Integer code : keyLocksFlow) {
            sendKeyLock(String.valueOf(code).charAt(0));
            if (code == 1) {
                count1++;
            }
            if (code == 2) {
                count2++;
            }
        }
        waitQueueIsEmpty();
        // then
        Mockito.verify(keyLockListener, Mockito.times(count1)).eventKeyLock(Mockito.eq(1));
        Mockito.verify(keyLockListener, Mockito.times(count2)).eventKeyLock(Mockito.eq(2));
        assertArrayEquals(keyLocksFlow, receivedKeyLocks.toArray(new Integer[receivedKeyLocks.size()]));
    }

    @Test
    public void testBarcodeAndKeyLock() throws Exception {
        // given
        String barcode = "0123456789012!";
        Integer[] codes = getCodes(barcode);
        // when
        sendCodes(codes);
        sendKeyLock('2');
        sendKeyLock('1');
        sendKeyLock('2');
        waitQueueIsEmpty();
        // then
        assertArrayEquals(codes, receivedBarcode.toArray(new Integer[receivedBarcode.size()]));
        Mockito.verify(keyLockListener, Mockito.times(1)).eventKeyLock(Mockito.eq(1));
        Mockito.verify(keyLockListener, Mockito.times(2)).eventKeyLock(Mockito.eq(2));
        assertArrayEquals(new Integer[]{2, 1, 2}, receivedKeyLocks.toArray(new Integer[receivedKeyLocks.size()]));
    }

    @Test
    public void testScanJSON() throws Exception {
        // given
        String barcode = "{ABCa{aBc}bc123}" + barcodeSuffix;
        Integer[] codes = getCodes(barcode);
        // when
        sendCodes(codes);
        waitQueueIsEmpty();
        // then
        assertArrayEquals(codes, receivedBarcode.toArray(new Integer[receivedBarcode.size()]));
    }

    @Test
    public void testBarcode() throws Exception {
        // given
        String barcode = "0123456789012" + barcodeSuffix;
        Integer[] codes = getCodes(barcode);
        // when
        sendCodes(codes);
        waitQueueIsEmpty();
        // then
        assertArrayEquals(codes, receivedBarcode.toArray(new Integer[receivedBarcode.size()]));
        Mockito.verify(keyLockListener, Mockito.times(0)).eventKeyLock(Mockito.anyInt());
    }

    @Test
    public void testKeyLockAndBarcode() throws Exception {
        // given
        String barcode = "0123456789012!";
        Integer[] codes = getCodes(barcode);
        // when
        sendCodes(codes);
        Thread.sleep(100L);
        sendKeyLock('2');
        waitQueueIsEmpty();
        // then
        assertArrayEquals(codes, receivedBarcode.toArray(new Integer[receivedBarcode.size()]));
        Mockito.verify(keyLockListener, Mockito.times(1)).eventKeyLock(Mockito.eq(2));
        assertArrayEquals(new Integer[]{2}, receivedKeyLocks.toArray(new Integer[receivedKeyLocks.size()]));
    }

    @Test
    public void testTwoKey() throws Exception {
        // given
        Integer[] codes = new Integer[]{65, 75};
        // when
        sendCodes(codes);
        waitReceivedKeys(2);
        // then
        assertArrayEquals(codes, receivedKeys.toArray(new Integer[receivedKeys.size()]));
    }

    @Test
    public void testTwoKeyAndKeyLocks() throws Exception {
        // given
        Integer[] codes = new Integer[]{65, 75};
        // when
        sendCodes(codes);
        sendKeyLock('2');
        sendKeyLock('1');
        sendKeyLock('2');
        waitReceivedKeys(2);
        // then
        Mockito.verify(keyLockListener, Mockito.times(1)).eventKeyLock(Mockito.eq(1));
        Mockito.verify(keyLockListener, Mockito.times(2)).eventKeyLock(Mockito.eq(2));
        assertArrayEquals(codes, receivedKeys.toArray(new Integer[receivedKeys.size()]));
    }

    @Test
    public void testOneCodeFromPrefix() throws Exception {
        // given
        Integer[] codes = new Integer[]{17, 65};
        // when
        sendCodes(codes);
        waitReceivedKeys(2);
        // then
        assertArrayEquals(codes, receivedKeys.toArray(new Integer[receivedKeys.size()]));
    }

    @Test
    public void testCard() throws Exception {
        // given
        String card = "6635128897451232256IVANOV-IVAN";
        Integer[] codes = getCodes(card);
        // when
        sendCard(codes);
        //then
        waitQueueIsEmpty();
        Mockito.verify(msrListener).eventMSR(null, card, null, null);
    }

    @Test
    public void testKeysThenCard() throws Exception {
        // given
        Integer[] keys = new Integer[]{65, 66};
        String card = "4658784151";
        Integer[] cardCodes = getCodes(card);
        // when
        sendCodes(keys);
        sendCard(cardCodes);
        //then
        waitReceivedKeys(keys.length);
        Mockito.verify(msrListener).eventMSR(null, card, null, null);
        assertArrayEquals(keys, receivedKeys.toArray(new Integer[receivedKeys.size()]));
    }

    @Test
    public void testSequenceOf_Keys_Card_Lock_Keys() throws Exception {
        // given
        String card = "4658784151";
        Integer[] cardCodes = getCodes(card);
        Integer[] keys = new Integer[]{65, 75};
        Integer[] keys2 = new Integer[]{68, 69};
        Integer[] allKeys = new Integer[keys.length + keys2.length];
        System.arraycopy(keys, 0, allKeys, 0, keys.length);
        System.arraycopy(keys2, 0, allKeys, keys.length, keys2.length);

        // when
        sendCodes(keys);
        sendCard(cardCodes);
        sendKeyLock('2');
        sendKeyLock('1');
        sendCodes(keys2);
        //then
        waitReceivedKeys(keys.length);
        Mockito.verify(msrListener).eventMSR(null, card, null, null);
        assertArrayEquals(allKeys, receivedKeys.toArray(new Integer[receivedKeys.size()]));
        Mockito.verify(keyLockListener, Mockito.times(1)).eventKeyLock(Mockito.eq(1));
        Mockito.verify(keyLockListener, Mockito.times(1)).eventKeyLock(Mockito.eq(2));
    }

    // helpers:

    private Integer[] getCodes(String barcode) {
        Integer[] result = new Integer[barcode.length()];
        for (int i = 0; i < barcode.length(); i++) {
            result[i] = (int) barcode.charAt(i);
        }
        return result;
    }

    private void sendCodes(Integer[] codes) {
        for (Integer code : codes) {
            sendScanCode(code);
        }
    }

    private void sendKeyLock(char position) {
        for (Integer code : parser.getKeyLockPrefix()) {
            sendScanCode(code);
        }
        sendScanCode((int) position);
    }

    private void sendCard(Integer[] codes) {
        for (Integer code : parser.getCardPrefix()) {
            sendScanCode(code);
        }
        sendCodes(codes);
        for (Integer code : parser.getCardSufix()) {
            sendScanCode(code);
        }
    }

    private KeyEvent getEvent(int code) {
        return new KeyEvent(source, 0, 0, 0, code, (char) code);
    }

    private void sendScanCode(int code) {
        KeyEvent event = getEvent(code);
        sut.keyPressed(event);
        sut.keyTyped(event);
        sut.keyReleased(event);
    }

    private void waitQueueIsEmpty() throws InterruptedException {
        long time = System.currentTimeMillis();
        while (sut.scanCodes.size() > 0) {
            Thread.sleep(20L);
            failIfLongWait(time);
        }
    }

    private void waitReceivedKeys(int size) throws InterruptedException {
        long time = System.currentTimeMillis();
        while ((sut.scanCodes.size() > 0) || (receivedKeys.size() < size)) {
            Thread.sleep(20L);
            failIfLongWait(time);
        }
    }

    private void failIfLongWait(long startTime) {
        if ((System.currentTimeMillis() - startTime) > 5000L) {
            fail("Test failed after 5 seconds");
        }
    }

}
