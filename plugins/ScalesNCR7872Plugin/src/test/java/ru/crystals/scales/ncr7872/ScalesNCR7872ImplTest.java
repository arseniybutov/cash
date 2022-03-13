package ru.crystals.scales.ncr7872;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import ru.crystals.pos.CashException;
import ru.crystals.pos.scale.exception.ScaleException;
import ru.crystals.pos.utils.PortAdapterException;
import ru.crystals.pos.utils.SerialPortAdapter;
import ru.crystals.utils.time.Timer;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ScalesNCR7872ImplTest {
    @Mock
    private SerialPortAdapter serialPortAdapter;
    @Mock
    private ExecutorService executor;
    private static final byte[] GET_WEIGHT_COMMAND = new byte[]{0x31, 0x34, 0x03, 0x06};
    @Spy
    @InjectMocks
    private ScalesNCR7872Impl scales = new ScalesNCR7872Impl();

    @Mock
    private Timer receiveWeightTimer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        scales.receiveWeightTimerSupplier = () -> receiveWeightTimer;
        doNothing().when(serialPortAdapter).write(GET_WEIGHT_COMMAND);
    }

    @Test
    public void testStart() throws Exception {
        doNothing().when(serialPortAdapter).openPort();
        doNothing().when(executor).execute(Matchers.any(Runnable.class));
        scales.start();
        verify(serialPortAdapter).setRTS(true);
        verify(executor).execute(Matchers.any(Runnable.class));
    }

    @Test(expected = CashException.class)
    public void testStartPortAdapterException() throws Exception {
        doThrow(new PortAdapterException("Error")).when(serialPortAdapter).openPort();
        scales.start();
    }

    @Test(expected = CashException.class)
    public void testStartIOException() throws Exception {
        doThrow(new IOException()).when(serialPortAdapter).openPort();
        scales.start();
    }

    @Test
    public void testStop() {
        scales.stop();
        verify(executor).shutdown();
        verify(serialPortAdapter).close();
    }

    @Test
    public void testGetWeightWithNoError() throws Exception {
        shouldReturnWeightForSuccessStatuses(NcrStatus.NO_ERROR);
    }

    @Test
    public void testGetWeightWithStableZero() throws Exception {
        shouldReturnWeightForSuccessStatuses(NcrStatus.STABLE_ZERO_WEIGHT);
    }

    private void shouldReturnWeightForSuccessStatuses(NcrStatus status) throws Exception {
        when(receiveWeightTimer.isExpired())
                .thenReturn(false)
                .thenReturn(false)
                .then(inv -> {
                    scales.weight = 123;
                    scales.weightReceived = true;
                    scales.status = status;
                    return false;
                });

        assertEquals("Invalid weight with status " + status, scales.getWeight(), 123);
        assertFalse(scales.weightReceived);

        verify(receiveWeightTimer, times(3)).isExpired();
    }

    @Test
    public void testGetWeightUnstable() throws Exception {
        shouldReturnZeroForUnstableStatus(NcrStatus.SCALES_UNSTABLE);
    }

    @Test
    public void testGetWeightNegative() throws Exception {
        shouldReturnZeroForUnstableStatus(NcrStatus.NEGATIVE_WEIGHT);
    }

    @Test
    public void testGetWeightOverload() throws Exception {
        shouldReturnZeroForUnstableStatus(NcrStatus.SCALES_OVERLOAD);
    }

    private void shouldReturnZeroForUnstableStatus(NcrStatus status) throws Exception {
        when(receiveWeightTimer.isExpired())
                .thenReturn(false)
                .thenReturn(false)
                .then(inv -> {
                    scales.weight = 123;
                    scales.status = status;
                    scales.weightReceived = true;
                    return false;
                });

        assertEquals("Weight for unstable status should be zero: " + status, scales.getWeight(), 0);
        assertFalse(scales.weightReceived);

        verify(receiveWeightTimer, times(3)).isExpired();
    }

    @Test
    public void testGetWeightFatalError() {
        when(receiveWeightTimer.isExpired())
                .thenReturn(false)
                .thenReturn(false)
                .then(inv -> {
                    scales.status = NcrStatus.SCALES_NOT_READY;
                    scales.weightReceived = true;
                    return false;
                });
        try {
            scales.getWeight();
            fail("No expected exception");
        } catch (ScaleException e) {
            assertFalse(scales.weightReceived);
            verify(receiveWeightTimer, times(3)).isExpired();
        } catch (Exception e) {
            fail("No expected exception (another type): " + e.getClass());
        }
    }

    @Test
    public void testGetWeightScalesNotRespond() {
        when(receiveWeightTimer.isExpired())
                .thenReturn(false)
                .thenReturn(false)
                .thenReturn(true);
        scales.weightReceived = true;
        try {
            scales.getWeight();
            fail("No expected exception");
        } catch (ScaleException e) {
            assertFalse(scales.weightReceived);
            verify(receiveWeightTimer, times(3)).isExpired();
        } catch (Exception e) {
            fail("No expected exception (another type): " + e.getClass());
        }
    }

    @Test(expected = ScaleException.class)
    public void testGetWeightSerialPortAdapterThrowsException() throws Exception {
        doThrow(new IOException()).when(serialPortAdapter).write(GET_WEIGHT_COMMAND);
        scales.getWeight();
    }

    @Test
    public void testModuleCheckState() {
        when(serialPortAdapter.isConnected()).thenReturn(true);
        assertTrue(scales.moduleCheckState());
        verify(serialPortAdapter).isConnected();
    }

    @Test
    public void testModuleCheckStateSerialPortAdapterNotConnected() {
        assertFalse(scales.moduleCheckState());
    }

    @Test
    public void convert2DBarcodeTest() {
        assertBarcodeConverted("QR", "]Q03031303133343630303433323031383832317458535175256438303035303530303030393343357141",
                "010134600432018821tXSQu%d800505000093C5qA");
        assertBarcodeConverted("Datamatrix", "]d03230342D3036383339363539353831323430373137383835393738353739303130",
                "204-06839659581240717885978579010");
        assertBarcodeConverted("PDF417", "]L232324E30303030324E554E47344E504A5648533749454F3731303138303032303039" +
                        "353831464442434A593357584A5A5A485933473651504356434C5849444858413231",
                "22N00002NUNG4NPJVHS7IEO71018002009581FDBCJY3WXJZZHY3G6QPCVCLXIDHXA21");
        assertBarcodeConverted("GS1 Databar Expanded", "]e03031303935303131303135333030303331373134303730343130414273762D313233",
                "01095011015300031714070410ABsv-123");
    }

    private void assertBarcodeConverted(String description, String rawBarcode, String expected) {
        String result = scales.convert2DBarcode(rawBarcode);
        assertEquals("Raw 2D barcode not parsed: " + description, expected, result);
    }
}
